package com.couchbase.couchify.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.couchbase.core.index.QueryIndexed;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;
import org.springframework.data.couchbase.core.mapping.id.IdPrefix;

import static org.springframework.data.couchbase.core.mapping.id.GenerationStrategy.USE_ATTRIBUTES;

@Data
@Document
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = USE_ATTRIBUTES, delimiter = "::")
    private String id;

    @IdPrefix(order=0)
    @Builder.Default
    private String userPrefix = "user";

    @IdAttribute
    private String userId;

    @Version
    private long version;

    private String firstName;

    @QueryIndexed
    private String lastName;

    @QueryIndexed
    private String email;
    private String tagLine;

    public String genId() {
        return  "user::"+userId;
    }
}