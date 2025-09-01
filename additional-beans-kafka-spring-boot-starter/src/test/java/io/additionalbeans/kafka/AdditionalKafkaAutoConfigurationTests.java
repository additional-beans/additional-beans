package io.additionalbeans.kafka;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.ProducerListener;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalKafkaAutoConfigurationTests {

	private static final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(KafkaAutoConfiguration.class, AdditionalKafkaAutoConfiguration.class))
		.withPropertyValues("additional.kafka.prefixes=foo,bar", "spring.kafka.bootstrap-servers=localhost:9092",
				"spring.kafka.client-id=default", "foo.kafka.client-id=foo", "foo.kafka.retry.topic.enabled=true",
				"bar.kafka.client-id=bar", "bar.kafka.producer.transaction-id-prefix=bar");

	@Autowired
	private KafkaAdmin kafkaAdmin;

	@Test
	void testKafkaProperties() {
		runner.run((ctx) -> {
			KafkaProperties kafkaProperties = ctx.getBean(KafkaProperties.class);
			KafkaProperties fooKafkaProperties = ctx.getBean("fooKafkaProperties", KafkaProperties.class);
			KafkaProperties barKafkaProperties = ctx.getBean("barKafkaProperties", KafkaProperties.class);
			assertThat(fooKafkaProperties.getBootstrapServers()).isEqualTo(kafkaProperties.getBootstrapServers());
			assertThat(barKafkaProperties.getBootstrapServers()).isEqualTo(kafkaProperties.getBootstrapServers());
			assertThat(kafkaProperties.getClientId()).isEqualTo("default");
			assertThat(fooKafkaProperties.getClientId()).isEqualTo("foo");
			assertThat(barKafkaProperties.getClientId()).isEqualTo("bar");
		});
	}

	@Test
	void testKafkaConnectionDetails() {
		runner.run((ctx) -> {
			KafkaConnectionDetails kafkaConnectionDetails = ctx.getBean(KafkaConnectionDetails.class);
			KafkaConnectionDetails fooKafkaConnectionDetails = ctx.getBean("fooKafkaConnectionDetails",
					KafkaConnectionDetails.class);
			KafkaConnectionDetails barKafkaConnectionDetails = ctx.getBean("barKafkaConnectionDetails",
					KafkaConnectionDetails.class);
			KafkaProperties kafkaProperties = ctx.getBean(KafkaProperties.class);
			KafkaProperties fooKafkaProperties = ctx.getBean("fooKafkaProperties", KafkaProperties.class);
			KafkaProperties barKafkaProperties = ctx.getBean("barKafkaProperties", KafkaProperties.class);
			assertThat(kafkaConnectionDetails.getBootstrapServers()).isEqualTo(kafkaProperties.getBootstrapServers());
			assertThat(fooKafkaConnectionDetails.getBootstrapServers())
				.isEqualTo(fooKafkaProperties.getBootstrapServers());
			assertThat(barKafkaConnectionDetails.getBootstrapServers())
				.isEqualTo(barKafkaProperties.getBootstrapServers());
		});
	}

	@Test
	void testKafkaProducerFactory() {
		runner.run((ctx) -> {
			DefaultKafkaProducerFactory<?, ?> kafkaProducerFactory = ctx.getBean(DefaultKafkaProducerFactory.class);
			DefaultKafkaProducerFactory<?, ?> fooKafkaProducerFactory = ctx.getBean("fooProducerFactory",
					DefaultKafkaProducerFactory.class);
			DefaultKafkaProducerFactory<?, ?> barKafkaProducerFactory = ctx.getBean("barProducerFactory",
					DefaultKafkaProducerFactory.class);
			assertThat(kafkaProducerFactory).isNotSameAs(fooKafkaProducerFactory);
			assertThat(kafkaProducerFactory).isNotSameAs(barKafkaProducerFactory);
			assertThat(fooKafkaProducerFactory).isNotSameAs(barKafkaProducerFactory);
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	void testKafkaProducerListener() {
		runner.run((ctx) -> {
			ProducerListener<Object, Object> kafkaProducerListener = ctx.getBean(ProducerListener.class);
			ProducerListener<Object, Object> fooProducerListener = ctx.getBean("fooProducerListener",
					ProducerListener.class);
			ProducerListener<Object, Object> barProducerListener = ctx.getBean("barProducerListener",
					ProducerListener.class);
			assertThat(kafkaProducerListener).isNotSameAs(fooProducerListener);
			assertThat(fooProducerListener).isNotSameAs(barProducerListener);
			assertThat(fooProducerListener).isNotSameAs(barProducerListener);
		});
	}

	@Test
	void testKafkaConsumerFactory() {
		runner.run((ctx) -> {
			DefaultKafkaConsumerFactory<?, ?> kafkaConsumerFactory = ctx.getBean(DefaultKafkaConsumerFactory.class);
			DefaultKafkaConsumerFactory<?, ?> fooKafkaConsumerFactory = ctx.getBean("fooConsumerFactory",
					DefaultKafkaConsumerFactory.class);
			DefaultKafkaConsumerFactory<?, ?> barKafkaConsumerFactory = ctx.getBean("barConsumerFactory",
					DefaultKafkaConsumerFactory.class);
			assertThat(kafkaConsumerFactory).isNotSameAs(fooKafkaConsumerFactory);
			assertThat(kafkaConsumerFactory).isNotSameAs(barKafkaConsumerFactory);
			assertThat(fooKafkaConsumerFactory).isNotSameAs(barKafkaConsumerFactory);
		});
	}

	@Test
	void testKafkaTemplate() {
		runner.run((ctx) -> {
			KafkaTemplate<?, ?> kafkaTemplate = ctx.getBean(KafkaTemplate.class);
			KafkaTemplate<?, ?> fooKafkaTemplate = ctx.getBean("fooKafkaTemplate", KafkaTemplate.class);
			KafkaTemplate<?, ?> barKafkaTemplate = ctx.getBean("barKafkaTemplate", KafkaTemplate.class);
			assertThat(kafkaTemplate).isNotSameAs(fooKafkaTemplate);
			assertThat(kafkaTemplate).isNotSameAs(barKafkaTemplate);
			assertThat(barKafkaTemplate).isNotSameAs(fooKafkaTemplate);
		});
	}

	@Test
	void testKafkaAdmin() {
		runner.run((ctx) -> {
			KafkaAdmin kafkaAdmin = ctx.getBean(KafkaAdmin.class);
			KafkaAdmin fooKafkaAdmin = ctx.getBean("fooKafkaAdmin", KafkaAdmin.class);
			KafkaAdmin barKafkaAdmin = ctx.getBean("barKafkaAdmin", KafkaAdmin.class);
			assertThat(kafkaAdmin).isNotSameAs(fooKafkaAdmin);
			assertThat(kafkaAdmin).isNotSameAs(barKafkaAdmin);
			assertThat(barKafkaAdmin).isNotSameAs(fooKafkaAdmin);
		});
	}

	@Test
	void testKafkaTransactionManager() {
		runner.run((ctx) -> {
			assertThat(ctx).doesNotHaveBean("kafkaTransactionManager");
			assertThat(ctx).doesNotHaveBean("fooKafkaTransactionManager");
			assertThat(ctx).hasBean("barKafkaTransactionManager");
		});
	}

	@Test
	void testKafkaRetryTopicConfiguration() {
		runner.run((ctx) -> {
			assertThat(ctx).doesNotHaveBean("kafkaRetryTopicConfiguration");
			assertThat(ctx).hasBean("fooRetryTopicConfiguration");
			assertThat(ctx).doesNotHaveBean("barRetryTopicConfiguration");
		});
	}

}
