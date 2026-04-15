package com.example.config;

import org.aeonbits.owner.Config;

@Config.Sources({"classpath:config-${env}.properties"})
public interface EnvironmentConfig extends Config {
    @Key("baseUrl")
    String baseUrl();
}
