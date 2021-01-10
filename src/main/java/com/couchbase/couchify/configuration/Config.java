package com.couchbase.couchify.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.data.couchbase.repository.config.EnableReactiveCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories(basePackages = {"com.couchbase.couchify.data"})
@EnableReactiveCouchbaseRepositories(basePackages = {"com.couchbase.couchify.data.react"})
@ConditionalOnProperty(value = "data", havingValue = "do")
public class Config extends AbstractCouchbaseConfiguration {

    private final String connectionString;
    private final String username;
    private final String password;
    private final String bucketName;

    public Config(@Value("${couchbase.clusterHost}") String connectionString,
                  @Value("${couchbase.username}") String username,
                  @Value("${couchbase.password}") String password,
                  @Value("${couchbase.bucket}") String bucketName) {

        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        this.bucketName = bucketName;
    }

    @Override
    public String getConnectionString() {
        return "couchbase://"+connectionString;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
