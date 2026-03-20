package com.example.manultube.controller;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostUpdateDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.model.Comment;
import com.example.manultube.model.Page;
import com.example.manultube.model.Rating;
import com.example.manultube.model.TypicalResponse;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.PostService;
import com.example.manultube.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/p")
public class PostController {
    private final PostService postService;
    private final CookieService cookieService;
    private final UserService userService;
    private final PythonClient pythonClient;
    public PostController(PostService postService, CookieService cookieService, UserService userService, PythonClient pythonClient) {
        this.postService = postService;
        this.cookieService = cookieService;
        this.userService = userService;
        this.pythonClient = pythonClient;
    }
    private Boolean validateMimeVideo(Path file) throws IOException {
        Tika tika = new Tika();
        String mime = tika.detect(file);

        Set<String> allowed = Set.of(
                "video/mp4", "video/x-matroska"
        );

        if (!allowed.contains(mime)) {
            Files.deleteIfExists(file);
            return false;
        }
        return true;
    }
    @GetMapping({"/",""})
    public ResponseEntity<TypicalResponse<Page<PostResponseDTO>>> getPosts(HttpServletRequest request, HttpServletResponse response, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size, @RequestParam(value="q", required = false, defaultValue = "") String query, @RequestParam(value="f", required = false, defaultValue = "hot") String sort) {
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }

        Page<PostResponseDTO> posts = postService.getAll(page, size, query, sort);
        res.setStatus(HttpStatus.OK);
        res.setContent(posts);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @GetMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<PostResponseDTO>> getPostById(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id) {
        TypicalResponse<PostResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }

        PostResponseDTO post = postService.getById(id);
        if (post == null) {
            res.setStatus(HttpStatus.NOT_FOUND);
        }else{
            res.setStatus(HttpStatus.OK);
            res.setContent(post);
        }
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @GetMapping({"/{id}/r","/{id}/r/"})
    public ResponseEntity<TypicalResponse<Rating>> getUserPostRating(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id){
        TypicalResponse<Rating> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                res.setStatus(HttpStatus.OK);
                res.setContent(postService.getRating(user.getId(), id));
                return ResponseEntity.status(res.getStatus()).body(res);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @PostMapping({"/{id}/r","/{id}/r/"})
    public ResponseEntity<TypicalResponse<Rating>> changeUserPostRating(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @RequestBody(required = true) Rating rating){
        TypicalResponse<Rating> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                rating.setUserId(user.getId());
                rating.setPostId(id);
                res.setContent(postService.changeRating(rating));
                res.setStatus(HttpStatus.OK);
                return ResponseEntity.status(res.getStatus()).body(res);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @GetMapping({"/{id}/c","/{id}/c/"})
    public ResponseEntity<TypicalResponse<Page<Comment>>> getComments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size) {
        TypicalResponse<Page<Comment>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        Page<Comment> posts = postService.getComments(id, page, size);
        res.setStatus(HttpStatus.OK);
        res.setContent(posts);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @PostMapping({"/{id}/c","/{id}/c/"})
    public ResponseEntity<TypicalResponse<Comment>> getComments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @Valid @ModelAttribute Comment comment) {
        TypicalResponse<Comment> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                comment.setPostId(id);
                comment.setUserId(user.getId());
                comment.setUsername(user.getUsername());
                Comment createdComment = postService.createComment(comment);
                if (createdComment == null) {
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
                res.setStatus(HttpStatus.OK);
                res.setContent(createdComment);
                return ResponseEntity.status(res.getStatus()).body(res);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @DeleteMapping({"/{postId}/c/{commentId}","/{postId}/c/{commentId}/"})
    public ResponseEntity<TypicalResponse<Comment>> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<Comment> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                Comment comment = postService.getCommentById(commentId);
                if (comment != null && comment.getPostId().equals(postId) && comment.getUserId().equals(user.getId())){
                    postService.deleteComment(commentId);
                    res.setStatus(HttpStatus.OK);
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @PostMapping({"/",""})
    public ResponseEntity<TypicalResponse<PostResponseDTO>> createPost(@Valid @ModelAttribute PostRequestDTO post, @RequestParam(value = "file", required = true) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<PostResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");

        if (token!=null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                post.setUserId(user.getId());
                post.setUsername(user.getUsername());
                if (file!=null && !file.isEmpty() && file.getSize()<=1L*1000*1000*1000) {
                    try{
                        Path tempFile = Files.createTempFile("upload-", ".bin");
                        try (InputStream in = file.getInputStream()) {
                            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                        Boolean status = validateMimeVideo(tempFile);
                        if (status) {
                            PostResponseDTO createdPost = postService.create(post);
                            if (createdPost == null) {
                                res.setStatus(HttpStatus.BAD_REQUEST);
                                return ResponseEntity.status(res.getStatus()).body(res);
                            }
                            pythonClient.processVideo(tempFile, createdPost.getId());
                            res.setStatus(HttpStatus.CREATED);
                            res.setContent(createdPost);
                        }else{
                            res.setStatus(HttpStatus.BAD_REQUEST);
                        }
                        return ResponseEntity.status(res.getStatus()).body(res);
                    }catch (IOException ignored){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        return ResponseEntity.status(res.getStatus()).body(res);
                    }
                }else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @PutMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<PostResponseDTO>> putPost(@PathVariable("id") Long id, @Valid @RequestBody PostUpdateDTO post, HttpServletRequest request, HttpServletResponse response) {
        return updatePost(id, post, request, response);
    }
    @PatchMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<PostResponseDTO>> patchPost(@PathVariable("id") Long id, @Valid @RequestBody PostUpdateDTO post, HttpServletRequest request, HttpServletResponse response) {
        return updatePost(id, post, request, response);
    }

    private ResponseEntity<TypicalResponse<PostResponseDTO>> updatePost(@PathVariable("id") Long id, @RequestBody @Valid PostUpdateDTO post, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<PostResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                PostResponseDTO postResponseDTO = postService.getById(id);
                if (postResponseDTO != null && postResponseDTO.getUserId().equals(user.getId())){
                    postService.update(id, post);
                    res.setStatus(HttpStatus.OK);
                    res.setContent(postService.getById(id));
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @DeleteMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<PostResponseDTO>> deletePost(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<PostResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
                PostResponseDTO postResponseDTO = postService.getById(id);
                if (postResponseDTO != null && postResponseDTO.getUserId().equals(user.getId())){
                    postService.delete(id);
                    res.setStatus(HttpStatus.OK);
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
}
