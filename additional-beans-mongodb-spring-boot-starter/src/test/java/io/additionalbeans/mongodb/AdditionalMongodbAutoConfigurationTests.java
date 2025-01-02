package io.additionalbeans.mongodb;

import com.mongodb.client.MongoClient;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { "additional.mongodb.prefixes=foo,bar", "spring.data.mongodb.host=127.0.0.1",
		"spring.data.mongodb.port=27017", "foo.data.mongodb.port=27018", "foo.data.mongodb.database=foo",
		"bar.data.mongodb.port=27019", "bar.data.mongodb.database=bar" })
@ImportAutoConfiguration({ MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
		AdditionalMongodbAutoConfiguration.class })
@SpringJUnitConfig
class AdditionalMongodbAutoConfigurationTests {

	@Autowired
	private MongoProperties mongodbProperties;

	@Autowired
	@Qualifier("fooMongoProperties")
	private MongoProperties fooMongoProperties;

	@Autowired
	@Qualifier("barMongoProperties")
	private MongoProperties barMongoProperties;

	@Autowired
	private MongoConnectionDetails mongodbConnectionDetails;

	@Autowired
	@Qualifier("fooMongoConnectionDetails")
	private MongoConnectionDetails fooMongoConnectionDetails;

	@Autowired
	@Qualifier("barMongoConnectionDetails")
	private MongoConnectionDetails barMongoConnectionDetails;

	@Autowired
	private MongoClient mongoClient;

	@Autowired
	@Qualifier("fooMongoClient")
	private MongoClient fooMongoClient;

	@Autowired
	@Qualifier("barMongoClient")
	private MongoClient barMongoClient;

	@Autowired
	private MongoDatabaseFactory mongoDatabaseFactory;

	@Autowired
	@Qualifier("fooMongoDatabaseFactory")
	private MongoDatabaseFactory fooMongoDatabaseFactory;

	@Autowired
	@Qualifier("barMongoDatabaseFactory")
	private MongoDatabaseFactory barMongoDatabaseFactory;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	@Qualifier("fooMongoTemplate")
	private MongoTemplate fooMongoTemplate;

	@Autowired
	@Qualifier("barMongoTemplate")
	private MongoTemplate barMongoTemplate;

	@Test
	void testMongoProperties() {
		assertThat(this.mongodbProperties.getHost()).isEqualTo("127.0.0.1");
		assertThat(this.fooMongoProperties.getHost()).isEqualTo(this.mongodbProperties.getHost());
		assertThat(this.barMongoProperties.getHost()).isEqualTo(this.mongodbProperties.getHost());
		assertThat(this.mongodbProperties.getPort()).isEqualTo(27017);
		assertThat(this.fooMongoProperties.getPort()).isEqualTo(27018);
		assertThat(this.barMongoProperties.getPort()).isEqualTo(27019);
		assertThat(this.fooMongoProperties.getDatabase()).isEqualTo("foo");
		assertThat(this.barMongoProperties.getDatabase()).isEqualTo("bar");
	}

	@Test
	void testMongoConnectionDetails() {
		assertThat(this.mongodbConnectionDetails.getConnectionString().toString())
			.isEqualTo("mongodb://127.0.0.1:27017/test");
		assertThat(this.fooMongoConnectionDetails.getConnectionString().toString())
			.isEqualTo("mongodb://127.0.0.1:27018/foo");
		assertThat(this.barMongoConnectionDetails.getConnectionString().toString())
			.isEqualTo("mongodb://127.0.0.1:27019/bar");
	}

	@Test
	void testMongoClient() {
		assertThat(this.mongoClient).isNotSameAs(this.fooMongoClient);
		assertThat(this.mongoClient).isNotSameAs(this.barMongoClient);
		assertThat(this.barMongoClient).isNotSameAs(this.fooMongoClient);
	}

	@Test
	void testMongoDatabaseFactory() {
		assertThat(this.mongoDatabaseFactory).isNotSameAs(this.fooMongoDatabaseFactory);
		assertThat(this.mongoDatabaseFactory).isNotSameAs(this.barMongoDatabaseFactory);
		assertThat(this.barMongoDatabaseFactory).isNotSameAs(this.fooMongoDatabaseFactory);
	}

	@Test
	void testMongoTemplate() {
		assertThat(this.mongoTemplate).isNotSameAs(this.fooMongoTemplate);
		assertThat(this.mongoTemplate).isNotSameAs(this.barMongoTemplate);
		assertThat(this.barMongoTemplate).isNotSameAs(this.fooMongoTemplate);
	}

}
