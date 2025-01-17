package io.additionalbeans.rabbitmq;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.amqp.CachingConnectionFactoryConfigurer;
import org.springframework.boot.autoconfigure.amqp.ConnectionFactoryCustomizer;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public class AdditionalRabbitmqPostProcessor
		extends AdditionalBeansPostProcessor<RabbitProperties, RabbitConnectionDetails> {

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		registerRabbitConnectionFactory(registry, prefix);
		registerRabbitTemplate(registry, prefix);
		if (ClassUtils.isPresent("org.springframework.amqp.rabbit.core.RabbitMessagingTemplate",
				RabbitAutoConfiguration.class.getClassLoader())) {
			registerRabbitMessagingTemplate(registry, prefix);
		}
	}

	private void registerRabbitConnectionFactory(BeanDefinitionRegistry registry, String prefix) {
		String rabbitConnectionFactoryCreatorClassName = "RabbitConnectionFactoryCreator";
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setInstanceSupplier(() -> {
			try {
				Class<?> clazz = ClassUtils.forName(
						RabbitAutoConfiguration.class.getName() + '.' + rabbitConnectionFactoryCreatorClassName,
						RabbitAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = clazz.getDeclaredConstructor(RabbitProperties.class);
				ctor.setAccessible(true);
				return ctor.newInstance(beanFor(RabbitProperties.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		String configurationBeanName = prefix + rabbitConnectionFactoryCreatorClassName;
		registry.registerBeanDefinition(configurationBeanName, beanDefinition);

		registerBeanInstanceSupplier(registry, RabbitConnectionFactoryBeanConfigurer.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitConnectionFactoryBeanConfigurer", ResourceLoader.class,
							RabbitConnectionDetails.class, ObjectProvider.class, ObjectProvider.class,
							ObjectProvider.class);
				method.setAccessible(true);
				return (RabbitConnectionFactoryBeanConfigurer) method.invoke(connectionConfiguration,
						this.applicationContext, beanFor(RabbitConnectionDetails.class, prefix),
						beanProviderOf(CredentialsProvider.class), beanProviderOf(CredentialsRefreshService.class),
						beanProviderOf(SslBundles.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		registerBeanInstanceSupplier(registry, CachingConnectionFactoryConfigurer.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitConnectionFactoryConfigurer", RabbitConnectionDetails.class,
							ObjectProvider.class);
				method.setAccessible(true);
				return (CachingConnectionFactoryConfigurer) method.invoke(connectionConfiguration,
						beanFor(RabbitConnectionDetails.class, prefix), beanProviderOf(ConnectionNameStrategy.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		registerBeanInstanceSupplier(registry, ConnectionFactory.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitConnectionFactory", RabbitConnectionFactoryBeanConfigurer.class,
							CachingConnectionFactoryConfigurer.class, ObjectProvider.class);
				method.setAccessible(true);
				return (ConnectionFactory) method.invoke(connectionConfiguration,
						beanFor(RabbitConnectionFactoryBeanConfigurer.class, prefix),
						beanFor(CachingConnectionFactoryConfigurer.class, prefix),
						beanProviderOf(ConnectionFactoryCustomizer.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerRabbitTemplate(BeanDefinitionRegistry registry, String prefix) {
		String rabbitTemplateConfigurationClassName = "RabbitTemplateConfiguration";
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition
			.setBeanClassName(RabbitAutoConfiguration.class.getName() + '.' + rabbitTemplateConfigurationClassName);
		String configurationBeanName = prefix + rabbitTemplateConfigurationClassName;
		registry.registerBeanDefinition(configurationBeanName, beanDefinition);

		registerBeanInstanceSupplier(registry, RabbitTemplateConfigurer.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitTemplateConfigurer", RabbitProperties.class, ObjectProvider.class,
							ObjectProvider.class);
				method.setAccessible(true);
				return (RabbitTemplateConfigurer) method.invoke(connectionConfiguration,
						beanFor(RabbitProperties.class, prefix), beanProviderOf(MessageConverter.class),
						beanProviderOf(RabbitRetryTemplateCustomizer.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		registerBeanInstanceSupplier(registry, RabbitTemplate.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitTemplate", RabbitTemplateConfigurer.class, ConnectionFactory.class,
							ObjectProvider.class);
				method.setAccessible(true);
				return (RabbitTemplate) method.invoke(connectionConfiguration,
						beanFor(RabbitTemplateConfigurer.class, prefix), beanFor(ConnectionFactory.class, prefix),
						beanProviderOf(RabbitTemplateCustomizer.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		if (!"false".equals(this.environment.getProperty("spring.rabbitmq.dynamic".replace("spring", prefix)))) {
			registerBeanInstanceSupplier(registry, AmqpAdmin.class, prefix, () -> {
				Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
				try {
					Method method = connectionConfiguration.getClass()
						.getDeclaredMethod("amqpAdmin", ConnectionFactory.class);
					method.setAccessible(true);
					return (AmqpAdmin) method.invoke(connectionConfiguration, beanFor(ConnectionFactory.class, prefix));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
		}
	}

	private void registerRabbitMessagingTemplate(BeanDefinitionRegistry registry, String prefix) {
		String messagingTemplateConfigurationClassName = "MessagingTemplateConfiguration";
		String beanClassName = RabbitAutoConfiguration.class.getName() + '.' + messagingTemplateConfigurationClassName;
		if (!ClassUtils.isPresent(beanClassName, RabbitAutoConfiguration.class.getClassLoader())) {
			messagingTemplateConfigurationClassName = "RabbitMessagingTemplateConfiguration";
			beanClassName = RabbitAutoConfiguration.class.getName() + '.' + messagingTemplateConfigurationClassName;
		}
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition.setBeanClassName(beanClassName);
		String configurationBeanName = prefix + messagingTemplateConfigurationClassName;
		registry.registerBeanDefinition(configurationBeanName, beanDefinition);

		registerBeanInstanceSupplier(registry, RabbitTemplateConfigurer.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("rabbitMessagingTemplate", RabbitTemplate.class);
				method.setAccessible(true);
				return (RabbitTemplateConfigurer) method.invoke(connectionConfiguration,
						beanFor(RabbitTemplate.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

}
