package io.additionalbeans.commons;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.ResolvableType;
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
		ConfigurationProperties configurationProperties = this.configurationPropertiesClass
			.getAnnotation(ConfigurationProperties.class);
		Assert.notNull(this.configurationPropertiesClass,
				"configurationPropertiesClass should annotated with @ConfigurationProperties");
		this.defaultConfigurationPropertiesPrefix = configurationProperties.prefix();
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

	protected abstract void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix);

	protected void registerConfigurationProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, this.configurationPropertiesClass, prefix, () -> {
			try {
				CP properties = this.configurationPropertiesClass.getConstructor().newInstance();
				CP defaultProperties = this.applicationContext.getBean("%s-%s"
					.formatted(this.defaultConfigurationPropertiesPrefix, this.configurationPropertiesClass.getName()),
						this.configurationPropertiesClass);
				copyNonNullProperties(defaultProperties, properties);
				return properties;
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		String propertiesConnectionDetailsClassName = this.connectionDetailsClass.getPackageName() + ".Properties"
				+ this.connectionDetailsClass.getSimpleName();
		if (ClassUtils.isPresent(propertiesConnectionDetailsClassName, this.connectionDetailsClass.getClassLoader())) {
			registerBeanDefinition(registry, this.connectionDetailsClass, prefix, () -> {
				RootBeanDefinition beanDefinition = new RootBeanDefinition();
				beanDefinition.setBeanClassName(propertiesConnectionDetailsClassName);
				ConstructorArgumentValues arguments = new ConstructorArgumentValues();
				arguments.addGenericArgumentValue(
						new RuntimeBeanReference(beanNameFor(this.configurationPropertiesClass, prefix)));
				beanDefinition.setConstructorArgumentValues(arguments);
				return beanDefinition;
			});
		}
	}

	private static <T> void copyNonNullProperties(T source, T target) {
		PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(target.getClass());
		for (PropertyDescriptor targetPd : targetPds) {
			Method readMethod = targetPd.getReadMethod();
			Method writeMethod = targetPd.getWriteMethod();
			if (readMethod != null && writeMethod != null) {
				try {
					Object value = readMethod.invoke(source);
					if (value != null) {
						writeMethod.invoke(target, value);
					}
				}
				catch (Throwable ex) {
					throw new FatalBeanException(
							"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
				}
			}
		}
	}

	protected <T> void registerBeanDefinition(BeanDefinitionRegistry registry, Class<T> beanClass, String prefix,
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
	}

	protected <T> void registerBeanInstanceSupplier(BeanDefinitionRegistry registry, Class<T> beanClass, String prefix,
			Supplier<T> instanceSupplier) {
		this.registerBeanDefinition(registry, beanClass, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setInstanceSupplier(instanceSupplier);
			return beanDefinition;
		});
	}

	protected <T> ObjectProvider<T> beanProviderOf(Class<T> beanClass) {
		return this.applicationContext.getBeanProvider(beanClass);
	}

	protected <T> T beanFor(Class<T> beanClass, String prefix) {
		return this.applicationContext.getBean(beanNameFor(beanClass, prefix), beanClass);
	}

	protected String beanNameFor(Class<?> beanClass, String prefix) {
		String name = beanClass.getSimpleName();
		String classPrefix = "Default";
		if (name.startsWith(classPrefix)) {
			name = name.substring(classPrefix.length());
		}
		return prefix + name;
	}

}
