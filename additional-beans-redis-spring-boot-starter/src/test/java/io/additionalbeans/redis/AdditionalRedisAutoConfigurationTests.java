package io.additionalbeans.redis;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
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
		.withConfiguration(
				AutoConfigurations.of(DataRedisAutoConfiguration.class, AdditionalRedisAutoConfiguration.class))
		.withPropertyValues("additional.redis.prefixes=foo,bar", "spring.data.redis.database=1",
				"spring.data.redis.username=default", "foo.data.redis.database=2", "foo.data.redis.username=foo",
				"bar.data.redis.database=3", "bar.data.redis.client-type=jedis");

	@Test
	void testRedisProperties() {
		runner.run((ctx) -> {
			DataRedisProperties dataRedisProperties = ctx.getBean(DataRedisProperties.class);
			DataRedisProperties fooDataRedisProperties = ctx.getBean("fooDataRedisProperties",
					DataRedisProperties.class);
			DataRedisProperties barDataRedisProperties = ctx.getBean("barDataRedisProperties",
					DataRedisProperties.class);
			assertThat(fooDataRedisProperties.getHost()).isEqualTo(dataRedisProperties.getHost());
			assertThat(fooDataRedisProperties.getPort()).isEqualTo(dataRedisProperties.getPort());
			assertThat(barDataRedisProperties.getHost()).isEqualTo(dataRedisProperties.getHost());
			assertThat(barDataRedisProperties.getPort()).isEqualTo(dataRedisProperties.getPort());
			assertThat(dataRedisProperties.getDatabase()).isEqualTo(1);
			assertThat(fooDataRedisProperties.getDatabase()).isEqualTo(2);
			assertThat(barDataRedisProperties.getDatabase()).isEqualTo(3);
			assertThat(dataRedisProperties.getUsername()).isEqualTo("default");
			assertThat(fooDataRedisProperties.getUsername()).isEqualTo("foo");
			assertThat(barDataRedisProperties.getUsername()).isEqualTo(dataRedisProperties.getUsername());
		});
	}

	@Test
	void testRedisConnectionDetails() {
		runner.run((ctx) -> {
			DataRedisConnectionDetails redisProperties = ctx.getBean(DataRedisConnectionDetails.class);
			DataRedisProperties fooDataRedisProperties = ctx.getBean("fooDataRedisProperties",
					DataRedisProperties.class);
			DataRedisProperties barDataRedisProperties = ctx.getBean("barDataRedisProperties",
					DataRedisProperties.class);
			DataRedisConnectionDetails dataRedisConnectionDetails = ctx.getBean(DataRedisConnectionDetails.class);
			DataRedisConnectionDetails fooDataRedisConnectionDetails = ctx.getBean("fooDataRedisConnectionDetails",
					DataRedisConnectionDetails.class);
			DataRedisConnectionDetails barDataRedisConnectionDetails = ctx.getBean("barDataRedisConnectionDetails",
					DataRedisConnectionDetails.class);
			assertThat(dataRedisConnectionDetails.getUsername()).isEqualTo(redisProperties.getUsername());
			assertThat(fooDataRedisConnectionDetails.getUsername()).isEqualTo(fooDataRedisProperties.getUsername());
			assertThat(barDataRedisConnectionDetails.getUsername()).isEqualTo(barDataRedisProperties.getUsername());
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
