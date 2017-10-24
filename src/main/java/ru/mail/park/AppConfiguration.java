package ru.mail.park;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.mail.park.controllers.domain.User;
import ru.mail.park.dto.UserDTO;

@Configuration
@EnableTransactionManagement
public class AppConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper
                .createTypeMap(UserDTO.class, User.class)
                .addMappings(mapper -> mapper.skip(User::setId));
        return modelMapper;
    }
}
