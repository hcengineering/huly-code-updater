plugins {
    application
    java
}

repositories {
    mavenCentral()
}

sourceSets.main {
    java.setSrcDirs(listOf("src/main/java"))
    resources.setSrcDirs(listOf("src/main/resources"))
}

dependencies {
    implementation(files("lib/com.intellij.updater.updater-3.0.jar"))
    implementation("org.jetbrains:annotations:24.0.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.2")
    implementation("commons-io:commons-io:2.18.0")
    implementation("org.apache.commons:commons-compress:1.26.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    application {
        mainClass.set("com.hulylabs.updater.UpdateGenerator")
    }
}