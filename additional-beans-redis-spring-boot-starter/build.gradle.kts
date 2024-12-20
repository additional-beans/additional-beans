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
		val integrationTest by getting(JvmTestSuite::class)  {
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
		}
	}
}
