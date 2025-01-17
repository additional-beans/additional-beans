plugins {
	id("io.additionalbeans.java-library-conventions")
}

dependencies {
	implementation(project(":additional-beans-commons"))
	implementation("org.springframework.boot:spring-boot-starter-amqp")
}
