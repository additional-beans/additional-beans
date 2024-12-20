package io.additionalbeans.redis;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;

/**
 * @author Yanming Zhou
 */
@SuppressWarnings("unchecked")
class JedisConfigurationTests {

	@Test
	void test() {
		RedisProperties properties = new RedisProperties();
		RedisConnectionDetails connectionDetails = RedisConfigurationSupport.createRedisConnectionDetails(properties);
		JedisConfiguration jedisConfigurationSupport = new JedisConfiguration(properties, mock(ObjectProvider.class),
				mock(ObjectProvider.class), mock(ObjectProvider.class), connectionDetails, mock(ObjectProvider.class));
		RedisConnectionFactory connectionFactory = jedisConfigurationSupport
			.redisConnectionFactory(mock(ObjectProvider.class));
		jedisConfigurationSupport.redisTemplate(connectionFactory);
		jedisConfigurationSupport.stringRedisTemplate(connectionFactory);
	}

}
