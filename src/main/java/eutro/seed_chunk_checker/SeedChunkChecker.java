package eutro.seed_chunk_checker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToIntFunction;

public class SeedChunkChecker {
    public static void main(String[] args) {
        int x = Integer.parseInt(args[1]);
        int y = Integer.parseInt(args[2]);
        File file = new File(args[0]);
        if (!file.mkdir()) {
            System.err.printf("Directory %s already exists (seed may have already been checked)%n", args[0]);
        }
        setSeed(args[0], file);
        try (MinecraftServer server = startDedicatedServer("--nogui", "--universe", args[0])) {
            ServerWorld world;
            while ((world = server.getWorld(World.OVERWORLD)) == null) {
                //noinspection BusyWait - cry about it
                Thread.sleep(0);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject obj = new JsonObject();
            obj.addProperty("x", x);
            obj.addProperty("y", y);
            obj.add("frequencies", checkChunk(world, x, y));
            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(new File(file, "frequency.json")))) {
                gson.toJson(obj, writer);
            }
        } catch (Exception ignored) {
        }
        System.exit(0);
    }

    private static JsonObject checkChunk(ServerWorld world, int x, int y) {
        Object2IntMap<Block> counts = new Object2IntOpenHashMap<>();
        ChunkPos chunkPos = new ChunkPos(x, y);
        WorldChunk chunk = world.getChunk(x, y);
        for (BlockPos pos : BlockPos.iterate(
                chunkPos.toBlockPos(0, 0, 0),
                chunkPos.toBlockPos(15, 255, 15)
        )) {
            Block state = chunk.getBlockState(pos).getBlock();
            counts.put(state, counts.getInt(state) + 1);
        }
        JsonObject obj = new JsonObject();
        counts.object2IntEntrySet()
                .stream()
                .sorted(Comparator.comparingInt((ToIntFunction<? super Object2IntMap.Entry<?>>) Object2IntMap.Entry::getIntValue).reversed())
                .forEachOrdered(entry -> obj.addProperty(Registry.BLOCK.getId(entry.getKey()).toString(), entry.getIntValue()));
        return obj;
    }

    private static void setSeed(String seed, File file) {
        ServerPropertiesLoader properties = new ServerPropertiesLoader(new File(file, "server.properties").toPath());
        Properties props;
        try {
            Field propertiesField = AbstractPropertiesHandler.class.getDeclaredField("properties");
            propertiesField.setAccessible(true);
            props = (Properties) propertiesField.get(properties.getPropertiesHandler());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        props.setProperty("level-seed", seed);
        properties.store();
    }

    private static MinecraftServer startDedicatedServer(String... args) {
        Main.main(args);
        return getServer();
    }

    private static MinecraftServer getServer() {
        // I have the best hacks.
        Thread serverThread = Thread.getAllStackTraces()
                .keySet()
                .stream()
                .filter(thread -> "Server thread".equals(thread.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Couldn't find server thread"));
        try {
            Field target = Thread.class.getDeclaredField("target");
            target.setAccessible(true);
            Runnable runnable = (Runnable) target.get(serverThread);
            Field closureField = Arrays.stream(runnable.getClass().getDeclaredFields())
                    .filter(field -> AtomicReference.class.isAssignableFrom(field.getType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Couldn't find server field"));
            closureField.setAccessible(true);
            return (MinecraftServer) ((AtomicReference<?>) closureField.get(runnable)).get();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Couldn't get running server", e);
        }
    }
}
