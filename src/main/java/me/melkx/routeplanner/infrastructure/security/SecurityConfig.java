package me.melkx.routeplanner.infrastructure.security;

import me.melkx.common.security.AuthorizeRequestsCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtValidityProperties.class)
public class SecurityConfig {
    @Bean
    public AuthorizeRequestsCustomizer allowedRequests() {
        return customizer -> customizer.requestMatchers("/api/v1/auth/**")
                .permitAll()
                .anyRequest()
                .authenticated();
    }
}
