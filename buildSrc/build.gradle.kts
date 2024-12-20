import java.util.Properties
import java.io.FileInputStream

plugins {
	java
	`kotlin-dsl`
}

ext {
	FileInputStream("$rootDir/../gradle.properties").use {
		Properties().apply { load(it) }.forEach {
			val key = it.key as String
			if (!has(key))
				set(key, it.value)
		}
	}
}

repositories {
	val repoUrlPrefix: String? by project
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

dependencies {
	implementation("""io.spring.gradle:dependency-management-plugin:${property("dependency-management-plugin.version")}""")
	implementation("""io.spring.javaformat:spring-javaformat-gradle-plugin:${property("javaformat-plugin.version")}""")
	implementation("""com.societegenerale.commons:arch-unit-gradle-plugin:${property("arch-unit-gradle-plugin.version")}""")
	implementation("""com.societegenerale.commons:arch-unit-build-plugin-core:${property("arch-unit-build-plugin-core.version")}""")
	implementation("""org.springframework.boot:org.springframework.boot.gradle.plugin:${property("spring-boot.version")}""")
}
