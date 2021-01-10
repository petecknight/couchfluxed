# Howto Guide

[Couchbase](https://docs.couchbase.com/java-sdk/current/project-docs/compatibility.html#spring-compat) doesn't have an in-memory database for testing our app's [Spring Data integration](https://spring.io/projects/spring-data-couchbase) (by repository or template) therefore we use [test-containers](https://www.testcontainers.org/modules/databases/couchbase/) to wire in a Docker provided database to facilitate integration tests.

In this SpringBoot example the Couchbase configuration class annotated with @Configuration is automatically loaded into the context. However, during integration testing, we want to prefer the inner class configuration and this is achieved by providing it with an @Order(1) so it takes priority. This test configuration delegates the overriden methods to the Couchbase Docker container which is spun up, used and cleared up afer usage.  

The CmdRunner will enter some dummy data when run against the Couchbase configured in the application.properties file.

Here are some api calls using httpie:

```
echo '{"firstName" : "Jerry","lastName" : "Wasaracecardriver","email" : "jerry.wasaracecardriver@acme.com","tagLine" : "el sob number one"} | http POST localhost:8080/user

http GET localhost:8080/users-by-email?email=jiz.wipeoff@acme.com
```