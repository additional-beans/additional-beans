plugins {
	id("io.additionalbeans.java-library-conventions")
}

dependencies {
	api("org.springframework.boot:spring-boot-starter-data-redis")
	testImplementation("redis.clients:jedis")
}
