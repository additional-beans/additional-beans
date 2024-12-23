package io.additionalbeans.jdbc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class AdditionalJdbcAutoConfiguration {

	@Bean
	@Role(ROLE_INFRASTRUCTURE)
	static AdditionalJdbcPostProcessor additionalJdbcPostProcessor() {
		return new AdditionalJdbcPostProcessor();
	}

}
