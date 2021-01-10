package com.couchbase.couchify.data;

import com.couchbase.couchify.domain.User;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CouchbaseRepository<User, String> {

    List<User> findByEmailLike(String email);

    @Query("#{#n1ql.selectEntity} WHERE #{#n1ql.filter} AND email LIKE $email")
    public List<User> findUsersByEmail(String email);

}