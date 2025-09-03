package io.additionalbeans.jdbc;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.jdbc.HikariCheckpointRestoreLifecycle;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcClientAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcConnectionDetails;
import org.springframework.boot.jdbc.autoconfigure.JdbcProperties;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.TransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Yanming Zhou
 */
public class AdditionalJdbcPostProcessor
		extends AdditionalBeansPostProcessor<DataSourceProperties, JdbcConnectionDetails> {

	private static final String SPRING_JDBC_PREFIX = "spring.jdbc";

	private static final String HIKARI_DATASOURCE_CLASS_NAME = "com.zaxxer.hikari.HikariDataSource";

	private static final String DBCP2_DATASOURCE_CLASS_NAME = "org.apache.commons.dbcp2.BasicDataSource";

	private static final String TOMCAT_DATASOURCE_CLASS_NAME = "org.apache.tomcat.jdbc.pool.DataSource";

	private static final String ORACLE_UCP_DATASOURCE_CLASS_NAME = "oracle.ucp.jdbc.PoolDataSourceImpl";

	@Override
	protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String prefix) {
		registerDataSource(registry, prefix);
		registerDataSourceTransactionManager(registry, prefix);
		registerJdbcProperties(registry, prefix);
		registerJdbcTemplate(registry, prefix);
		registerJdbcClient(registry, prefix);
		if (useHikariFor(prefix)
				&& ClassUtils.isPresent("org.crac.Resource", DataSourceAutoConfiguration.class.getClassLoader())) {
			registerHikariCheckpointRestoreLifecycle(registry, prefix);
		}
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		bean = super.postProcessBeforeInitialization(bean, beanName);
		if (bean instanceof DataSource) {
			String suffix = DataSource.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					String type = bean.getClass().getName();
					String defaultPrefix = this.defaultConfigurationPropertiesPrefix + '.';
					String namePrefix = this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + '.';
					Bindable<?> bindable = Bindable.ofInstance(bean);
					switch (type) {
						case HIKARI_DATASOURCE_CLASS_NAME -> {
							this.binder.bind(defaultPrefix + "hikari", bindable);
							this.binder.bind(namePrefix + "hikari", bindable);
							String name = this.environment.getProperty(namePrefix + "name");
							if (!StringUtils.hasText(name)) {
								name = prefix;
							}
							((HikariDataSource) bean).setPoolName(name);
						}
						case DBCP2_DATASOURCE_CLASS_NAME -> {
							this.binder.bind(defaultPrefix + "dbcp2", bindable);
							this.binder.bind(namePrefix + "dbcp2", bindable);
						}
						case TOMCAT_DATASOURCE_CLASS_NAME -> {
							this.binder.bind(defaultPrefix + "tomcat", bindable);
							this.binder.bind(namePrefix + "tomcat", bindable);
						}
						case ORACLE_UCP_DATASOURCE_CLASS_NAME -> {
							this.binder.bind(defaultPrefix + "oracleucp", bindable);
							this.binder.bind(namePrefix + "oracleucp", bindable);
						}
					}

				}
			}
		}
		else if (bean instanceof JdbcProperties) {
			String suffix = JdbcProperties.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					this.binder.bind(SPRING_JDBC_PREFIX.replace("spring", prefix), Bindable.ofInstance(bean));
				}
			}
		}
		return bean;
	}

	private void registerJdbcProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, JdbcProperties.class, prefix);
	}

	private void registerDataSource(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, DataSource.class, prefix,
				() -> configurationPropertiesBean(prefix).initializeDataSourceBuilder().build());
	}

	private void registerDataSourceTransactionManager(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				DataSourceTransactionManagerAutoConfiguration.class.getName() + ".JdbcTransactionManagerConfiguration",
				prefix);
		registerBeanDefinition(registry, TransactionManager.class, prefix, configurationBeanName, "transactionManager");
	}

	private void registerJdbcTemplate(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry,
				JdbcTemplateAutoConfiguration.class.getPackageName() + ".JdbcTemplateConfiguration", prefix);
		registerBeanDefinition(registry, JdbcTemplate.class, prefix, configurationBeanName, "jdbcTemplate");
		configurationBeanName = registerBeanDefinition(registry,
				JdbcTemplateAutoConfiguration.class.getPackageName() + ".NamedParameterJdbcTemplateConfiguration",
				prefix);
		registerBeanDefinition(registry, NamedParameterJdbcTemplate.class, prefix, configurationBeanName,
				"namedParameterJdbcTemplate");
	}

	private void registerJdbcClient(BeanDefinitionRegistry registry, String prefix) {
		String configurationBeanName = registerBeanDefinition(registry, JdbcClientAutoConfiguration.class, prefix);
		registerBeanDefinition(registry, JdbcClient.class, prefix, configurationBeanName, "jdbcClient");
	}

	private void registerHikariCheckpointRestoreLifecycle(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, HikariCheckpointRestoreLifecycle.class, prefix);
	}

	private boolean useHikariFor(String prefix) {
		String suffix = ".type";
		String type = this.environment.getProperty(
				this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + suffix,
				this.environment.getProperty(this.defaultConfigurationPropertiesPrefix + suffix));
		return HIKARI_DATASOURCE_CLASS_NAME.equals(type) || type == null && ClassUtils
			.isPresent(HIKARI_DATASOURCE_CLASS_NAME, DataSourceAutoConfiguration.class.getClassLoader());
	}

}
