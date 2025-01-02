package io.additionalbeans.mongodb;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@AutoConfigureAfter(MongoAutoConfiguration.class)
public class AdditionalMongodbAutoConfiguration {

	@Bean
	@Role(ROLE_INFRASTRUCTURE)
	static AdditionalMongodbPostProcessor additionalMongodbPostProcessor() {
		return new AdditionalMongodbPostProcessor();
	}

}
