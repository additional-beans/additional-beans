package io.additionalbeans.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = { AdditionalJdbcPostProcessor.KEY_ADDITIONAL_JDBC_PREFIXES + "=foo, bar",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:default",
		"spring.datasource.hikari.minimum-idle=10", "spring.datasource.hikari.maximum-pool-size=20",
		"spring.jdbc.template.fetch-size=100", "spring.jdbc.template.max-rows=1000",
		"foo.datasource.url=jdbc:h2:mem:foo", "foo.datasource.hikari.minimum-idle=20",
		"foo.datasource.hikari.maximum-pool-size=40", "foo.jdbc.template.max-rows=2000",
		"bar.datasource.url=jdbc:h2:mem:bar", "bar.datasource.hikari.minimum-idle=30",
		"bar.datasource.hikari.maximum-pool-size=60", "bar.jdbc.template.max-rows=3000" })
@ImportAutoConfiguration({ DataSourceAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
		JdbcClientAutoConfiguration.class, AdditionalJdbcAutoConfiguration.class })
@SpringJUnitConfig
class AdditionalDataSourceAutoConfigurationTests {

	@Autowired
	private DataSourceProperties dataSourceProperties;

	@Autowired
	@Qualifier("fooDataSourceProperties")
	private DataSourceProperties fooDataSourceProperties;

	@Autowired
	@Qualifier("barDataSourceProperties")
	private DataSourceProperties barDataSourceProperties;

	@Autowired
	private JdbcConnectionDetails jdbcConnectionDetails;

	@Autowired
	@Qualifier("fooJdbcConnectionDetails")
	private JdbcConnectionDetails fooJdbcConnectionDetails;

	@Autowired
	@Qualifier("barJdbcConnectionDetails")
	private JdbcConnectionDetails barJdbcConnectionDetails;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier("fooDataSource")
	private DataSource fooDataSource;

	@Autowired
	@Qualifier("barDataSource")
	private DataSource barDataSource;

	@Autowired
	private JdbcProperties jdbcProperties;

	@Autowired
	@Qualifier("fooJdbcProperties")
	private JdbcProperties fooJdbcProperties;

	@Autowired
	@Qualifier("barJdbcProperties")
	private JdbcProperties barJdbcProperties;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("fooJdbcTemplate")
	private JdbcTemplate fooJdbcTemplate;

	@Autowired
	@Qualifier("barJdbcTemplate")
	private JdbcTemplate barJdbcTemplate;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	@Qualifier("fooNamedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate fooNamedParameterJdbcTemplate;

	@Autowired
	@Qualifier("barNamedParameterJdbcTemplate")
	private NamedParameterJdbcTemplate barNamedParameterJdbcTemplate;

	@Autowired
	private JdbcClient jdbcClient;

	@Autowired
	@Qualifier("fooJdbcClient")
	private JdbcClient fooJdbcClient;

	@Autowired
	@Qualifier("barJdbcClient")
	private JdbcClient barJdbcClient;

	@Test
	void testDataSourceProperties() {
		assertThat(this.fooDataSourceProperties.getDriverClassName())
			.isEqualTo(this.dataSourceProperties.getDriverClassName());
		assertThat(this.barDataSourceProperties.getDriverClassName())
			.isEqualTo(this.dataSourceProperties.getDriverClassName());
		assertThat(this.dataSourceProperties.getUrl()).endsWith("default");
		assertThat(this.fooDataSourceProperties.getUrl()).endsWith("foo");
		assertThat(this.barDataSourceProperties.getUrl()).endsWith("bar");
	}

	@Test
	void testDataSourceConnectionDetails() {
		assertThat(this.jdbcConnectionDetails.getJdbcUrl()).endsWith("default");
		assertThat(this.fooJdbcConnectionDetails.getJdbcUrl()).endsWith("foo");
		assertThat(this.barJdbcConnectionDetails.getJdbcUrl()).endsWith("bar");
	}

	@Test
	void testDataSource() throws SQLException {
		assertThat(this.dataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
			assertThat(ds.getMinimumIdle()).isEqualTo(10);
			assertThat(ds.getMaximumPoolSize()).isEqualTo(20);
		});
		assertThat(this.fooDataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
			assertThat(ds.getMinimumIdle()).isEqualTo(20);
			assertThat(ds.getMaximumPoolSize()).isEqualTo(40);
		});
		assertThat(this.barDataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
			assertThat(ds.getMinimumIdle()).isEqualTo(30);
			assertThat(ds.getMaximumPoolSize()).isEqualTo(60);
		});

		try (Connection connection = this.dataSource.getConnection()) {
			assertThat(connection.getCatalog()).isEqualTo("default".toUpperCase(Locale.ROOT));
		}
		try (Connection connection = this.fooDataSource.getConnection()) {
			assertThat(connection.getCatalog()).isEqualTo("foo".toUpperCase(Locale.ROOT));
		}
		try (Connection connection = this.barDataSource.getConnection()) {
			assertThat(connection.getCatalog()).isEqualTo("bar".toUpperCase(Locale.ROOT));
		}
	}

	@Test
	void testJdbcProperties() {
		assertThat(this.fooJdbcProperties.getTemplate().getFetchSize())
			.isEqualTo(this.jdbcProperties.getTemplate().getFetchSize());
		assertThat(this.barJdbcProperties.getTemplate().getFetchSize())
			.isEqualTo(this.jdbcProperties.getTemplate().getFetchSize());
		assertThat(this.jdbcProperties.getTemplate().getMaxRows()).isEqualTo(1000);
		assertThat(this.fooJdbcProperties.getTemplate().getMaxRows()).isEqualTo(2000);
		assertThat(this.barJdbcProperties.getTemplate().getMaxRows()).isEqualTo(3000);
	}

	@Test
	void testJdbcTemplate() {
		assertThat(this.jdbcTemplate.getDataSource()).isSameAs(this.dataSource);
		assertThat(this.fooJdbcTemplate.getDataSource()).isSameAs(this.fooDataSource);
		assertThat(this.barJdbcTemplate.getDataSource()).isSameAs(this.barDataSource);
	}

	@Test
	void testNamedParameterJdbcTemplate() {
		assertThat(this.namedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(this.jdbcTemplate);
		assertThat(this.fooNamedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(this.fooJdbcTemplate);
		assertThat(this.barNamedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(this.barJdbcTemplate);
	}

	@Test
	void testJdbcClient() {
		assertThat(this.jdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
			.isEqualTo("default".toUpperCase(Locale.ROOT));
		assertThat(this.fooJdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
			.isEqualTo("foo".toUpperCase(Locale.ROOT));
		assertThat(this.barJdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
			.isEqualTo("bar".toUpperCase(Locale.ROOT));
	}

}
