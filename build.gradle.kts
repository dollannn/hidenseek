plugins {
    java
    id("com.gradleup.shadow") version "9.0.0-beta15"
}

group = "se.playpark"
version = "1.0.0"
description = "Hide and Seek Plugin"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/")
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    // Provided dependencies (will not be included in final jar)
    compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT") {
        exclude(group = "junit")
    }
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    
    // Compile time only
    compileOnly("org.jetbrains:annotations:23.0.0")
    
    // Dependencies to be shaded (included in final jar)
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("com.github.cryptomorin:XSeries:9.4.0")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.2")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    
    shadowJar {
        // Relocate dependencies to avoid conflicts
        relocate("com.cryptomorin.xseries", "se.playpark.depend.xseries")
        relocate("com.zaxxer.hikari", "se.playpark.depend.hikari")
        
        // Only include specific dependencies
        dependencies {
            include(dependency("com.github.cryptomorin:XSeries"))
            include(dependency("org.xerial:sqlite-jdbc"))
            include(dependency("org.mariadb.jdbc:mariadb-java-client"))
            include(dependency("com.zaxxer:HikariCP"))
        }
        
        // Exclude unwanted files
        exclude("META-INF/**")
        exclude("org/sqlite/native/Mac/**")
        exclude("org/sqlite/native/Linux-Android/**")
        exclude("org/sqlite/native/Linux/ppc64/**")
        exclude("org/sqlite/native/Linux/armv7/**")
        
        // Handle service files
        mergeServiceFiles()
        
        archiveClassifier.set("")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    // Disable the default jar task since we want shadowJar
    jar {
        enabled = false
    }
} 