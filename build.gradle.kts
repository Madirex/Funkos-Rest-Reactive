plugins {
    id("java")
}

group = "com.madirex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")

    implementation("io.projectreactor:reactor-core:3.5.10")
    implementation("com.h2database:h2:2.1.210")
    implementation("org.mybatis:mybatis:3.5.13")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.test {
    useJUnitPlatform()
}