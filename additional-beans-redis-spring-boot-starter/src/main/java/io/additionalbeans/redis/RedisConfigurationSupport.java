package io.additionalbeans.redis;

import java.lang.reflect.Constructor;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public abstract class RedisConfigurationSupport {

	private static final RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();

	protected RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return redisAutoConfiguration.redisTemplate(redisConnectionFactory);
	}

	protected StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		return redisAutoConfiguration.stringRedisTemplate(redisConnectionFactory);
	}

	public static RedisConnectionDetails createRedisConnectionDetails(RedisProperties properties) {
		try {
			Constructor<?> ctor = ClassUtils
				.forName(RedisProperties.class.getPackageName() + ".PropertiesRedisConnectionDetails",
						RedisProperties.class.getClassLoader())
				.getDeclaredConstructor(RedisProperties.class);
			ctor.setAccessible(true);
			return (RedisConnectionDetails) ctor.newInstance(properties);
		}
		catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	static String getCurrentMethodName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

}
