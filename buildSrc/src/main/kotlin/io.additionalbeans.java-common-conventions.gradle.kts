plugins {
	java
	checkstyle
	id("io.spring.dependency-management")
	id("io.spring.javaformat")
	id("com.netflix.nebula.archrules.runner")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	val repoUrlPrefix: String? by rootProject
	if (repoUrlPrefix != null) {
		maven {
			url = uri("${repoUrlPrefix}/maven-public/")
			isAllowInsecureProtocol = true
		}
	} else {
		mavenCentral()
		maven {
			url = uri("https://repo.spring.io/milestone")
		}
	}
}

dependencyManagement {
	imports {
		mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
	}
	generatedPomCustomization {
		enabled(false)
	}
}

val integrationTest by sourceSets.creating {
	compileClasspath += sourceSets.test.get().output
}
val integrationTestImplementation by configurations.getting {
	extendsFrom(configurations.testImplementation.get())
}
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())
val integrationTestTask = tasks.register<Test>(integrationTest.name) {
	description = "Runs integration tests."
	group = tasks.test.get().group
	testClassesDirs = integrationTest.output.classesDirs
	classpath = configurations[integrationTest.runtimeClasspathConfigurationName] + sourceSets.test.get().output + integrationTest.output
	shouldRunAfter(tasks.test)
}
val integration: String? by rootProject
if (integration != null) {
	tasks.check {
		dependsOn(integrationTestTask)
	}
}

val mockitoAgent by configurations.creating

dependencies {
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	integrationTestImplementation(project)
	integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
	integrationTestImplementation("org.testcontainers:testcontainers-junit-jupiter")
	archRules(project(":additional-beans-archrules"))
	checkstyle("""io.spring.javaformat:spring-javaformat-checkstyle:${property("javaformat-plugin.version")}""")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

archRules {
	consoleReportEnabled = false
	skipPassingSummaries = true
	failureThreshold("MEDIUM")
}

java {
	withSourcesJar()
}

tasks.jar {
	manifest {
		attributes(
			mapOf(
				"Implementation-Title" to name,
				"Implementation-Version" to version,
				"Automatic-Module-Name" to name.replace("-", ".")  // for Jigsaw
			)
		)
	}
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.compilerArgs.add("-parameters")
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs(listOf("-javaagent:${mockitoAgent.asPath}", "-Xshare:off"))
}

tasks.register("checkstyle") {
	description = "Run Checkstyle analysis for all classes"
	sourceSets.map { "checkstyle" + it.name.replaceFirstChar(Char::titlecase) }.forEach(::dependsOn)
}
