package com.bqiao.demo.oauth2.resourceserverwebclient.service;

import com.bqiao.demo.oauth2.resourceserverwebclient.model.Repo;
import reactor.core.publisher.Flux;

public interface InternalService {
    Flux<Repo> getRepos(int count);
}
