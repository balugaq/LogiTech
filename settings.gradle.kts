plugins {
    // 本机/CI 无 JDK 21 时自动下载对应工具链
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "LogiTech"
