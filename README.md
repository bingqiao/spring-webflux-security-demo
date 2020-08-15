# Spring Webflux Security Demo

Two projects are included here to demo how to implement some typical use cases of OAuth2 via  
Spring Security 5 and Webflux.

A **legacy** Spring Boot service using `Spring Security OAuth2` has the following features:
* Acting as a resource server protected by JWT Bearer token issued by authorization server A
* Holding a client_credentials `OAuth2RestTemplate` to access external resource servers  
protected by JWT Bearer token issued by authorization server B
* Extension to `DefaultAccessTokenConverter` to add custom authorities to authentication object
* Method security to allow use of method access-control annotations such as `PreAuthorize`
* Parsing `hateoas` responses

It wasn't an easy path migrating this service to Spring Security 5. There are still a few other  
things yet to be figured out. But at least, after a lot of searching, experimenting, and frustrating,  
I now have a project that does all above.

The setup of these projects exclude `tomcat` and thus uses `netty`. I'm still not certain if this is  
the only way to get `Spring Webflux` to play nice with `Spring Security 5`. I started with   
various sample projects from others, some of which use `Spring Boot starter web` and have  
the default `tomcat` compared to others using `netty` by excluding `tomcat` dependencies. I  
however ended up with this particular setup that finally worked.

## resourceserver

This project is relatively straightforward.

### application.yml

```
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${AUTH_SERVER_B}
```

### build.gradle

```
implementation('org.springframework.boot:spring-boot-starter-hateoas') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-web'
}
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
implementation('org.springframework.boot:spring-boot-starter-web') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

### ServerHttpSecurityConfig

```
@EnableWebFluxSecurity
public class ServerHttpSecurityConfig {
    @Bean
    protected SecurityWebFilterChain configure(ServerHttpSecurity http) {
        return http.authorizeExchange(authz -> authz
                .anyExchange().authenticated())
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt)
                .build();
    }
}
```

## resourceserver-webclient

This project implements `Resource Server`, client_credentials `WebClient`, `Method Security`,  
custom `JwtGrantedAuthoritiesConverter` and `JwtBearerTokenAuthenticationConverter`.

### application.yml

```
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          # the following is for site authentication
          issuer-uri: ${AUTH_SERVER_A}
      # the following is for webclient. the authorization server is the same
      # as the one configured for external resourceserver
      client:
        registration:
          custom:
            authorization-grant-type: client_credentials
            client-id: ${CLIENT_ID_FROM_AUTH_SERVER_B}
            client-secret: ${CLIENT_SECRET_FROM_AUTH_SERVER_B}
            scope: demo-external
            provider: customprovider
        provider:
          customprovider:
            token-uri: ${TOKEN_URI_OF_AUTH_SERVER_FOR_B}
```

### build.gradle

```
implementation('org.springframework.boot:spring-boot-starter-hateoas') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-web'
}
implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
implementation 'org.springframework.boot:spring-boot-starter-security'
implementation('org.springframework.boot:spring-boot-starter-web') {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
implementation 'org.springframework.boot:spring-boot-starter-webflux'
```

### ServerHttpSecurityConfig

```
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
    }
}
```

### WebClientCOnfig

This is probably the most troublesome one. Most examples I gleaned from internet have the following code:

```
@Bean
WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
            new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                    clientRegistrations,
                    new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
    oauth.setDefaultClientRegistrationId("custom");
    return WebClient.builder()
            .filter(oauth)
            .build();
}
```

Which gives error *The user org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication*

The following works:

```
@Configuration
public class WebClientConfig {
    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId("custom");
        return WebClient.builder().filter(oauth).build();
    }
}
```

### ServerHttpSecurityConfig

```
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
}
```

### Method Security

```
@EnableReactiveMethodSecurity
public class MethodSecurityConfig {
}
```

