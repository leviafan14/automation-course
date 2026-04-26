package com.example.config;

import java.io.IOException;
import java.util.Properties;

public class AuthTestConfig {
    private static final Properties properties = new Properties();

    static {
        try (var inputStream = AuthTestConfig.class.getClassLoader().getResourceAsStream("login.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл login.properties не найден в resources");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки login.properties", e);
        }
    }


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getUrlProperty(){
        String baseUrl = properties.getProperty("baseUrl");
        if (baseUrl == null){
            return "Ссылка на страницу не получена из properties файла!";
        }
        return baseUrl;
    }
    public static String getLoginProperty(){
        String login = (String) properties.get("validLogin");
        return login;
    }

    public static String getPasswordProperty(){
        String password = (String) properties.get("validPassword");
        return password;
    }

}
