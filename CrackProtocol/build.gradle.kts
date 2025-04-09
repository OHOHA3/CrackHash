group = "ru.nsu.leontev"
version = "0.0.1-SNAPSHOT"

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.4")
    implementation("org.glassfish.jaxb:jaxb-xjc:4.0.4")
}

tasks.register<JavaExec>("xjcRequest") {
    group = "build"
    description = "Генерация Java-классов для запроса на взлом из XSD"

    val xsdPath = "src/main/resources/crackRequest.xsd"
    val outputDir = "src/main/java"
    val packageName = "ru.nsu.leontev.request"

    mainClass.set("com.sun.tools.xjc.XJCFacade")

    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf(
            xsdPath,
            "-d", outputDir,
            "-p", packageName
    )
}

tasks.register<JavaExec>("xjcResponse") {
    group = "build"
    description = "Генерация Java-классов для отправки статуса по взлому из XSD"

    val xsdPath = "src/main/resources/crackResponse.xsd"
    val outputDir = "src/main/java"
    val packageName = "ru.nsu.leontev.response"

    mainClass.set("com.sun.tools.xjc.XJCFacade")

    classpath = sourceSets.main.get().runtimeClasspath
    args = listOf(
            xsdPath,
            "-d", outputDir,
            "-p", packageName
    )
}

tasks.named("build"){
    dependsOn("xjcRequest", "xjcResponse")
}

tasks.test {
    useJUnitPlatform()
}
