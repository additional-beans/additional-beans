package io.additionalbeans.mongodb;

import java.util.Set;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.data.mongodb.autoconfigure.MongoDataAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.boot.mongodb.autoconfigure.StandardMongoClientSettingsBuilderCustomizer;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

/**
 * @author Yanming Zhou
 */
public class AdditionalMongodbPostProcessor
		extends AdditionalBeansPostProcessor<MongoProperties, MongoConnectionDetails> {

	@Override
	protected Set<Class<?>> getSharedTypes() {
		return Set.of(MongoConverter.class);
	}

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		registerMongoClientSettingsConfiguration(registry, prefix);
		registerMongoClient(registry, prefix);
		if (registry.containsBeanDefinition(MongoDataAutoConfiguration.class.getName())) {
			registerMongoDatabaseFactory(registry, prefix);
			registerMongoTemplate(registry, prefix);
		}
	}

	private void registerMongoClientSettingsConfiguration(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				MongoAutoConfiguration.class.getName() + ".MongoClientSettingsConfiguration", prefix);
		registerBeanDefinition(registry, StandardMongoClientSettingsBuilderCustomizer.class, prefix,
				configurationBeanName, "standardMongoSettingsCustomizer");
		registerBeanDefinition(registry, MongoClientSettings.class, prefix, configurationBeanName,
				"mongoClientSettings");
	}

	private void registerMongoClient(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry, MongoAutoConfiguration.class, prefix);
		registerBeanDefinition(registry, MongoClient.class, prefix, configurationBeanName, "mongo");
	}

	private void registerMongoDatabaseFactory(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				MongoDataAutoConfiguration.class.getPackageName() + ".MongoDatabaseFactoryConfiguration", prefix);
		registerBeanDefinition(registry, MongoDatabaseFactory.class, prefix, configurationBeanName,
				"mongoDatabaseFactory");
	}

	private void registerMongoTemplate(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				MongoDataAutoConfiguration.class.getPackageName() + ".MongoDatabaseFactoryDependentConfiguration",
				prefix);
		registerBeanDefinition(registry, MongoTemplate.class, prefix, configurationBeanName, "mongoTemplate");
	}

}
