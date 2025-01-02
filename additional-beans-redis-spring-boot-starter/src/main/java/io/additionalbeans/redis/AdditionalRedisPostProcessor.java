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
	protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {
		registerRedisConnectionFactory(registry, prefix);
		registerRedisTemplate(registry, prefix);
	}

	private void registerRedisConnectionFactory(BeanDefinitionRegistry registry, String prefix) {

		if (registry.containsBeanDefinition(beanNameForPrefix(RedisConnectionFactory.class, prefix))) {
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
				return ctor.newInstance(beanForPrefix(RedisProperties.class, prefix),
						beanProviderFor(RedisStandaloneConfiguration.class),
						beanProviderFor(RedisSentinelConfiguration.class),
						beanProviderFor(RedisClusterConfiguration.class),
						beanForPrefix(RedisConnectionDetails.class, prefix), beanProviderFor(SslBundles.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		String connectionConfigurationBeanName = prefix + connectionConfigurationClassName;
		registry.registerBeanDefinition(connectionConfigurationBeanName, beanDefinition);

		if (!useJedis) {
			beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(DefaultClientResources.class);
			beanDefinition.setDestroyMethodName("shutdown");
			beanDefinition.setInstanceSupplier(() -> {
				Object connectionConfiguration = this.applicationContext.getBean(connectionConfigurationBeanName);
				try {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("lettuceClientResources", ObjectProvider.class);
					method.setAccessible(true);
					return method.invoke(connectionConfiguration,
							beanProviderFor(ClientResourcesBuilderCustomizer.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			registry.registerBeanDefinition(beanNameForPrefix(ClientResources.class, prefix), beanDefinition);
		}

		beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setTargetType(RedisConnectionFactory.class);
		beanDefinition.setInstanceSupplier(() -> {
			Object connectionConfiguration = this.applicationContext.getBean(connectionConfigurationBeanName);
			try {
				if (useJedis) {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class);
					method.setAccessible(true);
					return method.invoke(connectionConfiguration,
							beanProviderFor(JedisClientConfigurationBuilderCustomizer.class));
				}
				else {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class, ObjectProvider.class,
								ClientResources.class);
					method.setAccessible(true);
					return method.invoke(connectionConfiguration,
							beanProviderFor(LettuceClientConfigurationBuilderCustomizer.class),
							beanProviderFor(LettuceClientOptionsBuilderCustomizer.class),
							beanForPrefix(ClientResources.class, prefix));
				}

			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		registry.registerBeanDefinition(beanNameForPrefix(RedisConnectionFactory.class, prefix), beanDefinition);
	}

	private void registerRedisTemplate(BeanDefinitionRegistry registry, String prefix) {

		RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();

		registerBeanDefinition(registry, RedisTemplate.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(RedisTemplate.class);
			beanDefinition.setInstanceSupplier(
					() -> redisAutoConfiguration.redisTemplate(beanForPrefix(RedisConnectionFactory.class, prefix)));
			return beanDefinition;
		});

		registerBeanDefinition(registry, StringRedisTemplate.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(StringRedisTemplate.class);
			beanDefinition.setInstanceSupplier(() -> redisAutoConfiguration
				.stringRedisTemplate(beanForPrefix(RedisConnectionFactory.class, prefix)));
			return beanDefinition;
		});
	}

	private boolean useJedisFor(String prefix) {
		String suffix = ".client-type";
		return "jedis".equalsIgnoreCase(this.environment.getProperty(
				this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + suffix,
				this.environment.getProperty(this.defaultConfigurationPropertiesPrefix + suffix)));
	}

}
