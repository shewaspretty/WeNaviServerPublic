plugins {
    id("java")
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm")
}

group = "com.hrlee"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://mvn.topobyte.de") }
    maven { url = uri("https://mvn.slimjars.com") }
    maven { url = uri("https://repo.osgeo.org/repository/release/")}
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.jsonwebtoken:jjwt:0.9.1")
    implementation("javax.xml.bind:jaxb-api:2.3.1")

    implementation("mysql:mysql-connector-java:8.0.33")

    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("de.topobyte:osm4j-pbf:1.4.1")
    testImplementation("de.topobyte:osm4j-pbf:1.4.1")

    implementation("org.geotools:gt-bom:34.2")
    testImplementation("org.geotools:gt-bom:34.2")
    implementation("org.geotools:gt-main:34.2")
    testImplementation("org.geotools:gt-main:34.2")
    implementation("org.geotools:gt-referencing:34.2")
    testImplementation("org.geotools:gt-referencing:34.2")

    implementation("org.geotools:gt-shapefile:34.2")
    testImplementation("org.geotools:gt-shapefile:34.2")

    implementation("org.geotools:gt-epsg-hsql:34.2")
    testImplementation("org.geotools:gt-epsg-hsql:34.2")
    /*
    Recover when implement osm/CoordinateTool.getInboundingBox() in another way which uses this library
     */
    //implementation("org.locationtech.jts:jts-core:1.20.0")
    implementation("org.geotools:gt-api:34.2")
    testImplementation("org.geotools:gt-api:34.2")
    implementation(kotlin("stdlib-jdk8"))

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}