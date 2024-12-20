package io.additionalbeans.redis.jedis;

import io.additionalbeans.redis.RedisConfigurationIntegrationTestBase;

import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@Import({ FooRedisConfiguration.class, BarRedisConfiguration.class })
class JedisConfigurationIntegrationTests extends RedisConfigurationIntegrationTestBase {

}
