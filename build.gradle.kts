group = "ru.nsu.leontev"
version = "0.0.1-SNAPSHOT"

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(20))
        }
    }

    repositories {
        mavenCentral()
    }
}

tasks.register<Exec>("crackHashStart") {
    group = "deploy"
    description = "Запуск контейнеров"

    dependsOn(subprojects.map { it.tasks.named("build") })

    commandLine("docker-compose", "up", "-d", "--build")
}

tasks.register<Exec>("crackHashStop") {
    group = "deploy"
    description = "Остановка контейнеров"

    commandLine("docker-compose", "down", "--rmi", "all", "-v", "--remove-orphans")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
