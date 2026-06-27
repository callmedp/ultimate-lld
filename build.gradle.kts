plugins {
    java
    application
}

group = "com.ultimatelld"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Each module has its own driver (main class). Run a specific one with:
//   gradle run -Pdriver=com.ultimatelld.theory.module01solid.driver.Driver
// Theory modules live under com.ultimatelld.theory.*; question-bank problems under com.ultimatelld.problems.*
application {
    val driver = (project.findProperty("driver") as String?)
        ?: "com.ultimatelld.theory.module01solid.driver.Driver"
    mainClass.set(driver)
}
