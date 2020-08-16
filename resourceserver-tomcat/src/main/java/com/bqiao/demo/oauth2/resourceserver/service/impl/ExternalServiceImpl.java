package com.bqiao.demo.oauth2.resourceserver.service.impl;

import com.bqiao.demo.oauth2.resourceserver.model.Repo;
import com.bqiao.demo.oauth2.resourceserver.service.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExternalServiceImpl implements ExternalService {

    @Override
    public Mono<CollectionModel<Repo>> getRepos(int count) {
        log.info("Starting NON-BLOCKING Controller!");
        List<Repo> repos = new ArrayList<>();
        for (int i=0; i < count; i++) {
            repos.add(new Repo(i, "no." + i));
        }
        CollectionModel.of(repos);
        Mono<CollectionModel<Repo>> reposFlux = Mono.just(CollectionModel.of(repos));
        log.info("Exiting NON-BLOCKING Controller!");
        return reposFlux;
    }
}
