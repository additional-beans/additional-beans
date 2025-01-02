package io.additionalbeans.commons;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalBeansPostProcessorTests {

	AdditionalBeansPostProcessor<RedisProperties, RedisConnectionDetails> processor = new AdditionalBeansPostProcessor<>() {

		@Override
		protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {

		}
	};

	@Test
	void testBeanNameForPrefix() {
		assertThat(this.processor.beanNameForPrefix(RedisProperties.class, "test")).isEqualTo("testRedisProperties");
		assertThat(this.processor.beanNameForPrefix(DefaultBeanNameGenerator.class, "test"))
			.isEqualTo("testBeanNameGenerator");
	}

}
