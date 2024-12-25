package io.additionalbeans.commons;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * @author Yanming Zhou
 */
public abstract class AdditionalBeansPostProcessor
		implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor, ApplicationContextAware, InitializingBean {

	protected ApplicationContext applicationContext;

	protected Environment environment;

	protected Binder binder;

	protected List<String> prefixes = Collections.emptyList();

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

	protected abstract String configurationKeyForPrefixes();

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		for (String prefix : this.prefixes) {
			registerBeanDefinitionsForPrefix(registry, prefix);
		}
	}

	protected abstract void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix);

	protected <T> void registerBeanDefinition(BeanDefinitionRegistry registry, Class<T> beanClass, String prefix,
			Supplier<BeanDefinition> beanDefinitionSupplier) {
		String beanName = beanNameFor(beanClass, prefix);
		if (!registry.containsBeanDefinition(beanName)) {
			registry.registerBeanDefinition(beanName, beanDefinitionSupplier.get());
		}
	}

	public String beanNameFor(Class<?> beanClass, String prefix) {
		return prefix + beanClass.getSimpleName();
	}

}
