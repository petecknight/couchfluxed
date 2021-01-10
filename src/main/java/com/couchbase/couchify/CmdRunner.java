package com.couchbase.couchify;

import com.couchbase.couchify.data.UserRepository;
import com.couchbase.couchify.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(value = "data", havingValue = "do")
public class CmdRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;


    @Override
    public void run(String... strings) throws Exception {

        userRepository.save(createUser("user::0001", "Perry", "Manson", "perry.mason@acme.com", "Who can we get on the case?"));
        userRepository.save(createUser("user::0002", "Major", "Tom", "major.tom@acme.com", "Send me up a drink"));
        userRepository.save(createUser("user::0003", "Jerry", "Thomas", "jerry.wasaracecardriver@acme.com", "el sob number one"));
        userRepository.save(createUser("user::0004", "Jonny", "Tosca", "jonny.tosca@acme.com", "Opera singer to the gods"));
        userRepository.save(createUser("user::0005", "Jilly", "Nip", "jilly.nip@acme.com", "Good fisherman"));
        userRepository.save(createUser("user::0006", "Jeffy", "Tuck", "jeffy.tuck@acme.com", "Magnificent Plastic Surgen"));
        userRepository.save(createUser("user::0007", "Jazzy", "Wipe-out", "jaz.wipeout@acme.com", "Unfortunate but true"));

    }

    public static User createUser(String id, String firstName, String lastName,
                                  String email, String tagLine) {
        return User.builder().
        id(id).firstName(firstName).lastName(lastName).email(email).tagLine(tagLine).build();
    }

}