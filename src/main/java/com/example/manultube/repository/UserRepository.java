package com.example.manultube.repository;

import com.example.manultube.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsermail(String usermail);

    Optional<User> findByUsernameOrUsermail(String username, String usermail);
}
