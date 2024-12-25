package io.additionalbeans.commons;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalBeansPostProcessorTests {

	AdditionalBeansPostProcessor processor = new AdditionalBeansPostProcessor() {

		@Override
		protected String configurationKeyForPrefixes() {
			return "";
		}

		@Override
		protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {

		}
	};

	@Test
	void testBeanNameFor() {
		assertThat(this.processor.beanNameFor(RedisProperties.class, "test")).isEqualTo("testRedisProperties");
	}

}
