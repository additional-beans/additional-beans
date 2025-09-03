package io.additionalbeans.rabbitmq;

import org.junit.jupiter.api.Test;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionDetails;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalRabbitmqAutoConfigurationTests {

	private static final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(
				AutoConfigurations.of(RabbitAutoConfiguration.class, AdditionalRabbitmqAutoConfiguration.class))
		.withPropertyValues("additional.rabbitmq.prefixes=foo,bar", "spring.rabbitmq.host=127.0.0.1",
				"spring.rabbitmq.port=5672", "foo.rabbitmq.port=5673", "foo.rabbitmq.username=foo",
				"bar.rabbitmq.port=5674", "bar.rabbitmq.username=bar");

	@Test
	void testRabbitProperties() {
		runner.run((ctx) -> {
			RabbitProperties rabbitProperties = ctx.getBean(RabbitProperties.class);
			RabbitProperties fooRabbitProperties = ctx.getBean("fooRabbitProperties", RabbitProperties.class);
			RabbitProperties barRabbitProperties = ctx.getBean("barRabbitProperties", RabbitProperties.class);
			assertThat(rabbitProperties.getHost()).isEqualTo("127.0.0.1");
			assertThat(fooRabbitProperties.getHost()).isEqualTo(rabbitProperties.getHost());
			assertThat(barRabbitProperties.getHost()).isEqualTo(rabbitProperties.getHost());
			assertThat(rabbitProperties.getPort()).isEqualTo(5672);
			assertThat(fooRabbitProperties.getPort()).isEqualTo(5673);
			assertThat(barRabbitProperties.getPort()).isEqualTo(5674);
			assertThat(fooRabbitProperties.getUsername()).isEqualTo("foo");
			assertThat(barRabbitProperties.getUsername()).isEqualTo("bar");
		});
	}

	@Test
	void testRabbitConnectionDetails() {
		runner.run((ctx) -> {
			RabbitConnectionDetails rabbitConnectionDetails = ctx.getBean(RabbitConnectionDetails.class);
			RabbitConnectionDetails fooRabbitConnectionDetails = ctx.getBean("fooRabbitConnectionDetails",
					RabbitConnectionDetails.class);
			RabbitConnectionDetails barRabbitConnectionDetails = ctx.getBean("barRabbitConnectionDetails",
					RabbitConnectionDetails.class);
			assertThat(rabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5672);
			assertThat(fooRabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5673);
			assertThat(barRabbitConnectionDetails.getFirstAddress().port()).isEqualTo(5674);
			assertThat(barRabbitConnectionDetails.getFirstAddress().host())
				.isEqualTo(rabbitConnectionDetails.getFirstAddress().host());
			assertThat(barRabbitConnectionDetails.getFirstAddress().host())
				.isEqualTo(rabbitConnectionDetails.getFirstAddress().host());
		});
	}

	@Test
	void testConnectionFactory() {
		runner.run((ctx) -> {
			ConnectionFactory connectionFactory = ctx.getBean(ConnectionFactory.class);
			ConnectionFactory fooConnectionFactory = ctx.getBean("fooConnectionFactory", ConnectionFactory.class);
			ConnectionFactory barConnectionFactory = ctx.getBean("barConnectionFactory", ConnectionFactory.class);
			assertThat(connectionFactory).isNotSameAs(fooConnectionFactory);
			assertThat(connectionFactory).isNotSameAs(barConnectionFactory);
			assertThat(barConnectionFactory).isNotSameAs(fooConnectionFactory);
		});
	}

	@Test
	void testRabbitTemplate() {
		runner.run((ctx) -> {
			RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);
			RabbitTemplate fooRabbitTemplate = ctx.getBean("fooRabbitTemplate", RabbitTemplate.class);
			RabbitTemplate barRabbitTemplate = ctx.getBean("barRabbitTemplate", RabbitTemplate.class);
			assertThat(rabbitTemplate).isNotSameAs(fooRabbitTemplate);
			assertThat(rabbitTemplate).isNotSameAs(barRabbitTemplate);
			assertThat(barRabbitTemplate).isNotSameAs(fooRabbitTemplate);
		});
	}

	@Test
	void testRabbitAdmin() {
		runner.run((ctx) -> {
			AmqpAdmin amqpAdmin = ctx.getBean(AmqpAdmin.class);
			AmqpAdmin fooAmqpAdmin = ctx.getBean("fooAmqpAdmin", AmqpAdmin.class);
			AmqpAdmin barAmqpAdmin = ctx.getBean("barAmqpAdmin", AmqpAdmin.class);
			assertThat(amqpAdmin).isNotSameAs(fooAmqpAdmin);
			assertThat(amqpAdmin).isNotSameAs(barAmqpAdmin);
			assertThat(barAmqpAdmin).isNotSameAs(fooAmqpAdmin);
		});
	}

}
