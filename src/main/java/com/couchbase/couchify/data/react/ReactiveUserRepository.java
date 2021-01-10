package com.couchbase.couchify.data.react;

import com.couchbase.couchify.domain.User;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository extends ReactiveSortingRepository<User, String> {

    Flux<User> findByLastName(String firstname);

    Flux<User> findByFirstName(Publisher<String> firstname);

    Mono<User> findByFirstNameAndLastName(String firstName, String lastName);

    Page<User> findByFirstnameOrderByLastname(String firstname, Pageable pageable);

}