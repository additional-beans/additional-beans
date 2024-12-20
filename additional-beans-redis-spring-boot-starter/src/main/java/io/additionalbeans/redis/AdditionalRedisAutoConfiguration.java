package io.additionalbeans.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@AutoConfigureAfter(RedisAutoConfiguration.class)
public class AdditionalRedisAutoConfiguration {

	@Bean
	@Role(ROLE_INFRASTRUCTURE)
	static AdditionalRedisPostProcessor additionalRedisPostProcessor() {
		return new AdditionalRedisPostProcessor();
	}

}
