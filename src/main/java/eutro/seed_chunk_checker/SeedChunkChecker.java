package eutro.seed_chunk_checker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import net.minecraft.server.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public class SeedChunkChecker {
    // You should really keep these two equal.
    private static final int X_RANGE = 100;
    private static final int Z_RANGE = 100;

    public static void main(String[] args) {
        String seed = args[0];
        File file = new File(seed);
        if (!file.mkdir()) {
            System.err.printf("Directory %s already exists (seed may have already been checked)%n", seed);
        }
        setSeed(seed);
        try {
            MinecraftServer server = startDedicatedServer("--nogui", "--world", seed);
            ServerWorld world;
            while ((world = server.getWorld(World.OVERWORLD)) == null) {
                //noinspection BusyWait - cry about it
                Thread.sleep(0);
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject data = checkWorld(world);
            try (JsonWriter writer = gson.newJsonWriter(new FileWriter(new File(file, "data.json")))) {
                gson.toJson(data, writer);
                writer.flush();
            }

            server.stop(true);
        } catch (Exception ignored) {
        }

        try {
//            // This is beautiful.
//            String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
//            Runtime.getRuntime().exec("kill -9 " + pid);
        } catch (Exception youAreDeadAnyway) {}
    }

    private static JsonObject checkWorld(ServerWorld world) {
        System.out.println(world.getSeed());
        int[][] grid = new int[X_RANGE*2+1][Z_RANGE*2+1];
        for (int x = -X_RANGE; x <= X_RANGE; x++) {
            for (int z = -Z_RANGE; z <= Z_RANGE; z++) {
                int total = 0;

                for (int y = 5; y <= 80; y++) {
                    String block = Registry.BLOCK.getId(world.getBlockState(new BlockPos(x, y, z)).getBlock()).toString();
                    if (block.equals("minecraft:air") || block.equals("minecraft:cave_air"))
                        continue;

                    total++;
                }
                grid[x+X_RANGE][z+Z_RANGE] = total;
            }
        }

        return bestArea(grid);
    }

    public static JsonObject bestArea(int[][] list){
        int totalLines = X_RANGE * 2;
        int sideLength = 16;
        int bestAreaNum = Integer.MAX_VALUE;
        int[] bestCoords = new int[2];
        for (int x = 0; x < totalLines-sideLength+1; x++) {
            for (int y = 0; y < totalLines-sideLength+1; y++) {
                int total = 0;
                for (int i = x; i < x+sideLength; i++) {
                    for (int j = y; j < y+sideLength; j++) {
                        total+= list[i][j];
                    }
                }
                if(total < bestAreaNum){
                    bestAreaNum = total;
                    bestCoords[0] = x-(totalLines / 2);
                    bestCoords[1] = y-(totalLines / 2);
                }

            }
        }
        System.out.println(bestAreaNum);
        JsonObject obj = new JsonObject();
        obj.add("area", new JsonPrimitive(bestAreaNum));
        obj.add("x", new JsonPrimitive(bestCoords[0]));
        obj.add("z", new JsonPrimitive(bestCoords[1]));
        System.out.println(obj);
        return obj;
    }

    private static void setSeed(String seed) {
        ServerPropertiesLoader properties = new ServerPropertiesLoader(new File("server.properties").toPath());
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
