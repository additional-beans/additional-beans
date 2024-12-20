@file:Suppress("UnstableApiUsage")

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
	val repoUrlPrefix: String? by project
	if (repoUrlPrefix != null) {
		repositories {
			val version: String by project
			val repoUser: String? by project
			val repoPassword: String? by project
			maven {
				url = if (version.endsWith("-SNAPSHOT")) {
					uri("${repoUrlPrefix}/maven-snapshots/")
				} else {
					uri("${repoUrlPrefix}/maven-releases/")
				}
				isAllowInsecureProtocol = true
				credentials {
					username = repoUser
					password = repoPassword
				}
			}
		}
	}
}
