package io.additionalbeans.rabbitmq;

import org.junit.jupiter.api.Test;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { "additional.rabbitmq.prefixes=foo,bar", "spring.rabbitmq.host=127.0.0.1",
		"spring.rabbitmq.port=5672", "foo.rabbitmq.port=5673", "foo.rabbitmq.username=foo", "bar.rabbitmq.port=5674",
		"bar.rabbitmq.username=bar" })
@ImportAutoConfiguration({ RabbitAutoConfiguration.class, AdditionalRabbitmqAutoConfiguration.class })
@SpringJUnitConfig
class AdditionalRabbitmqAutoConfigurationTests {

	@Autowired
	private RabbitProperties rabbitProperties;

	@Autowired
	@Qualifier("fooRabbitProperties")
	private RabbitProperties fooRabbitProperties;

	@Autowired
	@Qualifier("barRabbitProperties")
	private RabbitProperties barRabbitProperties;

	@Autowired
	private RabbitConnectionDetails rabbitConnectionDetails;

	@Autowired
	@Qualifier("fooRabbitConnectionDetails")
	private RabbitConnectionDetails fooRabbitConnectionDetails;

	@Autowired
	@Qualifier("barRabbitConnectionDetails")
	private RabbitConnectionDetails barRabbitConnectionDetails;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	@Qualifier("fooConnectionFactory")
	private ConnectionFactory fooConnectionFactory;

	@Autowired
	@Qualifier("barConnectionFactory")
	private ConnectionFactory barConnectionFactory;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	@Qualifier("fooRabbitTemplate")
	private RabbitTemplate fooRabbitTemplate;

	@Autowired
	@Qualifier("barRabbitTemplate")
	private RabbitTemplate barRabbitTemplate;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Autowired
	@Qualifier("fooAmqpAdmin")
	private AmqpAdmin fooAmqpAdmin;

	@Autowired
	@Qualifier("barAmqpAdmin")
	private AmqpAdmin barAmqpAdmin;

	@Test
	void testRabbitProperties() {
		assertThat(this.rabbitProperties.getHost()).isEqualTo("127.0.0.1");
		assertThat(this.fooRabbitProperties.getHost()).isEqualTo(this.rabbitProperties.getHost());
		assertThat(this.barRabbitProperties.getHost()).isEqualTo(this.rabbitProperties.getHost());
		assertThat(this.rabbitProperties.getPort()).isEqualTo(5672);
		assertThat(this.fooRabbitProperties.getPort()).isEqualTo(5673);
		assertThat(this.barRabbitProperties.getPort()).isEqualTo(5674);
		assertThat(this.fooRabbitProperties.getUsername()).isEqualTo("foo");
		assertThat(this.barRabbitProperties.getUsername()).isEqualTo("bar");
	}

	@Test
	void testRabbitConnectionDetails() {
		assertThat(this.rabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5672);
		assertThat(this.fooRabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5673);
		assertThat(this.barRabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5674);
		assertThat(this.barRabbitConnectionDetails.getFirstAddress().host())
			.isEqualTo(this.rabbitConnectionDetails.getFirstAddress().host());
		assertThat(this.barRabbitConnectionDetails.getFirstAddress().host())
			.isEqualTo(this.rabbitConnectionDetails.getFirstAddress().host());
	}

	@Test
	void testConnectionFactory() {
		assertThat(this.connectionFactory).isNotSameAs(this.fooConnectionFactory);
		assertThat(this.connectionFactory).isNotSameAs(this.barConnectionFactory);
		assertThat(this.barConnectionFactory).isNotSameAs(this.fooConnectionFactory);
	}

	@Test
	void testRabbitTemplate() {
		assertThat(this.rabbitTemplate).isNotSameAs(this.fooRabbitTemplate);
		assertThat(this.rabbitTemplate).isNotSameAs(this.barRabbitTemplate);
		assertThat(this.barRabbitTemplate).isNotSameAs(this.fooRabbitTemplate);
	}

	@Test
	void testRabbitAdmin() {
		assertThat(this.amqpAdmin).isNotSameAs(this.fooAmqpAdmin);
		assertThat(this.amqpAdmin).isNotSameAs(this.barAmqpAdmin);
		assertThat(this.barAmqpAdmin).isNotSameAs(this.fooAmqpAdmin);
	}

}
