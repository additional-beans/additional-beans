package io.additionalbeans.kafka;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaConnectionDetails;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { "additional.kafka.prefixes=foo,bar", "spring.kafka.bootstrap-servers=localhost:9092",
		"spring.kafka.client-id=default", "foo.kafka.client-id=foo", "foo.kafka.retry.topic.enabled=true",
		"bar.kafka.client-id=bar", "bar.kafka.producer.transaction-id-prefix=bar" })
@ImportAutoConfiguration({ KafkaAutoConfiguration.class, AdditionalKafkaAutoConfiguration.class })
@SpringJUnitConfig
class AdditionalKafkaAutoConfigurationTests {

	@Autowired
	private KafkaProperties kafkaProperties;

	@Autowired
	@Qualifier("fooKafkaProperties")
	private KafkaProperties fooKafkaProperties;

	@Autowired
	@Qualifier("barKafkaProperties")
	private KafkaProperties barKafkaProperties;

	@Autowired
	private KafkaConnectionDetails kafkaConnectionDetails;

	@Autowired
	@Qualifier("fooKafkaConnectionDetails")
	private KafkaConnectionDetails fooKafkaConnectionDetails;

	@Autowired
	@Qualifier("barKafkaConnectionDetails")
	private KafkaConnectionDetails barKafkaConnectionDetails;

	@Autowired
	private DefaultKafkaProducerFactory<?, ?> kafkaProducerFactory;

	@Autowired
	@Qualifier("fooKafkaProducerFactory")
	private DefaultKafkaProducerFactory<?, ?> fooKafkaProducerFactory;

	@Autowired
	@Qualifier("barKafkaProducerFactory")
	private DefaultKafkaProducerFactory<?, ?> barKafkaProducerFactory;

	@Autowired
	private ProducerListener<Object, Object> kafkaProducerListener;

	@Autowired
	@Qualifier("fooProducerListener")
	private ProducerListener<Object, Object> fooProducerListener;

	@Autowired
	@Qualifier("barProducerListener")
	private ProducerListener<Object, Object> barProducerListener;

	@Autowired
	private DefaultKafkaConsumerFactory<?, ?> kafkaConsumerFactory;

	@Autowired
	@Qualifier("fooKafkaConsumerFactory")
	private DefaultKafkaConsumerFactory<?, ?> fooKafkaConsumerFactory;

	@Autowired
	@Qualifier("barKafkaConsumerFactory")
	private DefaultKafkaConsumerFactory<?, ?> barKafkaConsumerFactory;

	@Autowired
	private KafkaTemplate<?, ?> kafkaTemplate;

	@Autowired
	@Qualifier("fooKafkaTemplate")
	private KafkaTemplate<?, ?> fooKafkaTemplate;

	@Autowired
	@Qualifier("barKafkaTemplate")
	private KafkaTemplate<?, ?> barKafkaTemplate;

	@Autowired
	private KafkaAdmin kafkaAdmin;

	@Autowired
	@Qualifier("fooKafkaAdmin")
	private KafkaAdmin fooKafkaAdmin;

	@Autowired
	@Qualifier("barKafkaAdmin")
	private KafkaAdmin barKafkaAdmin;

	@Autowired
	private BeanFactory beanFactory;

	@Test
	void testKafkaProperties() {
		assertThat(this.fooKafkaProperties.getBootstrapServers()).isEqualTo(this.kafkaProperties.getBootstrapServers());
		assertThat(this.barKafkaProperties.getBootstrapServers()).isEqualTo(this.kafkaProperties.getBootstrapServers());
		assertThat(this.kafkaProperties.getClientId()).isEqualTo("default");
		assertThat(this.fooKafkaProperties.getClientId()).isEqualTo("foo");
		assertThat(this.barKafkaProperties.getClientId()).isEqualTo("bar");
	}

	@Test
	void testKafkaConnectionDetails() {
		assertThat(this.kafkaConnectionDetails.getBootstrapServers())
			.isEqualTo(this.kafkaProperties.getBootstrapServers());
		assertThat(this.fooKafkaConnectionDetails.getBootstrapServers())
			.isEqualTo(this.fooKafkaProperties.getBootstrapServers());
		assertThat(this.barKafkaConnectionDetails.getBootstrapServers())
			.isEqualTo(this.barKafkaProperties.getBootstrapServers());
	}

	@Test
	void testKafkaProducerFactory() {
		assertThat(this.kafkaProducerFactory).isNotSameAs(this.fooKafkaProducerFactory);
		assertThat(this.kafkaProducerFactory).isNotSameAs(this.barKafkaProducerFactory);
		assertThat(this.fooKafkaProducerFactory).isNotSameAs(this.barKafkaProducerFactory);
	}

	@Test
	void testKafkaProducerListener() {
		assertThat(this.kafkaProducerListener).isNotSameAs(this.fooProducerListener);
		assertThat(this.fooProducerListener).isNotSameAs(this.barProducerListener);
		assertThat(this.fooProducerListener).isNotSameAs(this.barProducerListener);
	}

	@Test
	void testKafkaConsumerFactory() {
		assertThat(this.kafkaConsumerFactory).isNotSameAs(this.fooKafkaConsumerFactory);
		assertThat(this.kafkaConsumerFactory).isNotSameAs(this.barKafkaConsumerFactory);
		assertThat(this.fooKafkaConsumerFactory).isNotSameAs(this.barKafkaConsumerFactory);
	}

	@Test
	void testKafkaTemplate() {
		assertThat(this.kafkaTemplate).isNotSameAs(this.fooKafkaTemplate);
		assertThat(this.kafkaTemplate).isNotSameAs(this.barKafkaTemplate);
		assertThat(this.barKafkaTemplate).isNotSameAs(this.fooKafkaTemplate);
	}

	@Test
	void testKafkaAdmin() {
		assertThat(this.kafkaAdmin).isNotSameAs(this.fooKafkaAdmin);
		assertThat(this.kafkaAdmin).isNotSameAs(this.barKafkaAdmin);
		assertThat(this.barKafkaAdmin).isNotSameAs(this.fooKafkaAdmin);
	}

	@Test
	void testKafkaTransactionManager() {
		assertThat(this.beanFactory.containsBean("kafkaTransactionManager")).isFalse();
		assertThat(this.beanFactory.containsBean("fooKafkaTransactionManager")).isFalse();
		assertThat(this.beanFactory.containsBean("barKafkaTransactionManager")).isTrue();
	}

	@Test
	void testKafkaRetryTopicConfiguration() {
		assertThat(this.beanFactory.containsBean("kafkaRetryTopicConfiguration")).isFalse();
		assertThat(this.beanFactory.containsBean("fooRetryTopicConfiguration")).isTrue();
		assertThat(this.beanFactory.containsBean("barRetryTopicConfiguration")).isFalse();
	}

}
