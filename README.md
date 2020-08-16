# Spring Webflux Security Demo

Four projects are included here to demo how to implement some typical use cases of OAuth2 via  
Spring Security 5 for both Reactive and Servlet stacks.

A **legacy** Spring Boot service using `Spring Security OAuth2` has the following features:
* Acting as a resource server protected by JWT Bearer token issued by authorization server A
* Holding a client_credentials `OAuth2RestTemplate` to access external resource servers  
protected by JWT Bearer token issued by authorization server B
* Extension to `DefaultAccessTokenConverter` to add custom authorities to authentication object
* Method security to allow use of method access-control annotations such as `PreAuthorize`
* Parsing `hateoas` responses

It wasn't an easy path migrating this service to Spring Security 5. But after a lot of searching,  
experimenting, and frustrating, I now have two sets of projects that work for all above.

The major mistake I made was to mix Servlet and Reactive stacks in Spring. Spring `WebClient`  
can be used in both stacks but which stack to use has implications on what and how to configure  
your Spring Beans.

The two servlet projects are as follows:
* resourceserver-tomcat
* resourceserver-webclient-tomcat

The two reactive projects are as follows:
* resourceserver
* resourceserver-webclient

The matrix below shows differences setting up those two sets of projects.

|                        | Reactive                                                                                                                                                                                                              | Servlet                                                                                                                                                                                                                             |
|------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Dependencies           | Exclude spring-boot-starter-tomcat from spring-boot-starter-web                                                                                                                                                       |                                                                                                                                                                                                                                     |
| Security Configuration | <ul><li>Apply @EnableWebFluxSecurity</li> <li>Configure @Bean SecurityWebFilterChain   that takes ServerHttpSecurity</li><ul>                                                                                         | <ul><li>Apply @Configuration</li> <li>@Override WebSecurityConfigurerAdapter.configure</li></ul>                                                                                                                                    |
| Method Security        | Apply @EnableReactiveMethodSecurity                                                                                                                                                                                   | Apply @EnableGlobalMethodSecurity                                                                                                                                                                                                   |
| WebClient              | <ul><li>Configure @Bean WebClient</li> <li>Configurate @Bean ReactiveOAuth2AuthorizedClientManager that <br> uses injected ReactiveClientRegistrationRepository and   ReactiveOAuth2AuthorizedClientService</li></ul> | <ul><li>Configure @Bean WebClient</li> <li>Configure @Bean ReactiveOAuth2AuthorizedClientManager<br> that instantiates InMemoryReactiveClientRegistrationRepository<br> and InMemoryReactiveOAuth2AuthorizedClientService</li></ul> |


## resourceserver

This project only implements `Resource Server` protected by JWT. The following is the how  
JWT issuer can be configured in application.yml.

```
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${AUTH_SERVER_B}
```

## resourceserver-webclient

This project implements `Resource Server`, client_credentials `WebClient`, `Method Security`,  
custom `JwtGrantedAuthoritiesConverter` and `JwtBearerTokenAuthenticationConverter`.

The following is how both site authentication (as `Resource Server`) and OAuth2 client 
can be configured in application.yml.

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
