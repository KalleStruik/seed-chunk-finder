plugins {
    java
    application
    id("fabric-loom")
}

group = "eutro"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.16.1")
    mappings("net.fabricmc:yarn:1.16.1+build.1:v2")
    modImplementation("net.fabricmc:fabric-loader:0.11.0") // pulls in the rest of the deps
}
