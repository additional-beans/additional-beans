package io.additionalbeans.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Yanming Zhou
 */
class AdditionalDataSourceAutoConfigurationTests {

	private static final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class,
				DataSourceTransactionManagerAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
				JdbcClientAutoConfiguration.class, AdditionalJdbcAutoConfiguration.class))
		.withPropertyValues("additional.jdbc.prefixes=foo,bar", "spring.datasource.name=spring",
				"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.url=jdbc:h2:mem:default",
				"spring.datasource.hikari.minimum-idle=10", "spring.datasource.hikari.maximum-pool-size=20",
				"spring.jdbc.template.fetch-size=100", "spring.jdbc.template.max-rows=1000", "foo.datasource.name=foo",
				"foo.datasource.url=jdbc:h2:mem:foo", "foo.datasource.hikari.minimum-idle=20",
				"foo.datasource.hikari.maximum-pool-size=40", "foo.jdbc.template.max-rows=2000",
				"bar.datasource.name=bar", "bar.datasource.url=jdbc:h2:mem:bar",
				"bar.datasource.hikari.minimum-idle=30", "bar.datasource.hikari.maximum-pool-size=60",
				"bar.jdbc.template.max-rows=3000");

	@Test
	void testDataSourceProperties() {
		runner.run((ctx) -> {
			DataSourceProperties dataSourceProperties = ctx.getBean(DataSourceProperties.class);
			DataSourceProperties fooDataSourceProperties = ctx.getBean("fooDataSourceProperties",
					DataSourceProperties.class);
			DataSourceProperties barDataSourceProperties = ctx.getBean("barDataSourceProperties",
					DataSourceProperties.class);
			assertThat(fooDataSourceProperties.getDriverClassName())
				.isEqualTo(dataSourceProperties.getDriverClassName());
			assertThat(barDataSourceProperties.getDriverClassName())
				.isEqualTo(dataSourceProperties.getDriverClassName());
			assertThat(dataSourceProperties.getUrl()).endsWith("default");
			assertThat(fooDataSourceProperties.getUrl()).endsWith("foo");
			assertThat(barDataSourceProperties.getUrl()).endsWith("bar");
		});
	}

	@Test
	void testDataSourceConnectionDetails() {
		runner.run((ctx) -> {
			JdbcConnectionDetails jdbcConnectionDetails = ctx.getBean(JdbcConnectionDetails.class);
			JdbcConnectionDetails fooJdbcConnectionDetails = ctx.getBean("fooJdbcConnectionDetails",
					JdbcConnectionDetails.class);
			JdbcConnectionDetails barJdbcConnectionDetails = ctx.getBean("barJdbcConnectionDetails",
					JdbcConnectionDetails.class);
			assertThat(jdbcConnectionDetails.getJdbcUrl()).endsWith("default");
			assertThat(fooJdbcConnectionDetails.getJdbcUrl()).endsWith("foo");
			assertThat(barJdbcConnectionDetails.getJdbcUrl()).endsWith("bar");
		});
	}

	@Test
	void testDataSource() throws SQLException {
		runner.run((ctx) -> {
			DataSource dataSource = ctx.getBean(DataSource.class);
			DataSource fooDataSource = ctx.getBean("fooDataSource", DataSource.class);
			DataSource barDataSource = ctx.getBean("barDataSource", DataSource.class);
			assertThat(dataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
				assertThat(ds.getPoolName()).isEqualTo("spring");
				assertThat(ds.getMinimumIdle()).isEqualTo(10);
				assertThat(ds.getMaximumPoolSize()).isEqualTo(20);
			});
			assertThat(fooDataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
				assertThat(ds.getPoolName()).isEqualTo("foo");
				assertThat(ds.getMinimumIdle()).isEqualTo(20);
				assertThat(ds.getMaximumPoolSize()).isEqualTo(40);
			});
			assertThat(barDataSource).isInstanceOfSatisfying(HikariDataSource.class, (ds) -> {
				assertThat(ds.getPoolName()).isEqualTo("bar");
				assertThat(ds.getMinimumIdle()).isEqualTo(30);
				assertThat(ds.getMaximumPoolSize()).isEqualTo(60);
			});

			try (Connection connection = dataSource.getConnection()) {
				assertThat(connection.getCatalog()).isEqualTo("default".toUpperCase(Locale.ROOT));
			}
			try (Connection connection = fooDataSource.getConnection()) {
				assertThat(connection.getCatalog()).isEqualTo("foo".toUpperCase(Locale.ROOT));
			}
			try (Connection connection = barDataSource.getConnection()) {
				assertThat(connection.getCatalog()).isEqualTo("bar".toUpperCase(Locale.ROOT));
			}
		});
	}

	@Test
	void testJdbcTransactionManager() {
		runner.run((ctx) -> {
			PlatformTransactionManager transactionManager = ctx.getBean(PlatformTransactionManager.class);
			PlatformTransactionManager fooTransactionManager = ctx.getBean("fooTransactionManager",
					PlatformTransactionManager.class);
			PlatformTransactionManager barTransactionManager = ctx.getBean("barTransactionManager",
					PlatformTransactionManager.class);
			assertThat(fooTransactionManager).isNotSameAs(transactionManager);
			assertThat(barTransactionManager).isNotSameAs(transactionManager);
			assertThat(fooTransactionManager).isNotSameAs(barTransactionManager);
		});
	}

	@Test
	void testJdbcProperties() {
		runner.run((ctx) -> {
			JdbcProperties jdbcProperties = ctx.getBean(JdbcProperties.class);
			JdbcProperties fooJdbcProperties = ctx.getBean("fooJdbcProperties", JdbcProperties.class);
			JdbcProperties barJdbcProperties = ctx.getBean("barJdbcProperties", JdbcProperties.class);
			assertThat(fooJdbcProperties.getTemplate().getFetchSize())
				.isEqualTo(jdbcProperties.getTemplate().getFetchSize());
			assertThat(barJdbcProperties.getTemplate().getFetchSize())
				.isEqualTo(jdbcProperties.getTemplate().getFetchSize());
			assertThat(jdbcProperties.getTemplate().getMaxRows()).isEqualTo(1000);
			assertThat(fooJdbcProperties.getTemplate().getMaxRows()).isEqualTo(2000);
			assertThat(barJdbcProperties.getTemplate().getMaxRows()).isEqualTo(3000);
		});
	}

	@Test
	void testJdbcTemplate() {
		runner.run((ctx) -> {
			JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
			JdbcTemplate fooJdbcTemplate = ctx.getBean("fooJdbcTemplate", JdbcTemplate.class);
			JdbcTemplate barJdbcTemplate = ctx.getBean("barJdbcTemplate", JdbcTemplate.class);
			DataSource dataSource = ctx.getBean(DataSource.class);
			DataSource fooDataSource = ctx.getBean("fooDataSource", DataSource.class);
			DataSource barDataSource = ctx.getBean("barDataSource", DataSource.class);
			assertThat(jdbcTemplate.getDataSource()).isSameAs(dataSource);
			assertThat(fooJdbcTemplate.getDataSource()).isSameAs(fooDataSource);
			assertThat(barJdbcTemplate.getDataSource()).isSameAs(barDataSource);
		});
	}

	@Test
	void testNamedParameterJdbcTemplate() {
		runner.run((ctx) -> {
			NamedParameterJdbcTemplate namedParameterJdbcTemplate = ctx.getBean(NamedParameterJdbcTemplate.class);
			NamedParameterJdbcTemplate fooNamedParameterJdbcTemplate = ctx.getBean("fooNamedParameterJdbcTemplate",
					NamedParameterJdbcTemplate.class);
			NamedParameterJdbcTemplate barNamedParameterJdbcTemplate = ctx.getBean("barNamedParameterJdbcTemplate",
					NamedParameterJdbcTemplate.class);
			JdbcTemplate jdbcTemplate = ctx.getBean(JdbcTemplate.class);
			JdbcTemplate fooJdbcTemplate = ctx.getBean("fooJdbcTemplate", JdbcTemplate.class);
			JdbcTemplate barJdbcTemplate = ctx.getBean("barJdbcTemplate", JdbcTemplate.class);
			assertThat(namedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(jdbcTemplate);
			assertThat(fooNamedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(fooJdbcTemplate);
			assertThat(barNamedParameterJdbcTemplate.getJdbcTemplate()).isSameAs(barJdbcTemplate);
		});
	}

	@Test
	void testJdbcClient() {
		runner.run((ctx) -> {
			JdbcClient jdbcClient = ctx.getBean(JdbcClient.class);
			JdbcClient fooJdbcClient = ctx.getBean("fooJdbcClient", JdbcClient.class);
			JdbcClient barJdbcClient = ctx.getBean("barJdbcClient", JdbcClient.class);
			assertThat(jdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
				.isEqualTo("default".toUpperCase(Locale.ROOT));
			assertThat(fooJdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
				.isEqualTo("foo".toUpperCase(Locale.ROOT));
			assertThat(barJdbcClient.sql("SELECT CURRENT_CATALOG()").query().singleValue())
				.isEqualTo("bar".toUpperCase(Locale.ROOT));
		});
	}

}
