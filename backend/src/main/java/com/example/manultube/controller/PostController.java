package com.example.manultube.controller;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.Comment.CommentRequestDTO;
import com.example.manultube.dto.Comment.CommentResponseDTO;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostUpdateDTO;
import com.example.manultube.dto.Rating.RatingRequestDTO;
import com.example.manultube.dto.Rating.RatingResponseDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.model.*;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.PostService;
import com.example.manultube.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        try (InputStream is = Files.newInputStream(file)) {
            Tika tika = new Tika();
            String mime = tika.detect(is, file.getFileName().toString());

            Set<String> allowed = Set.of(
                    "video/mp4",
                    "application/mp4",
                    "application/x-matroska",
                    "application/webm",
                    "video/x-matroska",
                    "video/webm"
            );

            if (!allowed.contains(mime)) {
                Files.deleteIfExists(file);
                return false;
            }
            return true;
        }
    }
    @Operation(summary = "Get posts", description = "Returns a page of all posts created")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch all posts"),
    })
    @GetMapping()
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
    @Operation(summary = "Get specific post", description = "Returns post data by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch post successful"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
    })
    @GetMapping("/{id}")
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
    @Operation(summary = "Get user's rating for specific post", description = "Returns a current rating that user has for that specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User's rating fetch successful"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
    })
    @GetMapping("/{id}/r")
    public ResponseEntity<TypicalResponse<RatingResponseDTO>> getUserPostRating(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id){
        TypicalResponse<RatingResponseDTO> res = new TypicalResponse<>();
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
                res.setContent(postService.getRating(user.getId(),id));
                return ResponseEntity.status(res.getStatus()).body(res);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @Operation(summary = "Change user's rating for this post", description = "Receive boolean value for rating and change it or put")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User's rating changed successful"),
            @ApiResponse(responseCode = "400", description = "Rating is required"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PostMapping("/{id}/r")
    public ResponseEntity<TypicalResponse<RatingResponseDTO>> changeUserPostRating(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @Parameter(description = "Rating body data") @RequestBody(required = true) RatingRequestDTO rating){
        TypicalResponse<RatingResponseDTO> res = new TypicalResponse<>();
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
    @Operation(summary = "Get comments for specific post", description = "Returns a page of comments for a specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fetch post comments")
    })
    @GetMapping("/{id}/c")
    public ResponseEntity<TypicalResponse<Page<CommentResponseDTO>>> getComments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size) {
        TypicalResponse<Page<CommentResponseDTO>> res = new TypicalResponse<>();
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
        Page<CommentResponseDTO> posts = postService.getComments(id, page, size);
        res.setStatus(HttpStatus.OK);
        res.setContent(posts);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
    @Operation(summary = "Create a new comment for specific post", description = "Gets user's info and their comment to add for specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Created comment successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request and body params"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
    })
    @PostMapping("/{id}/c")
    public ResponseEntity<TypicalResponse<CommentResponseDTO>> createComments(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @Parameter(description = "Comment body data") @Valid @RequestBody CommentRequestDTO comment) {
        TypicalResponse<CommentResponseDTO> res = new TypicalResponse<>();
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
                CommentResponseDTO createdComment = postService.createComment(comment);
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
    @Operation(summary = "Delete specific comment", description = "Remove comment by it's ID and the post ID where it is contained")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment removed successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @DeleteMapping( "/{postId}/c/{commentId}")
    public ResponseEntity<TypicalResponse<CommentResponseDTO>> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<CommentResponseDTO> res = new TypicalResponse<>();
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
                CommentResponseDTO comment = postService.getCommentById(commentId);
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
    @Operation(summary = "Create new post from form", description = "Creates a new post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad form data. File is required"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TypicalResponse<PostResponseDTO>> createPost(@Parameter(description = "Post form data") @Valid @ModelAttribute PostRequestDTO post, HttpServletRequest request, HttpServletResponse response) {
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
                if (post.getFile()!=null && !post.getFile().isEmpty() && post.getFile().getSize()<=1L*1000*1000*1000) {
                    try{
                        Path tempFile = Paths.get("/app/shared/upload-" + UUID.randomUUID() + ".bin");
                        try (InputStream in = post.getFile().getInputStream()) {
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
    @Operation(summary = "Update whole post", description = "Put new post information for specific one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad post form data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TypicalResponse<PostResponseDTO>> putPost(@PathVariable("id") Long id, @Parameter(description = "Post form data") @Valid @RequestBody PostUpdateDTO post, HttpServletRequest request, HttpServletResponse response) {
        return updatePost(id, post, request, response);
    }
    @Operation(summary = "Update specific post info", description = "Patch post information for specific one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad post form data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TypicalResponse<PostResponseDTO>> patchPost(@PathVariable("id") Long id, @Parameter(description = "Post form data") @Valid @RequestBody PostUpdateDTO post, HttpServletRequest request, HttpServletResponse response) {
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

    @Operation(summary = "Delete specific post", description = "Remove post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @DeleteMapping("/{id}")
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
                    postService.deleteAllCommentsByPost(id);
                    postService.deleteRatingsForPost(id);
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
