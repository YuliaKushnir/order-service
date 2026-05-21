package org.example.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/orders/**")
                        .hasAnyAuthority("ROLE_CLIENT", "ROLE_MANAGER", "ROLE_EXECUTOR", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/orders/**")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_EXECUTOR", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/api/orders/**")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_EXECUTOR", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/api/orders/**")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/orders")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_EXECUTOR", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/orders/user/**")
                        .authenticated()

                        .requestMatchers(HttpMethod.GET, "/api/orders/**")
                        .authenticated()

                        .requestMatchers("/actuator/**").permitAll()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null) {
                return List.of();
            }

            Map<String, Object> client = (Map<String, Object>) resourceAccess.get("gateway-client");
            if (client == null || client.get("roles") == null) {
                return List.of();
            }

            List<String> roles = (List<String>) client.get("roles");
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();
        });

        return converter;
    }
}