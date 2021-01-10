package com.couchbase.couchify.web;

import com.couchbase.couchify.data.react.ReactiveUserRepository;
import com.couchbase.couchify.domain.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class WebFluxUserController {

    private final ReactiveUserRepository reactiveUserRepository;

    public WebFluxUserController(ReactiveUserRepository reactiveUserRepository) {
        this.reactiveUserRepository = reactiveUserRepository;
    }

    @PostMapping("/fluxer")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> createFluxUser(@RequestBody User user){
        return reactiveUserRepository.save(user);
    }
}
