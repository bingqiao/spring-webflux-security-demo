package com.bqiao.demo.oauth2.resourceserverwebclient.config;

import com.bqiao.demo.oauth2.resourceserverwebclient.config.jwt.CustomJwtBearerTokenAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
public class ServerHttpSecurityConfig {
    @Bean
    protected SecurityWebFilterChain configure(ServerHttpSecurity http) {
        CustomJwtBearerTokenAuthenticationConverter jwtBearerToeknAuthConverter =
                new CustomJwtBearerTokenAuthenticationConverter();
        Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> monoConverter =
                new ReactiveJwtAuthenticationConverterAdapter(jwtBearerToeknAuthConverter);

        return http.authorizeExchange(authz -> authz
                .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(monoConverter))
                .build();
    }}
