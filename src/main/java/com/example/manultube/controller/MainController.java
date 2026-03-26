package com.example.manultube.controller;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.Post.PostRequestDTO;
import com.example.manultube.dto.Post.PostResponseDTO;
import com.example.manultube.dto.Session.SessionRequestDTO;
import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.dto.User.UserLoginDTO;
import com.example.manultube.dto.User.UserRegisterDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.model.Page;
import com.example.manultube.model.Rating;
import com.example.manultube.model.TypicalResponse;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.PostService;
import com.example.manultube.service.SessionService;
import com.example.manultube.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("")
public class MainController {
    private final UserService userService;
    private final PostService postService;
    private final SessionService sessionService;
    private final CookieService cookieService;
    private final PythonClient pythonClient;
    public MainController(UserService userService, PostService postService, SessionService sessionService, CookieService cookieService, PythonClient pythonClient) {
        this.userService = userService;
        this.postService = postService;
        this.sessionService = sessionService;
        this.cookieService = cookieService;
        this.pythonClient = pythonClient;
    }
    private Boolean validateMimeImage(Path file) throws IOException {
        Tika tika = new Tika();
        String mime = tika.detect(file);

        Set<String> allowed = Set.of(
                "image/jpeg", "image/png"
        );

        if (!allowed.contains(mime)) {
            Files.deleteIfExists(file);
            return false;
        }
        return true;
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
    public String index(Model model, HttpServletRequest request, HttpServletResponse response, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size, @RequestParam(value="q", required = false, defaultValue = "") String query, @RequestParam(value="f", required = false, defaultValue = "hot") String sort) {
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        res.setParams(Map.of("theme", theme));
        model.addAttribute("res", res);
        return "home.html";
    }
    @GetMapping({"/login","/login/"})
    public String loginPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        res.setStatus(HttpStatus.OK);
        res.setParams(Map.of("theme", theme));
        if (model.getAttribute("res") == null) {
            model.addAttribute("res", res);
        }
        return "login.html";
    }
    @PostMapping({"/login","/login/"})
    public String loginUser(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @Valid @ModelAttribute UserLoginDTO user) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
        UserResponseDTO createdUser;
        createdUser = userService.loginUser(user);
        if (createdUser == null) {
            res.setStatus(HttpStatus.UNAUTHORIZED);
        }else {
            res.setStatus(HttpStatus.OK);
            res.setContent(createdUser);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", createdUser.getId());
            userMap.put("name", createdUser.getUsername());
            res.setCurrentUser(userMap);
            createTokenAndRemovePrevious(response, token, spec_cookie, createdUser);
        }
        res.setParams(Map.of("theme", theme));
        if (res.getStatus() == HttpStatus.OK) {
            return "redirect:/u/"+createdUser.getId();
        }else{
            redirectAttributes.addFlashAttribute("res", res);
            return "redirect:/login";
        }
    }
    private void createTokenAndRemovePrevious(HttpServletResponse response, String token, Cookie spec_cookie, UserResponseDTO createdUser) {
        if (token != null) {
            SessionResponseDTO existingSession = sessionService.getSessionByToken(token);
            if (existingSession != null) {
                sessionService.deleteSession(existingSession.getId());
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        SessionRequestDTO sessionRequestDTO = new SessionRequestDTO();
        sessionRequestDTO.setUserId(createdUser.getId());
        SessionResponseDTO sessionResponseDTO = sessionService.insertSession(sessionRequestDTO);
        if (sessionResponseDTO != null) {
            response.addCookie(cookieService.createCookie(sessionResponseDTO));
        }
    }
    @GetMapping({"/register","/register/"})
    public String registerPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        res.setStatus(HttpStatus.OK);
        res.setParams(Map.of("theme", theme));
        if (model.getAttribute("res") == null) {
            model.addAttribute("res", res);
        }
        return "register.html";
    }
    @PostMapping({"/register","/register/"})
    public String registerUser(HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @Valid @ModelAttribute UserRegisterDTO user, @RequestParam(value = "file", required = false) MultipartFile file) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
        UserResponseDTO createdUser;
        createdUser = userService.createUser(user);
        if (createdUser == null) {
            res.setStatus(HttpStatus.BAD_REQUEST);
        }else {
            res.setStatus(HttpStatus.OK);
            res.setContent(createdUser);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", createdUser.getId());
            userMap.put("name", createdUser.getUsername());
            res.setCurrentUser(userMap);
            if (file!=null && !file.isEmpty() && file.getSize()<=5L*1000*1000) {
                try {
                    Path tempFile = Files.createTempFile("upload-", ".bin");
                    try (InputStream in = file.getInputStream()) {
                        Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    Boolean status = validateMimeImage(tempFile);
                    if (status) {
                        pythonClient.processImage(tempFile, createdUser.getId());
                    }
                }catch (IOException ignored){
                }
            }
            createTokenAndRemovePrevious(response, token, spec_cookie, createdUser);
        }
        res.setParams(Map.of("theme", theme));
        if (res.getStatus() == HttpStatus.OK) {
            return "redirect:/u/"+createdUser.getId();
        }else{
            redirectAttributes.addFlashAttribute("res", res);
            return "redirect:/register";
        }
    }
    @GetMapping({"/u/{id}","/u/{id}/"})
    public String getUserPosts(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable Long id, @RequestParam(value="p", required = false, defaultValue = "1") Integer page, @RequestParam(value="s", required = false, defaultValue = "16") Integer size, @RequestParam(value="f", required = false, defaultValue = "hot") String sort) {
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        res.setParams(Map.of("theme", theme, "userId", id));
        model.addAttribute("res", res);
        return "profile.html";
    }
    @GetMapping({"/p","/p/"})
    public String createPostPage(Model model, HttpServletRequest request, HttpServletResponse response){
        TypicalResponse<Page<PostResponseDTO>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        res.setStatus(HttpStatus.OK);
        res.setParams(Map.of("theme", theme));
        if (model.getAttribute("res") == null) {
            model.addAttribute("res", res);
        }
        return "createPost.html";
    }
    @PostMapping({"/p","/p/"})
    public String createPost(Model model, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes, @Valid @ModelAttribute PostRequestDTO post, @RequestParam(value = "file", required = true) MultipartFile file){
        TypicalResponse<PostResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
        if (token != null) {
            UserResponseDTO user = userService.selectUserByToken(token);
            if (user != null) {
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
                            }else {
                                pythonClient.processVideo(tempFile, createdPost.getId());
                                res.setStatus(HttpStatus.CREATED);
                                res.setContent(createdPost);
                            }
                        }else{
                            res.setStatus(HttpStatus.BAD_REQUEST);
                        }

                    }catch (IOException ignored){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                    }
                }else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                }
            }else{
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setParams(Map.of("theme", theme));
        if (res.getStatus() == HttpStatus.CREATED) {
            return "redirect:/p/"+res.getContent().getId().toString();
        }else{
            redirectAttributes.addFlashAttribute("res", res);
            return "redirect:/p";
        }
    }
    @GetMapping({"/p/{id}","/p/{id}/"})
    public String getPost(Model model, HttpServletRequest request, HttpServletResponse response, @PathVariable Long id){
        TypicalResponse<Map<String, Object>> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        String theme = (String) cookieMap.get("theme");
        if (theme==null){
            theme="dark";
            response.addCookie(cookieService.createThemeCookie("dark"));
        }
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
        Map<String, Object> customMap = new HashMap<>();
        /*PostResponseDTO post = postService.getById(id);
        customMap.put("id", post.getId());
        customMap.put("userId", post.getUserId());
        customMap.put("username", post.getUsername());
        customMap.put("title", post.getTitle());
        customMap.put("description", post.getDescription());
        customMap.put("upvotes", post.getUpvotes());
        customMap.put("downvotes", post.getDownvotes());
        customMap.put("createdAt", post.getCreatedAt());*/
        //customMap.put("comments", postService.getComments(id, 1, 16));
        /*if (res.getCurrentUser()!=null) {
            Rating rating = postService.getRating((Long) res.getCurrentUser().get("id"),id);
            if (rating!=null) {
                customMap.put("rating", rating.getRating());
            }else{
                customMap.put("rating", null);
            }
        }else{
            customMap.put("rating", null);
        }*/
        res.setStatus(HttpStatus.OK);
        res.setParams(Map.of("theme", theme, "postId", id));
        res.setContent(customMap);
        model.addAttribute("res", res);
        return "post.html";
    }
}