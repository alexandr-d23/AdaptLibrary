plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.7.10"
}
repositories {
    google()
    mavenCentral()

}

dependencies {
    implementation("com.android.tools:r8:8.0.40")
    implementation (gradleApi())

    implementation(kotlin("stdlib-jdk8"))
    implementation ("com.android.tools.build:gradle:3.5.4")
    implementation ("com.android.tools.build:gradle-api:3.5.4")
    implementation ("org.ow2.asm:asm:7.1")
    implementation ("org.javassist:javassist:3.25.0-GA")
    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.5.1")
    implementation ("com.google.android.material:material:1.6.1")
    implementation ("com.github.adriankuta:tree-structure:3.0.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
    implementation("com.github.demidko:aot:2022.11.28")
}

gradlePlugin {
    plugins {
        create("voice_gradle") {
            id = "voice_gradle"
            implementationClass = "com.diner.plugin.bytecode.VoicePlugin"
        }
    }
}