package io.additionalbeans.redis.lettuce;

import io.additionalbeans.redis.LettuceConfiguration;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Yanming Zhou
 */
@Configuration(proxyBeanMethods = false)
public class BarRedisConfiguration extends LettuceConfiguration {

	public static final String PREFIX = "bar.data.redis";

	@Bean(defaultCandidate = false)
	public static RedisConnectionDetails barRedisConnectionDetails(
			@Qualifier("barRedisProperties") RedisProperties redisProperties) {
		return createRedisConnectionDetails(redisProperties);
	}

	@ConfigurationProperties(PREFIX)
	@Bean(defaultCandidate = false)
	public static RedisProperties barRedisProperties(RedisProperties redisProperties) {
		RedisProperties barRedisProperties = new RedisProperties();
		// inherit from "spring.data.redis" prefix
		BeanUtils.copyProperties(redisProperties, barRedisProperties);
		return barRedisProperties;
	}

	BarRedisConfiguration(@Qualifier("barRedisProperties") RedisProperties redisProperties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
			ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
			@Qualifier("barRedisConnectionDetails") RedisConnectionDetails redisConnectionDetails,
			ObjectProvider<SslBundles> sslBundles) {
		super(redisProperties, standaloneConfigurationProvider, sentinelConfigurationProvider,
				clusterConfigurationProvider, redisConnectionDetails, sslBundles);
	}

	@Bean(defaultCandidate = false, destroyMethod = "shutdown")
	public DefaultClientResources barLettuceClientResources(
			ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
		return super.lettuceClientResources(customizers);
	}

	@Bean(defaultCandidate = false)
	public LettuceConnectionFactory barRedisConnectionFactory(
			ObjectProvider<LettuceClientConfigurationBuilderCustomizer> clientConfigurationBuilderCustomizers,
			ObjectProvider<LettuceClientOptionsBuilderCustomizer> clientOptionsBuilderCustomizers,
			@Qualifier("barLettuceClientResources") ClientResources lettuceClientResources) {
		return super.redisConnectionFactory(clientConfigurationBuilderCustomizers, clientOptionsBuilderCustomizers,
				lettuceClientResources);
	}

	@Bean(defaultCandidate = false)
	public RedisTemplate<Object, Object> barRedisTemplate(
			@Qualifier("barRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.redisTemplate(redisConnectionFactory);
	}

	@Bean(defaultCandidate = false)
	public StringRedisTemplate barStringRedisTemplate(
			@Qualifier("barRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.stringRedisTemplate(redisConnectionFactory);
	}

}
