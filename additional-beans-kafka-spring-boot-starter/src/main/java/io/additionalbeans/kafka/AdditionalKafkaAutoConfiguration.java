package io.additionalbeans.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@AutoConfigureAfter(KafkaAutoConfiguration.class)
public class AdditionalKafkaAutoConfiguration {

	@Bean
	@Role(ROLE_INFRASTRUCTURE)
	static AdditionalKafkaPostProcessor additionalKafkaPostProcessor() {
		return new AdditionalKafkaPostProcessor();
	}

}
