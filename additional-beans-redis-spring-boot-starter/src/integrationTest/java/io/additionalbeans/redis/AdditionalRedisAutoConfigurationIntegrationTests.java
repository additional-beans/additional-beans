package io.additionalbeans.redis;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author Yanming Zhou
 */
@TestPropertySource(properties = AdditionalRedisPostProcessor.KEY_ADDITIONAL_REDIS_PREFIXES + "=foo,bar")
@ImportAutoConfiguration(AdditionalRedisAutoConfiguration.class)
class AdditionalRedisAutoConfigurationIntegrationTests extends RedisConfigurationIntegrationTestBase {

}
