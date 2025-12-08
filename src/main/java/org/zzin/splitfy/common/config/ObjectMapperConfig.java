package org.zzin.splitfy.common.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;

@Configuration
public class ObjectMapperConfig {

  @Bean
  public JsonMapperBuilderCustomizer customizer() {
    return builder -> builder.enable(SerializationFeature.INDENT_OUTPUT);
  }
}
