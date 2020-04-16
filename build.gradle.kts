plugins {
    id("java")
    id("application")
    id("antlr")

    id("com.github.ben-manes.versions") version "0.28.0"
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

repositories {
    jcenter()
}

dependencies {
    antlr("org.antlr:antlr4:4.8")

    implementation("com.google.code.gson:gson:2.8.6")

    testImplementation("junit:junit:4.12")
}

application {
    mainClassName = "proofcompiler.Main"
}

tasks.generateGrammarSource {
    arguments = listOf("-package", "proofcompiler.parser", "-Xexact-output-dir")
}
