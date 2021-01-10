package com.couchbase.couchify.web;

import com.couchbase.couchify.data.UserRepository;
import com.couchbase.couchify.domain.User;
import com.couchbase.couchify.exception.RepositoryException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {

        try {
            return ResponseEntity.ok().body(
                    userRepository.findById(userId).orElseThrow(() -> new RepositoryException("Not found")));

        } catch (RepositoryException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user")
    public ResponseEntity<Void> addUser(@RequestBody User user) {

        userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{userId}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/user")
    public ResponseEntity<Void> updateUser(@RequestBody User user) {

        if (!userRepository.existsById(user.genId())) {
            return ResponseEntity.notFound().build();
        }

        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/users-by-email")
    public ResponseEntity<List<User>> usersByEmail(@RequestParam(name = "email") String email) {

        try {
            List<User> users = userRepository.findByEmailLike(email);
            return ResponseEntity.ok().body(users);
        } catch (RepositoryException ex) {
            return ResponseEntity.notFound().build();
        }
    }

}
