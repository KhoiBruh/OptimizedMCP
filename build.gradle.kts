plugins {
    java
    id("org.graalvm.buildtools.native") version "0.11.1"
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

    implementation(group = "com.google.code.gson", name = "gson", version = "2.13.1")
    implementation(group = "com.google.guava", name = "guava", version = "33.4.8-jre")

    implementation(group = "io.netty", name = "netty-buffer", version = "4.2.10.Final")
    implementation(group = "io.netty", name = "netty-handler", version = "4.2.10.Final")
    implementation(group = "io.netty", name = "netty-transport-native-epoll", version = "4.2.10.Final")

    implementation(group = "commons-io", name = "commons-io", version = "2.21.0")
    implementation(group = "commons-codec", name = "commons-codec", version = "1.21.0")
    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.20.0")

    implementation(group = "org.apache.logging.log4j", name = "log4j-api", version = "2.25.3")
    implementation(group = "org.apache.logging.log4j", name = "log4j-core", version = "2.25.3")

    implementation(group = "info.picocli", name = "picocli", version = "4.7.7")
    annotationProcessor(group = "info.picocli", name = "picocli-codegen", version = "4.7.7")

    implementation(group = "org.joml", name = "joml", version = "1.10.8")
    implementation(group = "org.joml", name = "joml-primitives", version = "1.10.0")

    implementation(platform("org.lwjgl:lwjgl-bom:3.4.1"))

    implementation(group = "org.lwjgl", name = "lwjgl")
    implementation(group = "org.lwjgl", name = "lwjgl-stb")
    implementation(group = "org.lwjgl", name = "lwjgl-glfw")
    implementation(group = "org.lwjgl", name = "lwjgl-openal")
    implementation(group = "org.lwjgl", name = "lwjgl-opengl")
    implementation(group = "org.lwjgl", name = "lwjgl-rpmalloc")

    runtimeOnly(group = "org.lwjgl", name = "lwjgl", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-stb", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-glfw", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-openal", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-opengl", classifier = "natives-windows")
    runtimeOnly(group = "org.lwjgl", name = "lwjgl-rpmalloc", classifier = "natives-windows")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

// Task to extract LWJGL natives from JARs so they can be shipped alongside the native image
val extractLwjglNatives by tasks.registering {
    val nativesDir = layout.buildDirectory.dir("lwjgl-natives")
    outputs.dir(nativesDir)
    doLast {
        val outDir = nativesDir.get().asFile
        outDir.mkdirs()
        configurations.runtimeClasspath.get().files
            .filter { it.name.contains("natives") && it.name.contains("lwjgl") }
            .forEach { jar ->
                zipTree(jar).matching {
                    include("**/*.dll", "**/*.so", "**/*.dylib")
                }.forEach { nativeFile ->
                    nativeFile.copyTo(File(outDir, nativeFile.name), overwrite = true)
                }
            }
    }
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
                mainClass = "Start"
            })
            buildArgs.addAll(
                "--enable-url-protocols=https,http",
                // Include LWJGL native libraries as resources so SharedLibraryLoader can extract them
                "-H:IncludeResources=.*\\\\.dll$",
                "-H:IncludeResources=.*\\\\.so$",
                "-H:IncludeResources=.*\\\\.dylib$",
                // LWJGL needs JNI
                "-H:+JNI",
                // Allow incomplete classpath
                "--allow-incomplete-classpath"
            )
        }
    }
}