package com.bqiao.demo.oauth2.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class ServerHttpSecurityConfig {
    @Bean
    protected SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange(authz -> authz
                .anyExchange().authenticated())
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                .build();
    }}
