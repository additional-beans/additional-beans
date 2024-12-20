package io.additionalbeans.redis;

import io.lettuce.core.resource.DefaultClientResources;
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
class LettuceConfigurationTests {

	@Test
	void test() {
		RedisProperties properties = new RedisProperties();
		RedisConnectionDetails connectionDetails = RedisConfigurationSupport.createRedisConnectionDetails(properties);
		LettuceConfiguration lettuceConfigurationSupport = new LettuceConfiguration(properties,
				mock(ObjectProvider.class), mock(ObjectProvider.class), mock(ObjectProvider.class), connectionDetails,
				mock(ObjectProvider.class));
		DefaultClientResources clientResources = lettuceConfigurationSupport
			.lettuceClientResources(mock(ObjectProvider.class));
		RedisConnectionFactory connectionFactory = lettuceConfigurationSupport
			.redisConnectionFactory(mock(ObjectProvider.class), mock(ObjectProvider.class), clientResources);
		lettuceConfigurationSupport.redisTemplate(connectionFactory);
		lettuceConfigurationSupport.stringRedisTemplate(connectionFactory);
	}

}
