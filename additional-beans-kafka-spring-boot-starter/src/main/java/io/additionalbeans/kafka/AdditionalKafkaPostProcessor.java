package io.additionalbeans.kafka;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.transaction.KafkaTransactionManager;

/**
 * @author Yanming Zhou
 */
public class AdditionalKafkaPostProcessor
		extends AdditionalBeansPostProcessor<KafkaProperties, KafkaConnectionDetails> {

	@Override
	protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {
		registerKafkaAutoConfiguration(registry, prefix);
		registerKafkaProducerFactory(registry, prefix);
		registerKafkaProducerListener(registry, prefix);
		registerKafkaConsumerFactory(registry, prefix);
		registerKafkaTemplate(registry, prefix);
		registerKafkaAdmin(registry, prefix);
		if (this.environment
			.getProperty("spring.kafka.producer.transaction-id-prefix".replace("spring", prefix)) != null) {
			registerKafkaTransactionManager(registry, prefix);
		}
		if ("true".equals(this.environment.getProperty("spring.kafka.retry.topic.enabled".replace("spring", prefix)))) {
			registerKafkaRetryTopicConfiguration(registry, prefix);
		}
	}

	private void registerKafkaAutoConfiguration(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, KafkaAutoConfiguration.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setBeanClass(KafkaAutoConfiguration.class);
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments
				.addGenericArgumentValue(new RuntimeBeanReference(beanNameForPrefix(KafkaProperties.class, prefix)));
			beanDefinition.setConstructorArgumentValues(arguments);
			return beanDefinition;
		});
	}

	private void registerKafkaProducerFactory(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, DefaultKafkaProducerFactory.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix).kafkaProducerFactory(
						beanForPrefix(KafkaConnectionDetails.class, prefix),
						beanProviderFor(DefaultKafkaProducerFactoryCustomizer.class),
						beanProviderFor(SslBundles.class)));
	}

	private void registerKafkaProducerListener(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, ProducerListener.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix).kafkaProducerListener());
	}

	private void registerKafkaConsumerFactory(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, DefaultKafkaConsumerFactory.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix).kafkaConsumerFactory(
						beanForPrefix(KafkaConnectionDetails.class, prefix),
						beanProviderFor(DefaultKafkaConsumerFactoryCustomizer.class),
						beanProviderFor(SslBundles.class)));
	}

	@SuppressWarnings("unchecked")
	private void registerKafkaTemplate(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, KafkaTemplate.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix).kafkaTemplate(
						beanForPrefix(DefaultKafkaProducerFactory.class, prefix),
						beanForPrefix(ProducerListener.class, prefix), beanProviderFor(RecordMessageConverter.class)));
	}

	private void registerKafkaAdmin(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, KafkaAdmin.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix).kafkaAdmin(
						beanForPrefix(KafkaConnectionDetails.class, prefix), beanProviderFor(SslBundles.class)));
	}

	private void registerKafkaTransactionManager(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, KafkaTransactionManager.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix)
					.kafkaTransactionManager(beanForPrefix(DefaultKafkaProducerFactory.class, prefix)));
	}

	private void registerKafkaRetryTopicConfiguration(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, RetryTopicConfiguration.class, prefix,
				() -> beanForPrefix(KafkaAutoConfiguration.class, prefix)
					.kafkaRetryTopicConfiguration(beanForPrefix(KafkaTemplate.class, prefix)));
	}

}
