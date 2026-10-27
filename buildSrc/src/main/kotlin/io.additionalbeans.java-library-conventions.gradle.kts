plugins {
	id("io.additionalbeans.java-common-conventions")
	`java-library`
	`maven-publish`
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
			suppressAllPomMetadataWarnings()
		}
	}
	providers.gradleProperty("repoUrlPrefix").map {
		repositories {
			maven {
				uri("""${it}/maven-${if ((property("version") as String).endsWith("-SNAPSHOT")) "snapshots" else "releases"}/""")
				isAllowInsecureProtocol = true
				credentials {
					username = providers.gradleProperty("repoUser").orNull
					password = providers.gradleProperty("repoPassword").orNull
				}
			}
		}
	}
}
