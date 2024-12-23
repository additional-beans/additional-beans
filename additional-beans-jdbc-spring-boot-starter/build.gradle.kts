plugins {
	id("io.additionalbeans.java-library-conventions")
}

dependencies {
	api("org.springframework.boot:spring-boot-starter-jdbc")
	testImplementation("com.h2database:h2")
}
