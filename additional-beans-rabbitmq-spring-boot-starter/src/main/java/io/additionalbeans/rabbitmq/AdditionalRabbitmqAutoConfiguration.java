package io.additionalbeans.rabbitmq;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@Import(AdditionalRabbitmqPostProcessor.class)
public class AdditionalRabbitmqAutoConfiguration {

}
