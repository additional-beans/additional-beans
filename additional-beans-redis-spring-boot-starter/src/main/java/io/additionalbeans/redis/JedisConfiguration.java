package io.additionalbeans.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public class JedisConfiguration extends RedisConfigurationSupport {

	private final Object jedisConnectionConfiguration;

	protected JedisConfiguration(RedisProperties properties,
			ObjectProvider<RedisStandaloneConfiguration> standaloneConfigurationProvider,
			ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
			ObjectProvider<RedisClusterConfiguration> clusterConfiguration, RedisConnectionDetails connectionDetails,
			ObjectProvider<SslBundles> sslBundles) {
		try {
			Class<?> clazz = RedisAutoConfiguration.class;
			Class<?> configurationClass = ClassUtils.forName(clazz.getPackageName() + ".JedisConnectionConfiguration",
					clazz.getClassLoader());
			Constructor<?> ctor = configurationClass
				.getDeclaredConstructor(JedisConfiguration.class.getDeclaredConstructors()[0].getParameterTypes());
			ctor.setAccessible(true);
			this.jedisConnectionConfiguration = ctor.newInstance(properties, standaloneConfigurationProvider,
					sentinelConfiguration, clusterConfiguration, connectionDetails, sslBundles);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	protected JedisConnectionFactory redisConnectionFactory(
			ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers) {
		try {
			Method m = this.jedisConnectionConfiguration.getClass()
				.getDeclaredMethod(getCurrentMethodName(), ObjectProvider.class);
			m.setAccessible(true);
			return (JedisConnectionFactory) m.invoke(this.jedisConnectionConfiguration, builderCustomizers);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

}
