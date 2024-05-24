plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.19"
    id("groovy") 
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.3.8"
    id("io.micronaut.aot") version "4.3.8"

    id("io.micronaut.test-resources") version "4.3.8"
    id("com.google.osdetector") version "1.7.0"
}

version = "0.1"
group = "com.vladovsiychuk"

val kotlinVersion= project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    //base dependencies
    ksp("io.micronaut:micronaut-http-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
    compileOnly("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("io.micronaut:micronaut-http-client")

    //custom dependencies
    ksp("io.micronaut.data:micronaut-data-processor")
    ksp("io.micronaut.security:micronaut-security-annotations")
    implementation ("io.micronaut:micronaut-retry")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("io.micronaut.liquibase:micronaut-liquibase")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut:micronaut-websocket")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    runtimeOnly("dev.miku:r2dbc-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("io.r2dbc:r2dbc-pool")
    aotPlugins(platform("io.micronaut.platform:micronaut-platform:4.4.2"))
    aotPlugins("io.micronaut.security:micronaut-security-aot")

    if (osdetector.arch.equals("aarch_64")) {
        implementation("io.netty:netty-resolver-dns-native-macos:4.1.72.Final:osx-aarch_64")
    }
}


application {
    mainClass = "com.saga_orchestrator_ddd_chat.ApplicationKt"
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
}


graalvmNative.toolchainDetection = false
micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("com.vladovsiychuk.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
        configurationProperties.put("micronaut.security.jwks.enabled","false")
    }
}


tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "17"
}

tasks.test {
    if (project.hasProperty("excludeIntegrationTests")) {
        exclude("**/*Integration*.class")
    }
}
