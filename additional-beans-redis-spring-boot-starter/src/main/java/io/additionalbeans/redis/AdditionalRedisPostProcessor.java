package io.additionalbeans.redis;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.data.redis.autoconfigure.RedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.RedisConnectionDetails;
import org.springframework.boot.data.redis.autoconfigure.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Yanming Zhou
 */
public class AdditionalRedisPostProcessor
		extends AdditionalBeansPostProcessor<RedisProperties, RedisConnectionDetails> {

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		registerRedisConnectionFactory(registry, prefix);
		registerRedisTemplate(registry, prefix);
	}

	private void registerRedisConnectionFactory(BeanDefinitionRegistry registry, String prefix) {
		boolean useJedis = useJedisFor(prefix);

		String connectionConfigurationClassName = useJedis ? "JedisConnectionConfiguration"
				: "LettuceConnectionConfiguration";

		String connectionConfigurationBeanName = registerBeanDefinition(registry,
				RedisAutoConfiguration.class.getPackageName() + '.' + connectionConfigurationClassName, prefix);

		if (!useJedis) {
			registerBeanDefinition(registry, DefaultClientResources.class, prefix, connectionConfigurationBeanName,
					"lettuceClientResources");
		}
		registerBeanDefinition(registry, RedisConnectionFactory.class, prefix, connectionConfigurationBeanName,
				"redisConnectionFactory");
	}

	private void registerRedisTemplate(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry, RedisAutoConfiguration.class, prefix);
		registerBeanDefinition(registry, RedisTemplate.class, prefix, configurationBeanName, "redisTemplate");
		registerBeanDefinition(registry, StringRedisTemplate.class, prefix, configurationBeanName,
				"stringRedisTemplate");
	}

	private boolean useJedisFor(String prefix) {
		String suffix = ".client-type";
		return "jedis".equalsIgnoreCase(this.environment.getProperty(
				this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + suffix,
				this.environment.getProperty(this.defaultConfigurationPropertiesPrefix + suffix)));
	}

}
