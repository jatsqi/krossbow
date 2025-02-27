import org.hildan.github.changelog.builder.DEFAULT_EXCLUDED_LABELS

plugins {
    val kotlinVersion = "1.4.31"
    kotlin("jvm") version kotlinVersion apply false
    kotlin("js") version kotlinVersion apply false
    kotlin("multiplatform") version kotlinVersion apply false
    kotlin("plugin.spring") version kotlinVersion apply false
    kotlin("plugin.serialization") version kotlinVersion apply false
    id("org.jetbrains.dokka") version "1.4.30" apply false
    id("org.hildan.github.changelog") version "1.6.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.2.3"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
}

allprojects {
    group = "org.hildan.krossbow"

    repositories {
        jcenter()
    }

    apply(plugin = "org.jetbrains.dokka")

    afterEvaluate {
        // suppressing Dokka generation for JS because of the ZipException on NPM dependencies
        // https://github.com/Kotlin/dokka/issues/537
        tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
            dokkaSourceSets.findByName("jsMain")?.suppress?.set(true)
            dokkaSourceSets.findByName("jsTest")?.suppress?.set(true)
        }
    }
}

val Project.githubUser: String? get() = findProperty("githubUser") as String? ?: System.getenv("GITHUB_USER")
val githubSlug = "$githubUser/${rootProject.name}"
val githubRepoUrl = "https://github.com/$githubSlug"

changelog {
    githubUser = project.githubUser
    futureVersionTag = project.version.toString()
    excludeLabels = listOf("internal") + DEFAULT_EXCLUDED_LABELS
    customTagByIssueNumber = mapOf(6 to "0.1.1", 10 to "0.1.2", 15 to "0.4.0")
}

nexusPublishing {
    packageGroup.set("org.hildan")
    repositories {
        sonatype()
    }
    transitionCheckOptions {
        maxRetries.set(90) // sometimes Sonatype takes more than 10min...
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    val compilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>> {
        kotlinOptions.freeCompilerArgs += compilerArgs
    }

    tasks.withType<AbstractTestTask> {
        testLogging {
            events("failed", "standardOut", "standardError")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStackTraces = true
        }
    }

    val dokkaJar by tasks.creating(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks.findByName("dokkaHtml"))
    }

    afterEvaluate {
        val publications = extensions.getByType<PublishingExtension>().publications
        publications.filterIsInstance<MavenPublication>().forEach { pub ->
            pub.artifact(dokkaJar)
            pub.configurePomForMavenCentral(project)
        }

        signing {
            val signingKey: String? by project
            val signingPassword: String? by project
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(publishing.publications)
        }

        tasks["assemble"].dependsOn(tasks["dokkaHtml"])
    }
}

fun MavenPublication.configurePomForMavenCentral(project: Project) = pom {
    name.set(project.name)
    description.set(project.description)
    url.set(githubRepoUrl)
    licenses {
        license {
            name.set("The MIT License")
            url.set("https://opensource.org/licenses/MIT")
        }
    }
    developers {
        developer {
            id.set("joffrey-bion")
            name.set("Joffrey Bion")
            email.set("joffrey.bion@gmail.com")
        }
    }
    scm {
        connection.set("scm:git:$githubRepoUrl.git")
        developerConnection.set("scm:git:git@github.com:$githubSlug.git")
        url.set(githubRepoUrl)
    }
}
