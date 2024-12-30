package io.additionalbeans.redis;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { "additional.redis.prefixes=foo,bar", "spring.data.redis.database=1",
		"foo.data.redis.database=2", "bar.data.redis.database=3", "bar.data.redis.client-type=jedis" })
@SpringJUnitConfig
@Testcontainers
@ImportAutoConfiguration({ RedisAutoConfiguration.class, AdditionalRedisAutoConfiguration.class })
class AdditionalRedisAutoConfigurationIntegrationTests {

	@Container
	static final GenericContainer<?> container = new GenericContainer<>("redis").withExposedPorts(6379);

	@DynamicPropertySource
	static void registerDynamicProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", container::getHost);
		registry.add("spring.data.redis.port", container::getFirstMappedPort);
	}

	@Autowired
	private RedisProperties redisProperties;

	@Autowired
	@Qualifier("fooRedisProperties")
	private RedisProperties fooRedisProperties;

	@Autowired
	@Qualifier("barRedisProperties")
	private RedisProperties barRedisProperties;

	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	@Autowired
	@Qualifier("fooRedisTemplate")
	private RedisTemplate<Object, Object> fooRedisTemplate;

	@Autowired
	@Qualifier("barRedisTemplate")
	private RedisTemplate<Object, Object> barRedisTemplate;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
	@Qualifier("fooStringRedisTemplate")
	private StringRedisTemplate fooStringRedisTemplate;

	@Autowired
	@Qualifier("barStringRedisTemplate")
	private StringRedisTemplate barStringRedisTemplate;

	@Test
	void testRedisProperties() {
		assertThat(this.fooRedisProperties.getHost()).isEqualTo(this.redisProperties.getHost());
		assertThat(this.fooRedisProperties.getPort()).isEqualTo(this.redisProperties.getPort());
		assertThat(this.barRedisProperties.getHost()).isEqualTo(this.redisProperties.getHost());
		assertThat(this.barRedisProperties.getPort()).isEqualTo(this.redisProperties.getPort());
		assertThat(this.redisProperties.getDatabase()).isEqualTo(1);
		assertThat(this.fooRedisProperties.getDatabase()).isEqualTo(2);
		assertThat(this.barRedisProperties.getDatabase()).isEqualTo(3);
	}

	@Test
	void testRedisTemplate() {
		assertThat(this.redisTemplate).isNotSameAs(this.fooRedisTemplate);
		String key = "test";
		ValueOperations<Object, Object> ops = this.redisTemplate.opsForValue();
		ValueOperations<Object, Object> fooOps = this.fooRedisTemplate.opsForValue();
		ValueOperations<Object, Object> barOps = this.barRedisTemplate.opsForValue();
		ops.set(key, "redisTemplate");
		fooOps.set(key, "fooRedisTemplate");
		barOps.set(key, "barRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(fooOps.get(key));
		assertThat(ops.get(key)).isNotEqualTo(barOps.get(key));
		assertThat(fooOps.get(key)).isNotEqualTo(barOps.get(key));
		this.redisTemplate.delete(key);
		this.fooRedisTemplate.delete(key);
		this.barRedisTemplate.delete(key);
	}

	@Test
	void testStringRedisTemplate() {
		assertThat(this.stringRedisTemplate).isNotSameAs(this.fooStringRedisTemplate);
		String key = "test";
		ValueOperations<String, String> ops = this.stringRedisTemplate.opsForValue();
		ValueOperations<String, String> fooOps = this.fooStringRedisTemplate.opsForValue();
		ValueOperations<String, String> barOps = this.barStringRedisTemplate.opsForValue();
		ops.set(key, "stringRedisTemplate");
		fooOps.set(key, "fooStringRedisTemplate");
		barOps.set(key, "barStringRedisTemplate");
		assertThat(ops.get(key)).isNotEqualTo(fooOps.get(key));
		assertThat(ops.get(key)).isNotEqualTo(barOps.get(key));
		assertThat(fooOps.get(key)).isNotEqualTo(barOps.get(key));
		this.stringRedisTemplate.delete(key);
		this.fooStringRedisTemplate.delete(key);
		this.barStringRedisTemplate.delete(key);
	}

}
