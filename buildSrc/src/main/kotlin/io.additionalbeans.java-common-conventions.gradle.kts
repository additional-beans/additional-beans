@file:Suppress("UnstableApiUsage")

plugins {
	java
	checkstyle
	id("io.spring.dependency-management")
	id("io.spring.javaformat")
	id("com.societegenerale.commons.arch-unit-gradle-plugin")
}

java.sourceCompatibility = JavaVersion.VERSION_21

archUnit {
	if (project.name.endsWith("-bom")) {
		isSkip = true
	}
	preConfiguredRules = listOf(
		"com.societegenerale.commons.plugin.rules.NoInjectedFieldTest",
		"com.societegenerale.commons.plugin.rules.NoTestIgnoreWithoutCommentRuleTest",
		"com.societegenerale.commons.plugin.rules.NoPrefixForInterfacesRuleTest",
		"com.societegenerale.commons.plugin.rules.NoPowerMockRuleTest",
		"com.societegenerale.commons.plugin.rules.NoJodaTimeRuleTest",
		"com.societegenerale.commons.plugin.rules.NoJunitAssertRuleTest",
		"com.societegenerale.commons.plugin.rules.StringFieldsThatAreActuallyDatesRuleTest",
		"io.additionalbeans.build.architecture.ArchitectureRuleTest"
	)
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

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	checkstyle("""io.spring.javaformat:spring-javaformat-checkstyle:${property("javaformat-plugin.version")}""")
	mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

testing {
	suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter()
		}
		val integrationTest by registering(JvmTestSuite::class) {
			sources {
				compileClasspath += sourceSets.test.get().output
				runtimeClasspath += sourceSets.test.get().output
			}
			targets {
				all {
					testTask.configure {
						shouldRunAfter(test)
					}
				}
			}
		}

		val integration: String? by rootProject
		if (integration != null) {
			val check by tasks.existing
			check.get().dependsOn(integrationTest)
		}
	}
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
	jvmArgs(listOf("-javaagent:${mockitoAgent.asPath}", "-Xshare:off"))
}

tasks.register("checkstyle") {
	description = "Run Checkstyle analysis for all classes"
	sourceSets.map { "checkstyle" + it.name.replaceFirstChar(Char::titlecase) }.forEach(::dependsOn)
}