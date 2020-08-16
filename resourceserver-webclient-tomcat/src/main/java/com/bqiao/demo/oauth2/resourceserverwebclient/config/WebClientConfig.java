package com.bqiao.demo.oauth2.resourceserverwebclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.UnAuthenticatedServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    public static final String REGISTRATION_ID = "custom";

    /**
     * This does not work because authorizedClientService is not available either when tomcat is used.
     */
    //@Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository, ReactiveOAuth2AuthorizedClientService authorizedClientService) {

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
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        InMemoryReactiveClientRegistrationRepository clientRegistrationRepo =
                new InMemoryReactiveClientRegistrationRepository(clientRegistrationRepository.findByRegistrationId("custom"));
        ReactiveOAuth2AuthorizedClientService authorizedClientService =
                new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepo);
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrationRepo, authorizedClientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
    @Bean
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(REGISTRATION_ID);
        return WebClient.builder().filter(oauth).build();
    }

    /**
     * 1) ReactiveClientRegistrationRepository is not available if tomcat is used. So this bean has to be created manually.
     * 2) the bean name cannot be clientRegistrationRepository which conflicts with
     * {@link org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientRegistrationRepositoryConfiguration#clientRegistrationRepository(org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties)}
     * 3) this bean is not necessary if it gets created directly in bean authorizedClientManager
     */
    //@Bean
    public ReactiveClientRegistrationRepository reactiveClientRegistrationRepository(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveClientRegistrationRepository(clientRegistrationRepository.findByRegistrationId("custom"));
    }

    /**
     * The issue with is one is that UnAuthenticatedServerOAuth2AuthorizedClientRepository is deprecated.
     */
    //@Bean
    public WebClient webClient(ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        reactiveClientRegistrationRepository,
                        new UnAuthenticatedServerOAuth2AuthorizedClientRepository());
        oauth.setDefaultClientRegistrationId("custom");
        return WebClient.builder()
                .filter(oauth)
                .build();
    }
}
