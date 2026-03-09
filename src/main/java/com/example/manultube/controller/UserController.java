package com.example.manultube.controller;

import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.dto.User.UserUpdateDTO;
import com.example.manultube.model.Page;
import com.example.manultube.model.TypicalResponse;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.PostService;
import com.example.manultube.service.SessionService;
import com.example.manultube.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/u")
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final SessionService sessionService;
    private final CookieService cookieService;
    public UserController(UserService userService, PostService postService, SessionService sessionService, CookieService cookieService) {
        this.userService = userService;
        this.postService = postService;
        this.sessionService = sessionService;
        this.cookieService = cookieService;
    }
    @GetMapping({"/",""})
    public ResponseEntity<Void> get() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/api/p/"))
                .build();
    }
    @GetMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> getUserById(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        UserResponseDTO user = new UserResponseDTO();
        Map<String, Object> userMap = new HashMap<>();
        if (token != null) {
            user = userService.selectUserByToken(token);
            if (user != null) {
                userMap.put("id", user.getId());
                userMap.put("name", user.getUsername());
                res.setCurrentUser(userMap);
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        if (Objects.equals(id,userMap.getOrDefault("id",null))) {
            userResponseDTO.setId(id);
            userResponseDTO.setUsername(user.getUsername());
            userResponseDTO.setUsermail(user.getUsermail());
        }else{
            userResponseDTO = userService.selectUserById(id);
        }
        res.setStatus(HttpStatus.OK);
        res.setContent(userResponseDTO);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @GetMapping({"/{id}/p","/{id}/p/"})
    public ResponseEntity<TypicalResponse<Page<PostResponseDTO>>> getUserPosts(HttpServletRequest request, HttpServletResponse response, @PathVariable Long id, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size, @RequestParam(value="f", required = false, defaultValue = "hot") String sort) {
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
        Page<PostResponseDTO> posts =  postService.getByUserId(id, page, size, sort);
        res.setStatus(HttpStatus.OK);
        res.setContent(posts);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @PutMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> putUser(@PathVariable Long id, @Valid @ModelAttribute UserUpdateDTO user, HttpServletRequest request, HttpServletResponse response) {
        return updateUser(id, user, request, response);
    }
    @PatchMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> patchUser(@PathVariable Long id, @Valid @ModelAttribute UserUpdateDTO user, HttpServletRequest request, HttpServletResponse response) {
        return updateUser(id, user, request, response);
    }

    private ResponseEntity<TypicalResponse<UserResponseDTO>> updateUser(@PathVariable Long id, @ModelAttribute @Valid UserUpdateDTO user, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            UserResponseDTO userResponseDTO = userService.selectUserByToken(token);
            if (userResponseDTO == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userResponseDTO.getId());
                userMap.put("name", userResponseDTO.getUsername());
                res.setCurrentUser(userMap);
                if (id.equals(userResponseDTO.getId()) && userService.doesPassMatch(user.getOldUserpass(),userResponseDTO.getId())) {
                    userService.updateUser(id, user);
                    res.setStatus(HttpStatus.OK);
                    res.setContent(userService.selectUserById(id));
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @DeleteMapping({"/{id}","/{id}/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> deleteUser(@PathVariable Long id, @ModelAttribute @Valid UserUpdateDTO user, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            UserResponseDTO userResponseDTO = userService.selectUserByToken(token);
            if (userResponseDTO == null) {
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }else{
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", userResponseDTO.getId());
                userMap.put("name", userResponseDTO.getUsername());
                res.setCurrentUser(userMap);
                if (id.equals(userResponseDTO.getId()) && userService.doesPassMatch(user.getOldUserpass(),userResponseDTO.getId())) {
                    sessionService.deleteAllSessions(id);
                    postService.deleteAllPosts(id);
                    userService.deleteUser(id);
                    response.addCookie(cookieService.deleteCookie(spec_cookie));
                    res.setStatus(HttpStatus.OK);
                    res.setCurrentUser(null);
                    return ResponseEntity.status(res.getStatus()).body(res);
                }
            }
        }
        res.setStatus(HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(res.getStatus()).body(res);
    }
}
