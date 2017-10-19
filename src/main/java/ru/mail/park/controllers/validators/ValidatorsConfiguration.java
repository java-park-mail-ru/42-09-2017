package ru.mail.park.controllers.validators;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidatorsConfiguration {
    @Bean
    public UsernameValidator usernameValidator() {
        return new UsernameValidator();
    }

    @Bean
    public EmailValidator emailValidator() {
        return new EmailValidator();
    }

    @Bean
    public PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }

    @Bean
    public OldPasswordValidator oldPasswordValidator() {
        return new OldPasswordValidator();
    }
}
