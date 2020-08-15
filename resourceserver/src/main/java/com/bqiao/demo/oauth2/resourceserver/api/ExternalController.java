package com.bqiao.demo.oauth2.resourceserver.api;

import lombok.extern.slf4j.Slf4j;
import com.bqiao.demo.oauth2.resourceserver.model.Repo;
import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bqiao.demo.oauth2.resourceserver.service.ExternalService;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping(value = "/external")
public class ExternalController {
    private final ExternalService service;

    public ExternalController(ExternalService service) {
        this.service = service;
    }
    @GetMapping("/{count}/repos")
    public Mono<CollectionModel<Repo>> getRepos(@PathVariable int count) {
        return service.getRepos(count);
    }
}
