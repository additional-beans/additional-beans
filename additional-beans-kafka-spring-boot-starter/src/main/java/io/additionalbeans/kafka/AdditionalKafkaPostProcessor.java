package io.additionalbeans.kafka;

import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.transaction.KafkaTransactionManager;

/**
 * @author Yanming Zhou
 */
public class AdditionalKafkaPostProcessor extends AdditionalBeansPostProcessor {

	private static final String SPRING_KAFKA_PREFIX = "spring.kafka";

	@Override
	protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {
		registerKafkaProperties(registry, prefix);
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

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof KafkaProperties) {
			String suffix = KafkaProperties.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					this.binder.bind(SPRING_KAFKA_PREFIX.replace("spring", prefix), Bindable.ofInstance(bean));
				}
			}
		}
		return bean;
	}

	private void registerKafkaProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, KafkaProperties.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setTargetType(KafkaProperties.class);
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setInstanceSupplier(() -> {
				KafkaProperties properties = new KafkaProperties();
				KafkaProperties defaultKafkaProperties = this.applicationContext.getBean(
						"%s-%s".formatted(SPRING_KAFKA_PREFIX, KafkaProperties.class.getName()), KafkaProperties.class);
				BeanUtils.copyProperties(defaultKafkaProperties, properties);
				return properties;
			});
			return beanDefinition;
		});

		registerBeanDefinition(registry, KafkaConnectionDetails.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition
				.setBeanClassName(KafkaProperties.class.getPackageName() + ".PropertiesKafkaConnectionDetails");
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(KafkaProperties.class, prefix)));
			beanDefinition.setConstructorArgumentValues(arguments);
			beanDefinition.setTargetType(KafkaConnectionDetails.class);
			return beanDefinition;
		});
	}

	private void registerKafkaAutoConfiguration(BeanDefinitionRegistry registry, String prefix) {

		registerBeanDefinition(registry, KafkaAutoConfiguration.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setBeanClass(KafkaAutoConfiguration.class);
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(KafkaProperties.class, prefix)));
			beanDefinition.setConstructorArgumentValues(arguments);
			return beanDefinition;
		});

	}

	private void registerKafkaProducerFactory(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, DefaultKafkaProducerFactory.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(DefaultKafkaProducerFactory.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix).kafkaProducerFactory(
					beanFor(KafkaConnectionDetails.class, prefix),
					beanProviderFor(DefaultKafkaProducerFactoryCustomizer.class), beanProviderFor(SslBundles.class)));
			return beanDefinition;

		});
	}

	private void registerKafkaProducerListener(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, ProducerListener.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(LoggingProducerListener.class);
			beanDefinition
				.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix).kafkaProducerListener());
			return beanDefinition;
		});
	}

	private void registerKafkaConsumerFactory(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, DefaultKafkaConsumerFactory.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(DefaultKafkaConsumerFactory.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix).kafkaConsumerFactory(
					beanFor(KafkaConnectionDetails.class, prefix),
					beanProviderFor(DefaultKafkaConsumerFactoryCustomizer.class), beanProviderFor(SslBundles.class)));
			return beanDefinition;

		});
	}

	@SuppressWarnings("unchecked")
	private void registerKafkaTemplate(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, KafkaTemplate.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(KafkaTemplate.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix).kafkaTemplate(
					beanFor(DefaultKafkaProducerFactory.class, prefix), beanFor(ProducerListener.class, prefix),
					beanProviderFor(RecordMessageConverter.class)));
			return beanDefinition;
		});
	}

	private void registerKafkaAdmin(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, KafkaAdmin.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(KafkaAdmin.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix)
				.kafkaAdmin(beanFor(KafkaConnectionDetails.class, prefix), beanProviderFor(SslBundles.class)));
			return beanDefinition;
		});
	}

	private void registerKafkaTransactionManager(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, KafkaTransactionManager.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(KafkaTransactionManager.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix)
				.kafkaTransactionManager(beanFor(DefaultKafkaProducerFactory.class, prefix)));
			return beanDefinition;
		});
	}

	private void registerKafkaRetryTopicConfiguration(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, RetryTopicConfiguration.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(RetryTopicConfiguration.class);
			beanDefinition.setInstanceSupplier(() -> beanFor(KafkaAutoConfiguration.class, prefix)
				.kafkaRetryTopicConfiguration(beanFor(KafkaTemplate.class, prefix)));
			return beanDefinition;
		});
	}

}
