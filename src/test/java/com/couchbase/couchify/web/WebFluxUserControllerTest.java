package com.couchbase.couchify.web;

import com.couchbase.couchify.data.react.ReactiveUserRepository;
import com.couchbase.couchify.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@WebFluxTest(WebFluxUserController.class)
public class WebFluxUserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveUserRepository reactiveUserRepository;

    @Test
    void create() {

        final User userToCreate = User.builder()
                .id("1")
                .firstName("firstName")
                .lastName("lastName")
                .email("email")
                .tagLine("tagLine")
                .build();

        given(reactiveUserRepository.save(any(User.class))).willReturn(Mono.just(userToCreate));

        webTestClient.post()
                .uri("/fluxer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userToCreate)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(User.class)
                .value(user -> then(user.getId()).isEqualTo("1"));
    }
}