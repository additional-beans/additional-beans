package io.additionalbeans.jdbc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import io.additionalbeans.commons.AdditionalBeansPostProcessor;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcClientAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.autoconfigure.jdbc.JdbcProperties;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.jdbc.HikariCheckpointRestoreLifecycle;
import org.springframework.core.env.Environment;
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
					String namePrefix = this.defaultConfigurationPropertiesPrefix.replace("spring", prefix) + '.';
					Bindable<?> bindable = Bindable.ofInstance(bean);
					switch (type) {
						case HIKARI_DATASOURCE_CLASS_NAME -> {
							this.binder.bind(namePrefix + "hikari", bindable);
							String name = this.environment.getProperty(namePrefix + "name");
							if (!StringUtils.hasText(name)) {
								name = prefix;
							}
							((HikariDataSource) bean).setPoolName(name);
						}
						case DBCP2_DATASOURCE_CLASS_NAME -> this.binder.bind(namePrefix + "dbcp2", bindable);
						case TOMCAT_DATASOURCE_CLASS_NAME -> this.binder.bind(namePrefix + "tomcat", bindable);
						case ORACLE_UCP_DATASOURCE_CLASS_NAME -> this.binder.bind(namePrefix + "oracleucp", bindable);
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
		registerBeanInstanceSupplier(registry, JdbcProperties.class, prefix, () -> {
			JdbcProperties properties = new JdbcProperties();
			JdbcProperties defaultRedisProperties = this.applicationContext
				.getBean("%s-%s".formatted(SPRING_JDBC_PREFIX, JdbcProperties.class.getName()), JdbcProperties.class);
			BeanUtils.copyProperties(defaultRedisProperties, properties);
			return properties;
		});
	}

	private void registerDataSource(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, DataSource.class, prefix,
				() -> beanFor(DataSourceProperties.class, prefix).initializeDataSourceBuilder().build());
	}

	private void registerDataSourceTransactionManager(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, TransactionManager.class, prefix, () -> {
			try {
				Class<?> jdbcTransactionManagerConfiguration = DataSourceTransactionManagerAutoConfiguration.class
					.getDeclaredClasses()[0];
				Constructor<?> ctor = jdbcTransactionManagerConfiguration.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object configuration = ctor.newInstance();
				Method method = jdbcTransactionManagerConfiguration.getDeclaredMethod("transactionManager",
						Environment.class, DataSource.class, ObjectProvider.class);
				method.setAccessible(true);
				return (TransactionManager) method.invoke(configuration, this.environment,
						beanFor(DataSource.class, prefix), beanProviderOf(TransactionManagerCustomizers.class));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerJdbcTemplate(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, JdbcTemplate.class, prefix, () -> {
			try {
				Class<?> clazz = ClassUtils.forName(
						DataSourceAutoConfiguration.class.getPackageName() + ".JdbcTemplateConfiguration",
						DataSourceAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = clazz.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object configuration = ctor.newInstance();
				Method method = clazz.getDeclaredMethod("jdbcTemplate", DataSource.class, JdbcProperties.class);
				method.setAccessible(true);
				return (JdbcTemplate) method.invoke(configuration, beanFor(DataSource.class, prefix),
						beanFor(JdbcProperties.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});

		registerBeanInstanceSupplier(registry, NamedParameterJdbcTemplate.class, prefix, () -> {
			try {
				Class<?> clazz = ClassUtils.forName(
						DataSourceAutoConfiguration.class.getPackageName() + ".NamedParameterJdbcTemplateConfiguration",
						DataSourceAutoConfiguration.class.getClassLoader());
				Constructor<?> ctor = clazz.getDeclaredConstructor();
				ctor.setAccessible(true);
				Object configuration = ctor.newInstance();
				Method method = clazz.getDeclaredMethod("namedParameterJdbcTemplate", JdbcTemplate.class);
				method.setAccessible(true);
				return (NamedParameterJdbcTemplate) method.invoke(configuration, beanFor(JdbcTemplate.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerJdbcClient(BeanDefinitionRegistry registry, String prefix) {
		registerBeanInstanceSupplier(registry, JdbcClient.class, prefix, () -> {
			JdbcClientAutoConfiguration configuration = new JdbcClientAutoConfiguration();
			try {
				Method method = JdbcClientAutoConfiguration.class.getDeclaredMethod("jdbcClient",
						NamedParameterJdbcTemplate.class);
				method.setAccessible(true);
				return (JdbcClient) method.invoke(configuration, beanFor(NamedParameterJdbcTemplate.class, prefix));
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	private void registerHikariCheckpointRestoreLifecycle(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, HikariCheckpointRestoreLifecycle.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(DataSource.class, prefix)));
			arguments.addGenericArgumentValue(this.applicationContext);
			beanDefinition.setConstructorArgumentValues(arguments);
			return beanDefinition;
		});
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
