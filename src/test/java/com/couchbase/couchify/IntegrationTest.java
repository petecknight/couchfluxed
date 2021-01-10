package com.couchbase.couchify;

import com.couchbase.couchify.data.react.ReactiveUserRepository;
import com.couchbase.couchify.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.couchbase.CouchbaseService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.annotation.PostConstruct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {CouchifyApplication.class, IntegrationTest.Config.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

    @LocalServerPort
    int randomServerPort;

    @Container
    public static CouchbaseContainer couchbaseContainer = new CouchbaseContainer("couchbase/server")
            .withEnabledServices(CouchbaseService.QUERY, CouchbaseService.INDEX, CouchbaseService.KV)
            .withBucket(new BucketDefinition("couchmusic2"));

    @Configuration
    @Order(1)
    static class Config extends AbstractCouchbaseConfiguration {

        private CouchbaseContainer couchbaseContainer;

        @PostConstruct
        public void init() {
            couchbaseContainer = IntegrationTest.couchbaseContainer;
        }

        @Override
        public String getConnectionString() {
            return couchbaseContainer.getConnectionString();
        }

        @Override
        public String getUserName() {
            return couchbaseContainer.getUsername();
        }

        @Override
        public String getPassword() {
            return couchbaseContainer.getPassword();
        }

        @Override
        public String getBucketName() {
            return "couchmusic2";
        }

        @Override
        protected boolean autoIndexCreation() {
            return true;
        }
    }

    private final TestRestTemplate testRestTemplate = new TestRestTemplate();

    @Autowired
    private ReactiveUserRepository reactiveUserRepository;

    @Test
    void test() {

        final User user = User.builder()
                .userId("888")
                .firstName("test")
                .lastName("tickle")
                .email("only@one.com")
                .tagLine("The one and only")
                .build();

        HttpEntity<User> userEntity = new HttpEntity<>(user);

        testRestTemplate.postForEntity("http://localhost:" + randomServerPort + "/user/", userEntity, String.class);

        final ResponseEntity<User> userSought = testRestTemplate.getForEntity("http://localhost:" + randomServerPort + "/user/user::888", User.class);

        assertThat(userEntity).satisfies(userResponseEntity -> {
            then(userSought.getStatusCode()).isEqualTo(HttpStatus.OK);
            then(userSought.getBody().getEmail()).isEqualTo("only@one.com");
        });


        Mono<User> plop = reactiveUserRepository.findByFirstNameAndLastName("test", "tickle");
        StepVerifier.create(plop)
                .assertNext(ubey ->
                {
                    then(ubey.getEmail()).isEqualTo("only@one.com");
                    then(ubey.getLastName()).isEqualTo("tickle");
                })
                .verifyComplete();
    }
}