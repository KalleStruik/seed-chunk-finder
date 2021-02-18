pluginManagement {
    repositories {
        jcenter()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
    plugins {
        id("fabric-loom").version("0.5-SNAPSHOT")
    }
}
rootProject.name = "seed-chunk-checker"

