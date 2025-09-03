package io.additionalbeans.rabbitmq;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.amqp.autoconfigure.CachingConnectionFactoryConfigurer;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionDetails;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateConfigurer;
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
		String configurationBeanName = registerBeanDefinition(registry,
				RabbitAutoConfiguration.class.getName() + ".RabbitConnectionFactoryCreator", prefix);
		registerBeanDefinition(registry, RabbitConnectionFactoryBeanConfigurer.class, prefix, configurationBeanName,
				"rabbitConnectionFactoryBeanConfigurer");
		registerBeanDefinition(registry, CachingConnectionFactoryConfigurer.class, prefix, configurationBeanName,
				"rabbitConnectionFactoryConfigurer");
		registerBeanDefinition(registry, ConnectionFactory.class, prefix, configurationBeanName,
				"rabbitConnectionFactory");
	}

	private void registerRabbitTemplate(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				RabbitAutoConfiguration.class.getName() + ".RabbitTemplateConfiguration", prefix);
		registerBeanDefinition(registry, RabbitTemplateConfigurer.class, prefix, configurationBeanName,
				"rabbitTemplateConfigurer");
		registerBeanDefinition(registry, RabbitTemplate.class, prefix, configurationBeanName, "rabbitTemplate");
		if (!"false".equals(this.environment.getProperty("spring.rabbitmq.dynamic".replace("spring", prefix)))) {
			registerBeanDefinition(registry, AmqpAdmin.class, prefix, configurationBeanName, "amqpAdmin");
		}
	}

	private void registerRabbitMessagingTemplate(BeanDefinitionRegistry registry, String prefix) {
		String messagingTemplateConfigurationClassName = "MessagingTemplateConfiguration";
		String beanClassName = RabbitAutoConfiguration.class.getName() + '.' + messagingTemplateConfigurationClassName;
		if (!ClassUtils.isPresent(beanClassName, RabbitAutoConfiguration.class.getClassLoader())) {
			messagingTemplateConfigurationClassName = "RabbitMessagingTemplateConfiguration";
			beanClassName = RabbitAutoConfiguration.class.getName() + '.' + messagingTemplateConfigurationClassName;
		}
		String configurationBeanName = registerBeanDefinition(registry, beanClassName, prefix);
		registerBeanDefinition(registry, RabbitTemplateConfigurer.class, prefix, configurationBeanName,
				"rabbitMessagingTemplate");
	}

}
