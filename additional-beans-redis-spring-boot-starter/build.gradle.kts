@file:Suppress("UnstableApiUsage")

plugins {
	id("io.additionalbeans.java-library-conventions")
}

dependencies {
	api("org.springframework.boot:spring-boot-starter-data-redis")
	testImplementation("redis.clients:jedis")
}

testing {
	suites {
		val test by getting(JvmTestSuite::class)
		val integrationTest by registering(JvmTestSuite::class) {
			sources {
				compileClasspath += sourceSets.test.get().output
				runtimeClasspath += sourceSets.test.get().output
			}
			dependencies {
				implementation(project())
				configurations.implementation {
					dependencies.forEach { implementation(it) }
				}
				configurations.testImplementation {
					dependencies.forEach { implementation(it) }
				}
				configurations.testRuntimeOnly {
					dependencies.forEach { runtimeOnly(it) }
				}
				implementation("org.springframework.boot:spring-boot-testcontainers")
				implementation("org.testcontainers:junit-jupiter")
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
