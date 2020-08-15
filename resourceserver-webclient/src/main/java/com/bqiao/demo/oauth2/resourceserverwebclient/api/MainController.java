package com.bqiao.demo.oauth2.resourceserverwebclient.api;

import com.bqiao.demo.oauth2.resourceserverwebclient.model.Repo;
import com.bqiao.demo.oauth2.resourceserverwebclient.service.InternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/internal")
public class MainController {
    private final InternalService service;

    public MainController(InternalService service) {
        this.service = service;
    }
    @GetMapping("/{count}/repos")
    public Flux<Repo> getRepos(@PathVariable int count) {
        return service.getRepos(count);
    }

    @PreAuthorize("hasAnyAuthority('SCOPE_openid')")
    @GetMapping("/principal")
    public Mono<Map<String, String>> getPrincipal(Principal principal) {
        Map<String, String> map = new HashMap<>();
        map.put("principal", principal.getName());
        return Mono.just(map);
    }

    @GetMapping("/current-user")
    public Mono<Object> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal);
    }
}
