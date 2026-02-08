plugins {
    java
}

group = "net.minecraft"
version = "1.8.9"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation(group = "com.ibm.icu", name = "icu4j", version = "77.1")

    implementation(group = "com.github.oshi", name = "oshi-core", version = "6.6.5")

    implementation(group = "com.mojang", name = "authlib", version = "1.5.21") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-core")
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "org.apache.commons", module = "commons-lang3")
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "com.google.code.gson", module = "gson")
    }

    implementation(group = "com.google.guava", name = "guava", version = "33.4.8-jre")
    implementation(group = "com.google.code.gson", name = "gson", version = "2.13.1")

    implementation(group = "io.netty", name = "netty-all", version = "4.2.1.Final")

    implementation(group = "commons-io", name = "commons-io", version = "2.19.0")
    implementation(group = "commons-codec", name = "commons-codec", version = "1.18.0")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.17.0")

    implementation(group = "org.apache.commons", name = "commons-compress", version = "1.27.1") {
        exclude(group = "commons-io", module = "commons-io")
        exclude(group = "commons-codec", module = "commons-codec")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }

    implementation(group = "org.apache.commons", name = "commons-text", version = "1.13.1")

    implementation(group = "org.jcommander", name = "jcommander", version = "2.0")
    implementation(group = "net.sf.jopt-simple", name = "jopt-simple", version = "5.0.4")
    implementation(group = "org.joml", name = "joml", version = "1.10.8")

    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.24.3")
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.24.3")

    implementation(group = "org.lwjgl", name = "lwjgl", version = "3.3.6")
    implementation(group = "org.lwjgl", name = "lwjgl-glfw", version = "3.3.6")
    implementation(group = "org.lwjgl", name = "lwjgl-openal", version = "3.3.6")
    implementation(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.6")
    implementation(group = "org.lwjgl", name = "lwjgl-stb", version = "3.3.6")

    runtimeOnly(group = "org.lwjgl", name = "lwjgl", version = "3.3.6", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-glfw", version = "3.3.6", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-openal", version = "3.3.6", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", version = "3.3.6", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-stb", version = "3.3.6", classifier = "natives-windows")
}