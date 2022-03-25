plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("BuildManager") {
            id = "com.nitin-detekt.plugin"
            implementationClass = "BuildManager"
            version = "1.0.0"
        }
    }
}
