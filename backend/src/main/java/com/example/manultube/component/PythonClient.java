package com.example.manultube.component;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

@Component
public class PythonClient {
    private static final Path USER_DIR = Paths.get("uploads/u").toAbsolutePath();
    private static final Path POST_DIR = Paths.get("uploads/p").toAbsolutePath();
    private final WebClient webClient;
    public PythonClient(WebClient webClient) {
        this.webClient = webClient;
    }
    @Async
    public void processImage(Path input, Long userId) throws IOException {

        HashMap<String, String> body = new HashMap<>();
        body.put("path", input.toString());
        Mono<HashMap<String, String>> responseMono = webClient.post()
                .uri("http://fastapi:8000/process_image")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        HashMap<String, String> response = responseMono.block();
        Path output=Paths.get(response.get("path")).toAbsolutePath();
        Files.createDirectories(USER_DIR);
        Path finalPath = USER_DIR.resolve(userId.toString());
        Files.move(output, finalPath, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(output);
        Files.deleteIfExists(input);
    }
    @Async
    public void processVideo(Path input, Long postId) throws IOException {

        HashMap<String, String> body = new HashMap<>();
        body.put("path", input.toString());
        Mono<HashMap<String, String>> responseMono = webClient.post()
                .uri("http://fastapi:8000/process_video")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        HashMap<String, String> response = responseMono.block();
        Path output=Paths.get(response.get("path")).toAbsolutePath();
        Files.createDirectories(POST_DIR);
        Path finalPath = POST_DIR.resolve(postId.toString());

        body.put("path", output.toString());
        Mono<HashMap<String, String>> responseMono1 = webClient.post()
                .uri("http://fastapi:8000/create_thumbnail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});

        HashMap<String, String> response1 = responseMono1.block();
        Path output1=Paths.get(response1.get("path")).toAbsolutePath();
        Path finalPath1 = POST_DIR.resolve(postId.toString() +".jpg");
        Files.move(output, finalPath, StandardCopyOption.REPLACE_EXISTING);
        Files.move(output1, finalPath1, StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(output);
        Files.deleteIfExists(output1);
        Files.deleteIfExists(input);
    }
}