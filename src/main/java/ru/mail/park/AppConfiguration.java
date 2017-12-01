package ru.mail.park;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableTransactionManagement
public class AppConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public VkApiClient getVkApiClient() {
        TransportClient transportClient = HttpTransportClient.getInstance();
        return new VkApiClient(transportClient);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:8000");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://physicsio.tech");
        config.addAllowedOrigin("http://physicsio.tech:443");
        config.addAllowedOrigin("https://physicsio.tech");
        config.addAllowedOrigin("http://194.87.110.17:443");
        config.addAllowedOrigin("https://194.87.110.17");
        config.addAllowedOrigin("https://194.87.110.17:443");
        config.addAllowedHeader("*");
        config.addAllowedHeader("Content-Type");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/api/auth/**", config);
        source.registerCorsConfiguration("/api/game/**", config);
        return new CorsFilter(source);
    }
}
