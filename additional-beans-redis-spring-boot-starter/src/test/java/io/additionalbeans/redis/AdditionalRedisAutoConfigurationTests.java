package io.additionalbeans.redis;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { AdditionalRedisPostProcessor.KEY_ADDITIONAL_REDIS_PREFIXES + "=foo, bar",
		"spring.data.redis.database=1", "spring.data.redis.username=default", "foo.data.redis.database=2",
		"foo.data.redis.username=foo", "bar.data.redis.database=3", "bar.data.redis.username=bar",
		"bar.data.redis.client-type=jedis" })
@ImportAutoConfiguration({ RedisAutoConfiguration.class, AdditionalRedisAutoConfiguration.class })
@SpringJUnitConfig
class AdditionalRedisAutoConfigurationTests {

	@Autowired
	private RedisProperties redisProperties;

	@Autowired
	@Qualifier("fooRedisProperties")
	private RedisProperties fooRedisProperties;

	@Autowired
	@Qualifier("barRedisProperties")
	private RedisProperties barRedisProperties;

	@Autowired
	private RedisConnectionDetails redisConnectionDetails;

	@Autowired
	@Qualifier("fooRedisConnectionDetails")
	private RedisConnectionDetails fooRedisConnectionDetails;

	@Autowired
	@Qualifier("barRedisConnectionDetails")
	private RedisConnectionDetails barRedisConnectionDetails;

	@Autowired
	private RedisConnectionFactory redisConnectionFactory;

	@Autowired
	@Qualifier("fooRedisConnectionFactory")
	private RedisConnectionFactory fooRedisConnectionFactory;

	@Autowired
	@Qualifier("barRedisConnectionFactory")
	private RedisConnectionFactory barRedisConnectionFactory;

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
	void testRedisConnectionDetails() {
		assertThat(this.redisConnectionDetails.getUsername()).isEqualTo(this.redisProperties.getUsername());
		assertThat(this.fooRedisConnectionDetails.getUsername()).isEqualTo(this.fooRedisProperties.getUsername());
		assertThat(this.barRedisConnectionDetails.getUsername()).isEqualTo(this.barRedisProperties.getUsername());
	}

	@Test
	void testRedisConnectionFactory() {
		assertThat(this.redisConnectionFactory).isNotSameAs(this.fooRedisConnectionFactory);
		assertThat(this.redisConnectionFactory).isInstanceOf(LettuceConnectionFactory.class);
		assertThat(this.fooRedisConnectionFactory).isInstanceOf(LettuceConnectionFactory.class);
		assertThat(this.barRedisConnectionFactory).isInstanceOf(JedisConnectionFactory.class);
	}

	@Test
	void testRedisTemplate() {
		assertThat(this.redisTemplate).isNotSameAs(this.fooRedisTemplate);
		assertThat(this.redisTemplate).isNotSameAs(this.barRedisTemplate);
		assertThat(this.barRedisTemplate).isNotSameAs(this.fooRedisTemplate);

		assertThat(this.stringRedisTemplate).isNotSameAs(this.fooStringRedisTemplate);
		assertThat(this.stringRedisTemplate).isNotSameAs(this.barStringRedisTemplate);
		assertThat(this.barStringRedisTemplate).isNotSameAs(this.fooStringRedisTemplate);
	}

}
