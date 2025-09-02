package io.additionalbeans.jdbc;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@Import(AdditionalJdbcPostProcessor.class)
public class AdditionalJdbcAutoConfiguration {

}
