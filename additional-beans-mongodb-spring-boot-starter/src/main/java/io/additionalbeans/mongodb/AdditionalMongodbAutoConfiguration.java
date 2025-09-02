package io.additionalbeans.mongodb;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author Yanming Zhou
 */
@AutoConfiguration
@Import(AdditionalMongodbPostProcessor.class)
public class AdditionalMongodbAutoConfiguration {

}
