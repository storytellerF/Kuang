import com.android.build.gradle.internal.tasks.factory.dependsOn

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.22"
    id("io.ktor.plugin") version "2.1.3"
    id("com.storyteller_f.song") version "0.0.1"
}

group = "com.example"
version = "0.0.1"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
val userHome = System.getProperty("user.home")!!
val convertJarToDex = tasks.register<Exec>("convertJarToDex") {
    workingDir = File(buildDir, "libs")
    commandLine = listOf(
        "$userHome/Library/Android/sdk/build-tools/29.0.1/dx",
        "--dex",
        "--output=samples.jar",
        "$buildDir/libs/com.example.ktor-samples-0.0.1.jar"
    )
}

tasks.build {
    finalizedBy(convertJarToDex)
}

song {
    transfers.set(listOf("$buildDir/libs/samples.jar"))
    adb.set("$userHome/Library/Android/sdk/platform-tools/adb")
    paths.set(listOf())
    packages.set(listOf("com.storyteller_f.kuang" to "files"))
    outputName.set("sample.jar")
}

afterEvaluate {
    val taskName = "dispatchApk"
    tasks.findByName(taskName)?.let { task ->
        task.dependsOn(convertJarToDex)
        convertJarToDex.get().finalizedBy(task)
    }

}