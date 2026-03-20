package com.example.manultube.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Controller
public class ImageController {
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @GetMapping("/media/{folder}/{id}")
    public ResponseEntity<Resource> media(@PathVariable String folder, @PathVariable String id) {
        if (!isInteger(id)){
            id="1";
        }
        if (Objects.equals(folder, "u")){
            folder="u";
        }else if (Objects.equals(folder, "t")) {
            folder="p";
            id+=".jpg";
        }else{
            folder="p";
        }
        Path file = Paths.get("uploads").resolve(folder).resolve(id);

        Resource res = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }
}