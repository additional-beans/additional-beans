package io.additionalbeans.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@Import(AdditionalKafkaPostProcessor.class)
public class AdditionalKafkaAutoConfiguration {

}
