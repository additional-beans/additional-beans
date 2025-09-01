package io.additionalbeans.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public abstract class AdditionalBeansPostProcessor<CP, CD>
		implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor, ApplicationContextAware, InitializingBean {

	private static final String KEY_ADDITIONAL_PREFIXES_PATTERN = "additional.%s.prefixes";

	protected ApplicationContext applicationContext;

	protected Environment environment;

	protected Binder binder;

	protected List<String> prefixes = Collections.emptyList();

	protected final Class<CP> configurationPropertiesClass;

	protected final Class<CD> connectionDetailsClass;

	protected final String defaultConfigurationPropertiesPrefix;

	@SuppressWarnings("unchecked")
	protected AdditionalBeansPostProcessor() {
		ResolvableType resolvableType = ResolvableType.forClass(getClass()).as(AdditionalBeansPostProcessor.class);
		this.configurationPropertiesClass = (Class<CP>) resolvableType.getGeneric(0).resolve();
		this.connectionDetailsClass = (Class<CD>) resolvableType.getGeneric(1).resolve();
		Assert.notNull(this.configurationPropertiesClass, "configurationPropertiesClass shouldn't be null");
		ConfigurationProperties configurationProperties = AnnotationUtils
			.findAnnotation(this.configurationPropertiesClass, ConfigurationProperties.class);
		Assert.notNull(this.configurationPropertiesClass,
				"configurationPropertiesClass should annotated with @ConfigurationProperties");
		this.defaultConfigurationPropertiesPrefix = configurationProperties.prefix();
	}

	protected Set<Class<?>> getSharedTypes() {
		return Collections.emptySet();
	}

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
			this.prefixes = this.binder.bindOrCreate(configurationKeyForPrefixes(), List.class);
		}
		catch (BindException ignored) {
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (this.prefixes.isEmpty()) {
			return bean;
		}
		if (this.configurationPropertiesClass.isInstance(bean)) {
			String suffix = this.configurationPropertiesClass.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					this.binder.bind(this.defaultConfigurationPropertiesPrefix.replace("spring", prefix),
							Bindable.ofInstance(bean));
				}
			}
		}
		return bean;
	}

	protected String configurationKeyForPrefixes() {
		String module = getClass().getPackageName();
		module = module.substring(module.lastIndexOf('.') + 1);
		return KEY_ADDITIONAL_PREFIXES_PATTERN.formatted(module);
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (String prefix : this.prefixes) {
			registerConfigurationProperties(registry, prefix);
			registerBeanDefinitions(registry, prefix);
		}
	}

	protected void registerConfigurationProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, this.configurationPropertiesClass, prefix);
		String propertiesConnectionDetailsClassName = this.connectionDetailsClass.getPackageName() + ".Properties"
				+ this.connectionDetailsClass.getSimpleName();
		if (ClassUtils.isPresent(propertiesConnectionDetailsClassName, this.connectionDetailsClass.getClassLoader())) {
			registerBeanDefinition(registry, this.connectionDetailsClass, prefix,
					() -> buildConnectionDetailsBeanDefinition(propertiesConnectionDetailsClassName, prefix));
		}
	}

	protected abstract void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix);

	private RootBeanDefinition buildConnectionDetailsBeanDefinition(String propertiesConnectionDetailsClassName,
			String prefix) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setInstanceSupplier(() -> {
			try {
				Class<?> clazz = ClassUtils.forName(propertiesConnectionDetailsClassName,
						this.connectionDetailsClass.getClassLoader());
				return instantiateBean(clazz, prefix);
			}
			catch (ClassNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		});
		return beanDefinition;
	}

	protected String registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, String prefix) {
		String beanName = beanNameFor(beanClass, prefix);
		if (!registry.containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = new RootBeanDefinition(beanClass);
			bd.setDefaultCandidate(false);
			Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
			if (constructors.length == 1 && constructors[0].getParameterCount() > 0) {
				bd.setInstanceSupplier(() -> instantiateBean(beanClass, prefix));
			}
			registry.registerBeanDefinition(beanName, bd);
		}
		return beanName;
	}

	protected String registerBeanDefinition(BeanDefinitionRegistry registry, String beanClassName, String prefix) {
		try {
			return registerBeanDefinition(registry,
					ClassUtils.forName(beanClassName, this.configurationPropertiesClass.getClassLoader()), prefix);
		}
		catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}

	protected String registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, String prefix,
			String configurationBeanName, String factoryMethodName) {
		return this.registerBeanInstanceSupplier(registry, beanClass, prefix,
				() -> createBean(configurationBeanName, factoryMethodName, prefix));
	}

	protected <T> String registerBeanInstanceSupplier(BeanDefinitionRegistry registry, Class<T> beanClass,
			String prefix, Supplier<T> instanceSupplier) {
		return this.registerBeanDefinition(registry, beanClass, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setInstanceSupplier(instanceSupplier);
			return beanDefinition;
		});
	}

	protected CP configurationPropertiesBean(String prefix) {
		return beanFor(this.configurationPropertiesClass, prefix);
	}

	private String registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanClass, String prefix,
			Supplier<RootBeanDefinition> beanDefinitionSupplier) {
		String beanName = beanNameFor(beanClass, prefix);
		if (!registry.containsBeanDefinition(beanName)) {
			RootBeanDefinition bd = beanDefinitionSupplier.get();
			bd.setDefaultCandidate(false);
			if (bd.getTargetType() == null) {
				bd.setTargetType(beanClass);
			}
			registry.registerBeanDefinition(beanName, bd);
		}
		return beanName;
	}

	private String beanNameFor(Class<?> beanClass, String prefix) {
		String beanClassName = beanClass.getSimpleName();
		String classPrefix = "Default";
		if (beanClassName.startsWith(classPrefix)) {
			beanClassName = beanClassName.substring(classPrefix.length());
		}
		return prefix + beanClassName;
	}

	private <T> T beanFor(Class<T> beanClass, String prefix) {
		return this.applicationContext.getBean(beanNameFor(beanClass, prefix), beanClass);
	}

	private <T> ObjectProvider<T> beanProviderOf(Class<T> beanClass) {
		return this.applicationContext.getBeanProvider(beanClass);
	}

	private Object instantiateBean(Class<?> clazz, String prefix) {
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
			constructor.setAccessible(true);
			return constructor.newInstance(resolveParameters(constructor, prefix));
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T createBean(String configurationBeanName, String factoryMethodName, String prefix) {
		Object configuration = this.applicationContext.getBean(configurationBeanName);
		try {
			Method factoryMethod = Stream.of(configuration.getClass().getDeclaredMethods())
				.filter(m -> m.getName().equals(factoryMethodName))
				.findFirst()
				.orElseThrow();
			factoryMethod.setAccessible(true);
			return (T) factoryMethod.invoke(configuration, resolveParameters(factoryMethod, prefix));
		}
		catch (RuntimeException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Object[] resolveParameters(Executable executable, String prefix) {
		Type[] types = executable.getGenericParameterTypes();
		Object[] parameters = new Object[executable.getParameterCount()];
		for (int i = 0; i < types.length; i++) {
			if (types[i] instanceof Class<?> clz) {
				if (clz.isInstance(this.applicationContext)) {
					parameters[i] = this.applicationContext;
				}
				else if (clz == Environment.class) {
					parameters[i] = this.applicationContext.getEnvironment();
				}
				else if (clz == SslBundles.class) {
					parameters[i] = beanProviderOf(SslBundles.class).getIfAvailable();
				}
				else if (getSharedTypes().contains(clz)) {
					parameters[i] = beanProviderOf(clz).getIfAvailable();
				}
				else {
					parameters[i] = beanFor(clz, prefix);
				}
				continue;
			}
			else if (types[i] instanceof ParameterizedType pt) {
				Type rawType = pt.getRawType();
				if (rawType instanceof Class<?> clz) {
					if (clz == ObjectProvider.class) {
						parameters[i] = beanProviderOf((Class<?>) pt.getActualTypeArguments()[0]);
					}
					else {
						parameters[i] = this.applicationContext.getBean(beanNameFor(clz, prefix));
					}
					continue;
				}

			}
			throw new RuntimeException("Unsupported parameter type: " + types[i]);
		}
		return parameters;
	}

}
