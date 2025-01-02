package io.additionalbeans.mongodb;

import java.lang.reflect.Method;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.StandardMongoClientSettingsBuilderCustomizer;

/**
 * @author Yanming Zhou
 */
public class AdditionalMongodbPostProcessor
		extends AdditionalBeansPostProcessor<MongoProperties, MongoConnectionDetails> {

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		registerMongoClientSettingsConfiguration(registry, prefix);
		registerMongoClient(registry, prefix);
	}

	private void registerMongoClientSettingsConfiguration(BeanDefinitionRegistry registry, String prefix) {
		String mongoClientSettingsConfigurationClassName = "MongoClientSettingsConfiguration";
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDefaultCandidate(false);
		beanDefinition
			.setBeanClassName(MongoAutoConfiguration.class.getName() + '.' + mongoClientSettingsConfigurationClassName);
		String configurationBeanName = prefix + mongoClientSettingsConfigurationClassName;
		registry.registerBeanDefinition(configurationBeanName, beanDefinition);

		registerBeanInstanceSupplier(registry, StandardMongoClientSettingsBuilderCustomizer.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass()
					.getDeclaredMethod("standardMongoSettingsCustomizer", MongoProperties.class,
							MongoConnectionDetails.class, ObjectProvider.class);
				method.setAccessible(true);
				return (StandardMongoClientSettingsBuilderCustomizer) method.invoke(connectionConfiguration,
						beanFor(MongoProperties.class, prefix), beanFor(MongoConnectionDetails.class, prefix),
						beanProviderOf(ClientResourcesBuilderCustomizer.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}

		});

		registerBeanInstanceSupplier(registry, MongoClientSettings.class, prefix, () -> {
			Object connectionConfiguration = this.applicationContext.getBean(configurationBeanName);
			try {
				Method method = connectionConfiguration.getClass().getDeclaredMethod("mongoClientSettings");
				method.setAccessible(true);
				return (MongoClientSettings) method.invoke(connectionConfiguration);
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerMongoClient(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, MongoClient.class, prefix,
				() -> new MongoAutoConfiguration().mongo(beanProviderOf(MongoClientSettingsBuilderCustomizer.class),
						beanFor(MongoClientSettings.class, prefix)));
	}

}
