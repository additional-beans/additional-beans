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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Yanming Zhou
 */
public class AdditionalJdbcPostProcessor extends AdditionalBeansPostProcessor {

	public static final String KEY_ADDITIONAL_JDBC_PREFIXES = "additional.jdbc.prefixes";

	private static final String SPRING_DATASOURCE_PREFIX = "spring.datasource";

	private static final String SPRING_JDBC_PREFIX = "spring.jdbc";

	private static final String HIKARI_DATASOURCE_CLASS_NAME = "com.zaxxer.hikari.HikariDataSource";

	private static final String DBCP2_DATASOURCE_CLASS_NAME = "org.apache.commons.dbcp2.BasicDataSource";

	private static final String TOMCAT_DATASOURCE_CLASS_NAME = "org.apache.tomcat.jdbc.pool.DataSource";

	private static final String ORACLE_UCP_DATASOURCE_CLASS_NAME = "oracle.ucp.jdbc.PoolDataSourceImpl";

	@Override
	protected String configurationKeyForPrefixes() {
		return KEY_ADDITIONAL_JDBC_PREFIXES;
	}

	@Override
	protected void registerBeanDefinitionsForPrefix(BeanDefinitionRegistry registry, String prefix) {
		registerDataSourceProperties(registry, prefix);
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
		if (bean instanceof DataSourceProperties) {
			String suffix = DataSourceProperties.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					this.binder.bind(SPRING_DATASOURCE_PREFIX.replace("spring", prefix), Bindable.ofInstance(bean));
				}
			}
		}
		else if (bean instanceof DataSource) {
			String suffix = DataSource.class.getSimpleName();
			if (beanName.endsWith(suffix)) {
				String prefix = beanName.substring(0, beanName.length() - suffix.length());
				if (this.prefixes.contains(prefix)) {
					String type = bean.getClass().getName();
					String namePrefix = SPRING_DATASOURCE_PREFIX.replace("spring", prefix) + '.';
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

	private void registerDataSourceProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, DataSourceProperties.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setTargetType(DataSourceProperties.class);
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setInstanceSupplier(() -> {
				DataSourceProperties properties = new DataSourceProperties();
				DataSourceProperties defaultRedisProperties = this.applicationContext.getBean(
						"%s-%s".formatted(SPRING_DATASOURCE_PREFIX, DataSourceProperties.class.getName()),
						DataSourceProperties.class);
				BeanUtils.copyProperties(defaultRedisProperties, properties);
				return properties;
			});
			return beanDefinition;
		});

		registerBeanDefinition(registry, JdbcConnectionDetails.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition
				.setBeanClassName(DataSourceProperties.class.getPackageName() + ".PropertiesJdbcConnectionDetails");
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments
				.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(DataSourceProperties.class, prefix)));
			beanDefinition.setConstructorArgumentValues(arguments);
			beanDefinition.setTargetType(JdbcConnectionDetails.class);
			return beanDefinition;
		});
	}

	private void registerJdbcProperties(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, JdbcProperties.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setTargetType(JdbcProperties.class);
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setInstanceSupplier(() -> {
				JdbcProperties properties = new JdbcProperties();
				JdbcProperties defaultRedisProperties = this.applicationContext.getBean(
						"%s-%s".formatted(SPRING_JDBC_PREFIX, JdbcProperties.class.getName()), JdbcProperties.class);
				BeanUtils.copyProperties(defaultRedisProperties, properties);
				return properties;
			});
			return beanDefinition;
		});
	}

	private void registerDataSource(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, DataSource.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setTargetType(DataSource.class);
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setInstanceSupplier(() -> this.applicationContext
				.getBean(beanNameFor(DataSourceProperties.class, prefix), DataSourceProperties.class)
				.initializeDataSourceBuilder()
				.build());
			return beanDefinition;
		});
	}

	private void registerDataSourceTransactionManager(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, TransactionManager.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setTargetType(DataSourceTransactionManager.class);
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setInstanceSupplier(() -> {
				try {
					Class<?> jdbcTransactionManagerConfiguration = DataSourceTransactionManagerAutoConfiguration.class
						.getDeclaredClasses()[0];
					Constructor<?> ctor = jdbcTransactionManagerConfiguration.getDeclaredConstructor();
					ctor.setAccessible(true);
					Object configuration = ctor.newInstance();
					Method method = jdbcTransactionManagerConfiguration.getDeclaredMethod("transactionManager",
							Environment.class, DataSource.class, ObjectProvider.class);
					method.setAccessible(true);
					return method.invoke(configuration, this.environment,
							this.applicationContext.getBean(beanNameFor(DataSource.class, prefix), DataSource.class),
							this.applicationContext.getBeanProvider(TransactionManagerCustomizers.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			return beanDefinition;
		});
	}

	private void registerJdbcTemplate(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, JdbcTemplate.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(JdbcTemplate.class);
			beanDefinition.setInstanceSupplier(() -> {
				try {
					Class<?> clazz = ClassUtils.forName(
							DataSourceAutoConfiguration.class.getPackageName() + ".JdbcTemplateConfiguration",
							DataSourceAutoConfiguration.class.getClassLoader());
					Constructor<?> ctor = clazz.getDeclaredConstructor();
					ctor.setAccessible(true);
					Object configuration = ctor.newInstance();
					Method method = clazz.getDeclaredMethod("jdbcTemplate", DataSource.class, JdbcProperties.class);
					method.setAccessible(true);
					return method.invoke(configuration,
							this.applicationContext.getBean(beanNameFor(DataSource.class, prefix), DataSource.class),
							this.applicationContext.getBean(beanNameFor(JdbcProperties.class, prefix),
									JdbcProperties.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			return beanDefinition;
		});

		registerBeanDefinition(registry, NamedParameterJdbcTemplate.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(NamedParameterJdbcTemplate.class);
			beanDefinition.setInstanceSupplier(() -> {
				try {
					Class<?> clazz = ClassUtils.forName(
							DataSourceAutoConfiguration.class.getPackageName()
									+ ".NamedParameterJdbcTemplateConfiguration",
							DataSourceAutoConfiguration.class.getClassLoader());
					Constructor<?> ctor = clazz.getDeclaredConstructor();
					ctor.setAccessible(true);
					Object configuration = ctor.newInstance();
					Method method = clazz.getDeclaredMethod("namedParameterJdbcTemplate", JdbcTemplate.class);
					method.setAccessible(true);
					return method.invoke(configuration, this.applicationContext
						.getBean(beanNameFor(JdbcTemplate.class, prefix), JdbcTemplate.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			return beanDefinition;
		});
	}

	private void registerJdbcClient(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, JdbcClient.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setTargetType(JdbcClient.class);
			beanDefinition.setInstanceSupplier(() -> {
				JdbcClientAutoConfiguration configuration = new JdbcClientAutoConfiguration();
				try {
					Method method = JdbcClientAutoConfiguration.class.getDeclaredMethod("jdbcClient",
							NamedParameterJdbcTemplate.class);
					method.setAccessible(true);
					return method.invoke(configuration, this.applicationContext.getBean(
							beanNameFor(NamedParameterJdbcTemplate.class, prefix), NamedParameterJdbcTemplate.class));
				}
				catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			return beanDefinition;
		});
	}

	private void registerHikariCheckpointRestoreLifecycle(BeanDefinitionRegistry registry, String prefix) {
		registerBeanDefinition(registry, HikariCheckpointRestoreLifecycle.class, prefix, () -> {
			RootBeanDefinition beanDefinition = new RootBeanDefinition();
			beanDefinition.setDefaultCandidate(false);
			beanDefinition.setBeanClass(HikariCheckpointRestoreLifecycle.class);
			ConstructorArgumentValues arguments = new ConstructorArgumentValues();
			arguments.addGenericArgumentValue(new RuntimeBeanReference(beanNameFor(DataSource.class, prefix)));
			arguments.addGenericArgumentValue(this.applicationContext);
			beanDefinition.setConstructorArgumentValues(arguments);
			return beanDefinition;
		});
	}

	private boolean useHikariFor(String prefix) {
		String suffix = ".type";
		String type = this.environment.getProperty(SPRING_DATASOURCE_PREFIX.replace("spring", prefix) + suffix,
				this.environment.getProperty(SPRING_DATASOURCE_PREFIX + suffix));
		return HIKARI_DATASOURCE_CLASS_NAME.equals(type) || type == null && ClassUtils
			.isPresent(HIKARI_DATASOURCE_CLASS_NAME, DataSourceAutoConfiguration.class.getClassLoader());
	}

}
