plugins {
	id("io.additionalbeans.java-library-conventions")
}

dependencies {
	constraints {
		rootProject.subprojects.filter { !it.name.endsWith("-bom") }.forEach {
			api("${it.group}:${it.name}:${it.version}")
		}
	}
}
