plugins {
    java
    alias(libs.plugins.shadow)
}

group = "me.matl114"
// version 由 gradle.properties 提供（release.yml 从该文件读取）

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.bg-software.com/repository/api/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    // Core
    compileOnly(libs.paper.api)
    compileOnly(libs.slimefun4)
    compileOnly(libs.guizhan.lib.plugin)

    // Tools etc. (shade)
    implementation(libs.more.persistent.data.types)
    implementation(libs.matlib.all)
    implementation(libs.matlib.slimefun)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Supported Plugins
    compileOnly(libs.infinity.expansion)
    compileOnly(libs.networks)
    compileOnly(libs.wild.stacker.api)
    compileOnly(libs.rosestacker)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version.toString())
        }
    }

    shadowJar {
        archiveBaseName.set("LogiTech")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        minimize()

        // 与原 pom.xml shade 配置保持一致（bstats 原本就无依赖，略）
        relocate("com.jeff_media.morepersistentdatatypes", "me.matl114.libs.morepersistentdatatypes")
        relocate("me.matl114.matlib", "me.matl114.libs.matlib")

        exclude("META-INF/*")

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        mergeServiceFiles()
    }

    build {
        dependsOn(shadowJar)
    }
}

defaultTasks("clean", "build")
