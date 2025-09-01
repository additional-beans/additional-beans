package io.additionalbeans.kafka;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.transaction.KafkaTransactionManager;

/**
 * @author Yanming Zhou
 */
public class AdditionalKafkaPostProcessor
		extends AdditionalBeansPostProcessor<KafkaProperties, KafkaConnectionDetails> {

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry, KafkaAutoConfiguration.class, prefix);
		registerBeanDefinition(registry, ProducerFactory.class, prefix, configurationBeanName, "kafkaProducerFactory");
		registerBeanDefinition(registry, ProducerListener.class, prefix, configurationBeanName,
				"kafkaProducerListener");
		registerBeanDefinition(registry, ConsumerFactory.class, prefix, configurationBeanName, "kafkaConsumerFactory");
		registerBeanDefinition(registry, KafkaTemplate.class, prefix, configurationBeanName, "kafkaTemplate");
		registerBeanDefinition(registry, KafkaAdmin.class, prefix, configurationBeanName, "kafkaAdmin");
		if (this.environment
			.getProperty("spring.kafka.producer.transaction-id-prefix".replace("spring", prefix)) != null) {
			registerBeanDefinition(registry, KafkaTransactionManager.class, prefix, configurationBeanName,
					"kafkaTransactionManager");
		}
		if ("true".equals(this.environment.getProperty("spring.kafka.retry.topic.enabled".replace("spring", prefix)))) {
			registerBeanDefinition(registry, RetryTopicConfiguration.class, prefix, configurationBeanName,
					"kafkaRetryTopicConfiguration");
		}
	}

}
