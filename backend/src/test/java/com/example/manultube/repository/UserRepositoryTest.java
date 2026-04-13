package com.example.manultube.repository;

import com.example.manultube.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    @Test
    void shouldFindByUsername() {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass("asdQWE123!@#");

        repo.save(user);

        Optional<User> result = repo.findByUsername("john");

        assertTrue(result.isPresent());
    }

    @Test
    void shouldFindByUsernameOrEmail() {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass("asdQWE123!@#");

        repo.save(user);

        Optional<User> result = repo.findByUsernameOrUsermail("john", "john");

        assertTrue(result.isPresent());
    }
}