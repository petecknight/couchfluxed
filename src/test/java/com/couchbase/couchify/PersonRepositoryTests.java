package com.couchbase.couchify;

import com.couchbase.client.core.msg.kv.DurabilityLevel;
import com.couchbase.client.java.query.QueryScanConsistency;
import com.couchbase.couchify.data.UserRepository;
import com.couchbase.couchify.data.react.ReactiveUserRepository;
import com.couchbase.couchify.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.ReactiveCouchbaseTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.couchbase.CouchbaseContainer;
import org.testcontainers.couchbase.CouchbaseService;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.annotation.PostConstruct;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {CouchifyApplication.class, PersonRepositoryTests.Config.class}, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonRepositoryTests {

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
            couchbaseContainer = PersonRepositoryTests.couchbaseContainer;
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

    @Autowired
    private ReactiveUserRepository reactiveUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;

    @Autowired
    private ReactiveCouchbaseTemplate reactiveCouchbaseTemplate;

    @Test
    public void sortsElementsCorrectly() {

        Flux<User> users = reactiveUserRepository.findAll(Sort.by(new Sort.Order(ASC, "lastName")));

        StepVerifier.create(users)
                .assertNext(user -> {
                    then(user.getEmail()).isEqualTo("perry.mason@acme.com");
                    then(user.getLastName()).isEqualTo("Manson");
                })
                .assertNext(user ->
                {
                    then(user.getEmail()).isEqualTo("major.tom@acme.com");
                    then(user.getLastName()).isEqualTo("Tom");
                })
                .assertNext(user -> {
                    then(user.getEmail()).isEqualTo("jerry.wasaracecardriver@acme.com");
                    then(user.getLastName()).isEqualTo("Wasaracecardriver");
                })
                .verifyComplete();
    }

    @Test
    void fluxWise() {

        Flux<User> user = reactiveUserRepository.findByLastName("Wasaracecardriver");
        StepVerifier.create(user)
                .assertNext(ubey ->
                {
                    then(ubey.getEmail()).isEqualTo("jerry.wasaracecardriver@acme.com");
                    then(ubey.getLastName()).isEqualTo("Wasaracecardriver");
                })
                .verifyComplete();
    }

    @Test
    void monoWise() {

        Mono<User> user = reactiveUserRepository.findByFirstNameAndLastName("Perry", "Manson");
        StepVerifier.create(user)
                .assertNext(ubey ->
                {
                    then(ubey.getEmail()).isEqualTo("perry.mason@acme.com");
                    then(ubey.getLastName()).isEqualTo("Manson");
                })
                .verifyComplete();
    }

    @Test
    public void sdf() {

        final User user = generateUserWithId("999", "tickle");

        couchbaseTemplate.upsertById(User.class).one(user);

        User found = couchbaseTemplate.findById(User.class).one(user.getId());

        assertThat(found).isNotNull().extracting("firstName").isEqualTo("test");

    }

    @Test
    void addOneFlux() {

        // Upsert it
        final User tester = generateUserWithId("999", "tester");
        reactiveCouchbaseTemplate.upsertById(User.class)
                .withDurability(DurabilityLevel.NONE)
                .one(tester).block();

        // Retrieve it again
        final Mono<User> one = reactiveCouchbaseTemplate.findById(User.class).one(tester.getId());

        StepVerifier.create(one)
                .assertNext(user1 -> {
                    then(user1.getEmail()).isEqualTo("boff@jobby.com");
                })
        .verifyComplete();

    }

    @Test
    void oneMore() {

        couchbaseTemplate
                .upsertById(User.class)
                .one(generateUserWithId("999", "tester"));

        User modified = couchbaseTemplate
                .upsertById(User.class)
                .one(generateUserWithId("999", "toaster"));

        assertThat(userRepository.findById("user::999"))
                .isNotNull()
                .isPresent()
                .hasValueSatisfying(user ->
                        then(user.getLastName()).isEqualTo("toaster"));

        assertThat(userRepository.findUsersByEmail("%@jobby.com")).hasSize(1);

        final List<User> foundUsers = couchbaseTemplate
                .findByQuery(User.class)
                .consistentWith(QueryScanConsistency.REQUEST_PLUS)
                .all();

        assertThat(foundUsers)
                .filteredOn(user -> user.getId().equals("user::999"))
                .first()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringFields("userPrefix")
                .isEqualTo(modified);

    }


    private static User generateUserWithId(String id, String lastName) {

        return User.builder()
                .userId(id)
                .firstName("test")
                .lastName(lastName)
                .email("boff@jobby.com")
                .tagLine("You're a monster")
                .build();

    }

    @BeforeEach
    public void load() {

        User u1 = createUser("user::0001", "Perry", "Manson", "perry.mason@acme.com", "Who can we get on the case?");
        userRepository.save(u1);

        User u2 = createUser("user::0002", "Major", "Tom", "major.tom@acme.com", "Send me up a drink");
        userRepository.save(u2);

        User u3 = createUser("user::0003", "Jerry", "Wasaracecardriver", "jerry.wasaracecardriver@acme.com", "el sob number one");
        userRepository.save(u3);
    }

    @AfterEach
    public void cleanUp(){

        if(userRepository.existsById("user::999")){
            userRepository.deleteById("user::999");
        }
    }

    public static User createUser(String id, String firstName, String lastName,
                                  String email, String tagLine) {
        return User.builder().
                id(id).firstName(firstName).lastName(lastName).email(email).tagLine(tagLine).build();
    }


}