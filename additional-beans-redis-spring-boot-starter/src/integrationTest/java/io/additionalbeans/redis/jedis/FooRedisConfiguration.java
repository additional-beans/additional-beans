package io.additionalbeans.redis.jedis;

import io.additionalbeans.redis.JedisConfiguration;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
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
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Yanming Zhou
 */
@Configuration(proxyBeanMethods = false)
public class FooRedisConfiguration extends JedisConfiguration {

	public static final String PREFIX = "foo.data.redis";

	@Bean(defaultCandidate = false)
	public static RedisConnectionDetails fooRedisConnectionDetails(
			@Qualifier("fooRedisProperties") RedisProperties redisProperties) {
		return createRedisConnectionDetails(redisProperties);
	}

	@ConfigurationProperties(PREFIX)
	@Bean(defaultCandidate = false)
	public static RedisProperties fooRedisProperties(RedisProperties redisProperties) {
		RedisProperties fooRedisProperties = new RedisProperties();
		// inherit from "spring.data.redis" prefix
		BeanUtils.copyProperties(redisProperties, fooRedisProperties);
		return fooRedisProperties;
	}

	FooRedisConfiguration(@Qualifier("fooRedisProperties") RedisProperties redisProperties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
			ObjectProvider<RedisClusterConfiguration> clusterConfiguration,
			@Qualifier("fooRedisConnectionDetails") RedisConnectionDetails redisConnectionDetails,
			ObjectProvider<SslBundles> sslBundles) {
		super(redisProperties, standaloneConfigurationProvider, sentinelConfiguration, clusterConfiguration,
				redisConnectionDetails, sslBundles);
	}

	@Bean(defaultCandidate = false)
	public JedisConnectionFactory fooRedisConnectionFactory(
			ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		return super.redisConnectionFactory(builderCustomizers);
	}

	@Bean(defaultCandidate = false)
	public RedisTemplate<Object, Object> fooRedisTemplate(
			@Qualifier("fooRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.redisTemplate(redisConnectionFactory);
	}

	@Bean(defaultCandidate = false)
	public StringRedisTemplate fooStringRedisTemplate(
			@Qualifier("fooRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory) {
		return super.stringRedisTemplate(redisConnectionFactory);
	}

}
