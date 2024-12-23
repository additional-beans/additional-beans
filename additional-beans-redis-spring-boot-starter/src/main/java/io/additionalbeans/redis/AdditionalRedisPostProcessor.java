package io.additionalbeans.redis;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientOptionsBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
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
		implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor, ApplicationContextAware, InitializingBean {

	public static final String KEY_ADDITIONAL_REDIS_PREFIXES = "additional.redis.prefixes";

	private static final String SPRING_DATA_REDIS_PREFIX = "spring.data.redis";

	private ApplicationContext applicationContext;

	private Environment environment;

	private Binder binder;

	private List<String> names = Collections.emptyList();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() {
		this.environment = this.applicationContext.getEnvironment();
		this.binder = Binder.get(this.environment);
		try {
			this.names = this.binder.bindOrCreate(KEY_ADDITIONAL_REDIS_PREFIXES, List.class);
		}
		catch (BindException ignored) {
		}
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (String prefix : this.names) {
			if (!registry.containsBeanDefinition(beanNameFor(RedisProperties.class, prefix))) {
				registerRedisProperties(registry, prefix);
				registerRedisConnectionFactory(registry, prefix);
				registerRedisTemplate(registry, prefix);
			}
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof RedisProperties) {
			String suffix = RedisProperties.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.names.contains(prefix)) {
					this.binder.bind(SPRING_DATA_REDIS_PREFIX.replace("spring", prefix), Bindable.ofInstance(bean));
				}
			}
		}
		return bean;
	}

	private void registerRedisProperties(BeanDefinitionRegistry registry, String prefix) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setTargetType(RedisProperties.class);
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setInstanceSupplier(() -> {
			RedisProperties properties = new RedisProperties();
			RedisProperties defaultRedisProperties = this.applicationContext.getBean(
					"%s-%s".formatted(SPRING_DATA_REDIS_PREFIX, RedisProperties.class.getName()),
					RedisProperties.class);
			BeanUtils.copyProperties(defaultRedisProperties, properties);
			return properties;
		});
		registry.registerBeanDefinition(beanNameFor(RedisProperties.class, prefix), beanDefinition);

		beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setBeanClassName(RedisProperties.class.getPackageName() + ".PropertiesRedisConnectionDetails");
		ConstructorArgumentValues arguments = new ConstructorArgumentValues();
		arguments.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(RedisProperties.class, prefix)));
		beanDefinition.setConstructorArgumentValues(arguments);
		beanDefinition.setTargetType(RedisConnectionDetails.class);
		registry.registerBeanDefinition(beanNameFor(RedisConnectionDetails.class, prefix), beanDefinition);
	}

	private void registerRedisConnectionFactory(BeanDefinitionRegistry registry, String prefix) {

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
				return ctor.newInstance(
						this.applicationContext.getBean(prefix + RedisProperties.class.getSimpleName(),
								RedisProperties.class),
						this.applicationContext.getBeanProvider(RedisStandaloneConfiguration.class),
						this.applicationContext.getBeanProvider(RedisSentinelConfiguration.class),
						this.applicationContext.getBeanProvider(RedisClusterConfiguration.class),
						this.applicationContext.getBean(beanNameFor(RedisConnectionDetails.class, prefix),
								RedisConnectionDetails.class),
						this.applicationContext.getBeanProvider(SslBundles.class));
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
							this.applicationContext.getBeanProvider(ClientResourcesBuilderCustomizer.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			registry.registerBeanDefinition(beanNameFor(ClientResources.class, prefix), beanDefinition);
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
							this.applicationContext.getBeanProvider(JedisClientConfigurationBuilderCustomizer.class));
				}
				else {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("redisConnectionFactory", ObjectProvider.class, ObjectProvider.class,
								ClientResources.class);
					method.setAccessible(true);
					return method.invoke(connectionConfiguration,
							this.applicationContext.getBeanProvider(LettuceClientConfigurationBuilderCustomizer.class),
							this.applicationContext.getBeanProvider(LettuceClientOptionsBuilderCustomizer.class),
							this.applicationContext.getBean(beanNameFor(ClientResources.class, prefix),
									ClientResources.class));
				}

			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		registry.registerBeanDefinition(beanNameFor(RedisConnectionFactory.class, prefix), beanDefinition);
	}

	private void registerRedisTemplate(BeanDefinitionRegistry registry, String prefix) {

		RedisAutoConfiguration redisAutoConfiguration = new RedisAutoConfiguration();

		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setTargetType(RedisTemplate.class);
		beanDefinition.setInstanceSupplier(() -> redisAutoConfiguration.redisTemplate(this.applicationContext
			.getBean(prefix + RedisConnectionFactory.class.getSimpleName(), RedisConnectionFactory.class)));
		registry.registerBeanDefinition(beanNameFor(RedisTemplate.class, prefix), beanDefinition);

		beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setTargetType(StringRedisTemplate.class);
		beanDefinition.setInstanceSupplier(() -> redisAutoConfiguration.stringRedisTemplate(this.applicationContext
			.getBean(prefix + RedisConnectionFactory.class.getSimpleName(), RedisConnectionFactory.class)));
		registry.registerBeanDefinition(beanNameFor(StringRedisTemplate.class, prefix), beanDefinition);
	}

	private boolean useJedisFor(String prefix) {
		String suffix = ".client-type";
		return "jedis"
			.equalsIgnoreCase(this.environment.getProperty(SPRING_DATA_REDIS_PREFIX.replace("spring", prefix) + suffix,
					this.environment.getProperty(SPRING_DATA_REDIS_PREFIX + suffix)));
	}

	private static String beanNameFor(Class<?> beanClass, String prefix) {
		if (beanClass.getName().startsWith("org.springframework")) {
			return prefix + beanClass.getSimpleName();
		}
		else {
			return "%s-%s".formatted(prefix, beanClass.getName());
		}
	}

}
