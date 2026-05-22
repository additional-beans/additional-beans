plugins {
	id("io.additionalbeans.java-common-conventions")
	id("com.netflix.nebula.archrules.library")
}

tasks.named("checkstyleArchRules") {
	dependsOn("generateServicesRegistry")
}
