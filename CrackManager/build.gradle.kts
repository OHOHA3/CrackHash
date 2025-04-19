group = "ru.nsu.leontev"
version = "0.0.1-SNAPSHOT"

plugins {
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework:spring-oxm")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.15.0")
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.12.3")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.0")

    implementation(project(":CrackProtocol"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}