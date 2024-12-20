pluginManagement {
	repositories {
		val repoUrlPrefix: String? by settings
		if (repoUrlPrefix != null) {
			maven {
				url = uri("${repoUrlPrefix}/maven-public/")
				isAllowInsecureProtocol = true
			}
			maven {
				url = uri("${repoUrlPrefix}/gradle-plugins/")
				isAllowInsecureProtocol = true
			}
		} else {
			gradlePluginPortal()
			mavenCentral()
			maven {
				url = uri("https://repo.spring.io/milestone")
			}
		}
	}
}

rootProject.name = "additional-beans"
rootDir.listFiles(File::isDirectory)?.map(File::getName)?.filter { it.startsWith(rootProject.name) } ?.forEach(::include)
