package com.bqiao.demo.oauth2.resourceserver.service;

import com.bqiao.demo.oauth2.resourceserver.model.Repo;
import org.springframework.hateoas.CollectionModel;
import reactor.core.publisher.Mono;

public interface ExternalService {
    Mono<CollectionModel<Repo>> getRepos(int count);
}
