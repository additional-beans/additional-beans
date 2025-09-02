package io.additionalbeans.redis;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@Import(AdditionalRedisPostProcessor.class)
public class AdditionalRedisAutoConfiguration {

}
