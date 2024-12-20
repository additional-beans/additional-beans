package io.additionalbeans.redis.lettuce;

import io.additionalbeans.redis.RedisConfigurationIntegrationTestBase;

import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@Import({ FooRedisConfiguration.class, BarRedisConfiguration.class })
class LettuceConfigurationIntegrationTests extends RedisConfigurationIntegrationTestBase {

}
