package com.example.manultube;

import com.example.manultube.repository.PostRepository;
import com.example.manultube.repository.SessionRepository;
import com.example.manultube.repository.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
class AllServices {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SessionRepository sessionRepository;
    public AllServices(UserRepository userRepository, PostRepository postRepository, SessionRepository sessionRepository) {
        this.userRepository = userRepository;
        userRepository.createTable();
        this.postRepository = postRepository;
        postRepository.createTable();
        this.sessionRepository = sessionRepository;
        sessionRepository.createTable();
    }
}

@SpringBootApplication
@EnableAsync
public class ManulTubeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManulTubeApplication.class, args);
    }

}
