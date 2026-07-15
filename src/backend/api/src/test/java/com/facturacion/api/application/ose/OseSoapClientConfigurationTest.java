package com.facturacion.api.application.ose;

import static org.assertj.core.api.Assertions.assertThat;

import com.facturacion.api.web.config.RestTemplateConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OseSoapClientConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(RestTemplateConfig.class, OseSoapClient.class);

    @Test
    void applicationYamlCreatesOseSoapClientWhenOseEnvironmentIsAbsent() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(OseSoapClient.class));
    }
}
