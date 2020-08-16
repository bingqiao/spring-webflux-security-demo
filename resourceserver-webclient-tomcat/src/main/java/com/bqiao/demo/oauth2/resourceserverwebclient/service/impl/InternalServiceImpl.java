package com.bqiao.demo.oauth2.resourceserverwebclient.service.impl;

import com.bqiao.demo.oauth2.resourceserverwebclient.model.Repo;
import com.bqiao.demo.oauth2.resourceserverwebclient.service.InternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.stream.Collectors;

@Slf4j
@Component
public class InternalServiceImpl implements InternalService {
    private final WebClient webClient;

    @Value("${demo.external.resource}")
    private String url;

    public InternalServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }
    @Override
    public Flux<Repo> getRepos(int count) {
        log.info("Starting NON-BLOCKING Controller!");
        Flux<Repo> repos = webClient
                .get()
                .uri(url, count)
                .retrieve()
                .bodyToFlux(new TypeReferences.CollectionModelType<EntityModel<Repo>>() {})
                .flatMapIterable(collection -> collection.getContent().stream().map(EntityModel::getContent).collect(Collectors.toList()));

        //repos.subscribe(repos -> log.info(repos.toString()));
        log.info("Exiting NON-BLOCKING Controller!");
        return repos;
    }
}
