package io.additionalbeans.redis;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalRedisAutoConfigurationTests {

	private static final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(RedisAutoConfiguration.class, AdditionalRedisAutoConfiguration.class))
		.withPropertyValues("additional.redis.prefixes=foo,bar", "spring.data.redis.database=1",
				"spring.data.redis.username=default", "foo.data.redis.database=2", "foo.data.redis.username=foo",
				"bar.data.redis.database=3", "bar.data.redis.username=bar", "bar.data.redis.client-type=jedis");

	@Test
	void testRedisProperties() {
		runner.run((ctx) -> {
			RedisProperties redisProperties = ctx.getBean(RedisProperties.class);
			RedisProperties fooRedisProperties = ctx.getBean("fooRedisProperties", RedisProperties.class);
			RedisProperties barRedisProperties = ctx.getBean("barRedisProperties", RedisProperties.class);
			assertThat(fooRedisProperties.getHost()).isEqualTo(redisProperties.getHost());
			assertThat(fooRedisProperties.getPort()).isEqualTo(redisProperties.getPort());
			assertThat(barRedisProperties.getHost()).isEqualTo(redisProperties.getHost());
			assertThat(barRedisProperties.getPort()).isEqualTo(redisProperties.getPort());
			assertThat(redisProperties.getDatabase()).isEqualTo(1);
			assertThat(fooRedisProperties.getDatabase()).isEqualTo(2);
			assertThat(barRedisProperties.getDatabase()).isEqualTo(3);
		});
	}

	@Test
	void testRedisConnectionDetails() {
		runner.run((ctx) -> {
			RedisConnectionDetails redisProperties = ctx.getBean(RedisConnectionDetails.class);
			RedisProperties fooRedisProperties = ctx.getBean("fooRedisProperties", RedisProperties.class);
			RedisProperties barRedisProperties = ctx.getBean("barRedisProperties", RedisProperties.class);
			RedisConnectionDetails redisConnectionDetails = ctx.getBean(RedisConnectionDetails.class);
			RedisConnectionDetails fooRedisConnectionDetails = ctx.getBean("fooRedisConnectionDetails",
					RedisConnectionDetails.class);
			RedisConnectionDetails barRedisConnectionDetails = ctx.getBean("barRedisConnectionDetails",
					RedisConnectionDetails.class);
			assertThat(redisConnectionDetails.getUsername()).isEqualTo(redisProperties.getUsername());
			assertThat(fooRedisConnectionDetails.getUsername()).isEqualTo(fooRedisProperties.getUsername());
			assertThat(barRedisConnectionDetails.getUsername()).isEqualTo(barRedisProperties.getUsername());
		});
	}

	@Test
	void testRedisConnectionFactory() {
		runner.run((ctx) -> {
			RedisConnectionFactory redisConnectionFactory = ctx.getBean(RedisConnectionFactory.class);
			RedisConnectionFactory fooRedisConnectionFactory = ctx.getBean("fooRedisConnectionFactory",
					RedisConnectionFactory.class);
			RedisConnectionFactory barRedisConnectionFactory = ctx.getBean("barRedisConnectionFactory",
					RedisConnectionFactory.class);
			assertThat(redisConnectionFactory).isNotSameAs(fooRedisConnectionFactory);
			assertThat(redisConnectionFactory).isInstanceOf(LettuceConnectionFactory.class);
			assertThat(fooRedisConnectionFactory).isInstanceOf(LettuceConnectionFactory.class);
			assertThat(barRedisConnectionFactory).isInstanceOf(JedisConnectionFactory.class);
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	void testRedisTemplate() {
		runner.run((ctx) -> {
			RedisTemplate<Object, Object> redisTemplate = (RedisTemplate<Object, Object>) ctx
				.getBeanProvider(ResolvableType.forClassWithGenerics(RedisTemplate.class, Object.class, Object.class))
				.getObject();
			RedisTemplate<Object, Object> fooRedisTemplate = ctx.getBean("fooRedisTemplate", RedisTemplate.class);
			RedisTemplate<Object, Object> barRedisTemplate = ctx.getBean("barRedisTemplate", RedisTemplate.class);
			assertThat(redisTemplate).isNotSameAs(fooRedisTemplate);
			assertThat(redisTemplate).isNotSameAs(barRedisTemplate);
			assertThat(barRedisTemplate).isNotSameAs(fooRedisTemplate);

			StringRedisTemplate stringRedisTemplate = ctx.getBean(StringRedisTemplate.class);
			StringRedisTemplate fooStringRedisTemplate = ctx.getBean("fooStringRedisTemplate",
					StringRedisTemplate.class);
			StringRedisTemplate barStringRedisTemplate = ctx.getBean("barStringRedisTemplate",
					StringRedisTemplate.class);
			assertThat(stringRedisTemplate).isNotSameAs(fooStringRedisTemplate);
			assertThat(stringRedisTemplate).isNotSameAs(barStringRedisTemplate);
			assertThat(barStringRedisTemplate).isNotSameAs(fooStringRedisTemplate);
		});
	}

}
