package io.additionalbeans.mongodb;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalMongodbAutoConfigurationTests {

	private static final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
				AdditionalMongodbAutoConfiguration.class))
		.withPropertyValues("additional.mongodb.prefixes=foo,bar", "spring.data.mongodb.host=127.0.0.1",
				"spring.data.mongodb.port=27017", "foo.data.mongodb.port=27018", "foo.data.mongodb.database=foo",
				"bar.data.mongodb.port=27019", "bar.data.mongodb.database=bar");

	@Test
	void testMongoProperties() {
		runner.run((ctx) -> {
			MongoProperties mongoProperties = ctx.getBean(MongoProperties.class);
			MongoProperties fooMongoProperties = ctx.getBean("fooMongoProperties", MongoProperties.class);
			MongoProperties barMongoProperties = ctx.getBean("barMongoProperties", MongoProperties.class);
			assertThat(mongoProperties.getHost()).isEqualTo("127.0.0.1");
			assertThat(fooMongoProperties.getHost()).isEqualTo(mongoProperties.getHost());
			assertThat(barMongoProperties.getHost()).isEqualTo(mongoProperties.getHost());
			assertThat(mongoProperties.getPort()).isEqualTo(27017);
			assertThat(fooMongoProperties.getPort()).isEqualTo(27018);
			assertThat(barMongoProperties.getPort()).isEqualTo(27019);
			assertThat(fooMongoProperties.getDatabase()).isEqualTo("foo");
			assertThat(barMongoProperties.getDatabase()).isEqualTo("bar");
		});
	}

	@Test
	void testMongoConnectionDetails() {
		runner.run((ctx) -> {
			MongoConnectionDetails mongodbConnectionDetails = ctx.getBean(MongoConnectionDetails.class);
			MongoConnectionDetails fooMongoConnectionDetails = ctx.getBean("fooMongoConnectionDetails",
					MongoConnectionDetails.class);
			MongoConnectionDetails barMongoConnectionDetails = ctx.getBean("barMongoConnectionDetails",
					MongoConnectionDetails.class);
			assertThat(mongodbConnectionDetails.getConnectionString().toString())
				.isEqualTo("mongodb://127.0.0.1:27017/test");
			assertThat(fooMongoConnectionDetails.getConnectionString().toString())
				.isEqualTo("mongodb://127.0.0.1:27018/foo");
			assertThat(barMongoConnectionDetails.getConnectionString().toString())
				.isEqualTo("mongodb://127.0.0.1:27019/bar");
		});
	}

	@Test
	void testMongoClient() {
		runner.run((ctx) -> {
			MongoClient mongoClient = ctx.getBean(MongoClient.class);
			MongoClient fooMongoClient = ctx.getBean("fooMongoClient", MongoClient.class);
			MongoClient barMongoClient = ctx.getBean("barMongoClient", MongoClient.class);
			assertThat(mongoClient).isNotSameAs(fooMongoClient);
			assertThat(mongoClient).isNotSameAs(barMongoClient);
			assertThat(barMongoClient).isNotSameAs(fooMongoClient);
		});
	}

	@Test
	void testMongoDatabaseFactory() {
		runner.run((ctx) -> {
			MongoDatabaseFactory mongoDatabaseFactory = ctx.getBean(MongoDatabaseFactory.class);
			MongoDatabaseFactory fooMongoDatabaseFactory = ctx.getBean("fooMongoDatabaseFactory",
					MongoDatabaseFactory.class);
			MongoDatabaseFactory barMongoDatabaseFactory = ctx.getBean("barMongoDatabaseFactory",
					MongoDatabaseFactory.class);
			assertThat(mongoDatabaseFactory).isNotSameAs(fooMongoDatabaseFactory);
			assertThat(mongoDatabaseFactory).isNotSameAs(barMongoDatabaseFactory);
			assertThat(barMongoDatabaseFactory).isNotSameAs(fooMongoDatabaseFactory);
		});
	}

	@Test
	void testMongoTemplate() {
		runner.run((ctx) -> {
			MongoTemplate mongoTemplate = ctx.getBean(MongoTemplate.class);
			MongoTemplate fooMongoTemplate = ctx.getBean("fooMongoTemplate", MongoTemplate.class);
			MongoTemplate barMongoTemplate = ctx.getBean("barMongoTemplate", MongoTemplate.class);
			assertThat(mongoTemplate).isNotSameAs(fooMongoTemplate);
			assertThat(mongoTemplate).isNotSameAs(barMongoTemplate);
			assertThat(barMongoTemplate).isNotSameAs(fooMongoTemplate);
		});
	}

}
