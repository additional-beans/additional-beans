package io.additionalbeans.rabbitmq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@AutoConfigureAfter(RabbitAutoConfiguration.class)
public class AdditionalRabbitmqAutoConfiguration {

	@Bean
	@Role(ROLE_INFRASTRUCTURE)
	static AdditionalRabbitmqPostProcessor additionalRabbitmqPostProcessor() {
		return new AdditionalRabbitmqPostProcessor();
	}

}
