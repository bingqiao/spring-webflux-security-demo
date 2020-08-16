package com.bqiao.demo.oauth2.resourceserverwebclient.config;

import com.bqiao.demo.oauth2.resourceserverwebclient.config.jwt.CustomJwtBearerTokenAuthenticationConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class ServerHttpSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CustomJwtBearerTokenAuthenticationConverter jwtBearerToeknAuthConverter =
                new CustomJwtBearerTokenAuthenticationConverter();
        http.cors()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt().jwtAuthenticationConverter(jwtBearerToeknAuthConverter);
    }}
