package io.additionalbeans.mongodb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.autoconfigure.mongo.StandardMongoClientSettingsBuilderCustomizer;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.util.ClassUtils;

/**
 * @author Yanming Zhou
 */
public class AdditionalMongodbPostProcessor
		extends AdditionalBeansPostProcessor<MongoProperties, MongoConnectionDetails> {

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
							MongoConnectionDetails.class);
				method.setAccessible(true);
				return (StandardMongoClientSettingsBuilderCustomizer) method.invoke(connectionConfiguration,
						beanFor(MongoProperties.class, prefix), beanFor(MongoConnectionDetails.class, prefix));
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

	private void registerMongoDatabaseFactory(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, MongoDatabaseFactory.class, prefix, () -> {
			try {
				Class<?> clazz = ClassUtils.forName(
						MongoDataAutoConfiguration.class.getPackageName() + ".MongoDatabaseFactoryConfiguration",
						MongoDataAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = clazz.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object configuration = ctor.newInstance();
				Method method = clazz.getDeclaredMethod("mongoDatabaseFactory", MongoClient.class,
						MongoProperties.class, MongoConnectionDetails.class);
				method.setAccessible(true);
				return (MongoDatabaseFactory) method.invoke(configuration, beanFor(MongoClient.class, prefix),
						beanFor(MongoProperties.class, prefix), beanFor(MongoConnectionDetails.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerMongoTemplate(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, MongoTemplate.class, prefix, () -> {
			try {
				Class<?> clazz = ClassUtils.forName(
						MongoDataAutoConfiguration.class.getPackageName()
								+ ".MongoDatabaseFactoryDependentConfiguration",
						MongoDataAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = clazz.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object configuration = ctor.newInstance();
				Method method = clazz.getDeclaredMethod("mongoTemplate", MongoDatabaseFactory.class,
						MongoConverter.class);
				method.setAccessible(true);
				return (MongoTemplate) method.invoke(configuration, beanFor(MongoDatabaseFactory.class, prefix),
						this.applicationContext.getBean(MongoConverter.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

}
