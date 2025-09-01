package io.additionalbeans.commons;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalBeansPostProcessorTests {

	AdditionalBeansPostProcessor<TestProperties, TestConnectionDetails> processor = new AdditionalBeansPostProcessor<>() {

		@Override
		protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {

		}
	};

	@Test
	void testConfigurationKeyForPrefixes() {
		assertThat(this.processor.configurationKeyForPrefixes()).isEqualTo("additional.commons.prefixes");
	}

	@ConfigurationProperties("test")
	static class TestProperties {

	}

	static class TestConnectionDetails {

	}

}
