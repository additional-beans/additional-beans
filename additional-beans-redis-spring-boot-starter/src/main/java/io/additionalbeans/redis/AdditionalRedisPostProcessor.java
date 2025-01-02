package io.additionalbeans.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ClassUtils;

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

		if (registry.containsBeanDefinition(beanNameFor(RedisConnectionFactory.class, prefix))) {
			return;
		}
		boolean useJedis = useJedisFor(prefix);

		String connectionConfigurationClassName = useJedis ? "JedisConnectionConfiguration"
				: "LettuceConnectionConfiguration";

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setInstanceSupplier(() -> {
			try {
				Class<?> configurationClass = ClassUtils.forName(
						RedisAutoConfiguration.class.getPackageName() + '.' + connectionConfigurationClassName,
						RedisAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = configurationClass.getDeclaredConstructors()[0];
				ctor.setAccessible(true);
				return ctor.newInstance(beanFor(RedisProperties.class, prefix),
						beanProviderOf(RedisStandaloneConfiguration.class),
						beanProviderOf(RedisSentinelConfiguration.class),
						beanProviderOf(RedisClusterConfiguration.class), beanFor(RedisConnectionDetails.class, prefix),
						beanProviderOf(SslBundles.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		String connectionConfigurationBeanName = prefix + connectionConfigurationClassName;
		registry.registerBeanDefinition(connectionConfigurationBeanName, beanDefinition);

		if (!useJedis) {
			registerBeanDefinition(registry, DefaultClientResources.class, prefix, () -> {
				RootBeanDefinition bd = new RootBeanDefinition();
				bd.setDestroyMethodName("shutdown");
				bd.setInstanceSupplier(() -> {
					Object connectionConfiguration = this.applicationContext.getBean(connectionConfigurationBeanName);
					try {
						Method method = connectionConfiguration.getClass()
							.getDeclaredMethod("lettuceClientResources", ObjectProvider.class);
						method.setAccessible(true);
						return method.invoke(connectionConfiguration,
								beanProviderOf(ClientResourcesBuilderCustomizer.class));
					}
					catch (Exception ex) {
						throw new RuntimeException(ex);
					}

				});
				return bd;
			});
		}

		registerBeanInstanceSupplier(registry, RedisConnectionFactory.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(connectionConfigurationBeanName);
			try {
				if (useJedis) {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class);
					method.setAccessible(true);
					return (RedisConnectionFactory) method.invoke(connectionConfiguration,
							beanProviderOf(JedisClientConfigurationBuilderCustomizer.class));
				}
				else {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class, ObjectProvider.class,
								ClientResources.class);
					method.setAccessible(true);
					return (RedisConnectionFactory) method.invoke(connectionConfiguration,
							beanProviderOf(LettuceClientConfigurationBuilderCustomizer.class),
							beanProviderOf(LettuceClientOptionsBuilderCustomizer.class),
							beanFor(ClientResources.class, prefix));
				}

			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerRedisTemplate(BeanDefinitionRegistry registry, String prefix) {

		RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();

		registerBeanInstanceSupplier(registry, RedisTemplate.class, prefix,
				() -> redisAutoConfiguration.redisTemplate(beanFor(RedisConnectionFactory.class, prefix)));

		registerBeanInstanceSupplier(registry, StringRedisTemplate.class, prefix,
				() -> redisAutoConfiguration.stringRedisTemplate(beanFor(RedisConnectionFactory.class, prefix)));
	}

	private boolean useJedisFor(String prefix) {
		String suffix = ".client-type";
		return "jedis".equalsIgnoreCase(this.environment.getProperty(
				this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + suffix,
				this.environment.getProperty(this.defaultConfigurationPropertiesPrefix + suffix)));
	}

}
