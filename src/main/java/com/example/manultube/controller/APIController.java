package com.example.manultube.controller;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.Session.SessionRequestDTO;
import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.dto.User.UserLoginDTO;
import com.example.manultube.dto.User.UserRegisterDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.model.TypicalResponse;
import com.example.manultube.model.User;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.SessionService;
import com.example.manultube.service.UserService;
import com.example.manultube.service.VerificationService;
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class APIController {
    private final UserService userService;
    private final SessionService sessionService;
    private final CookieService cookieService;
    private final PythonClient pythonClient;
    private final VerificationService verificationService;
    public APIController(UserService userService, SessionService sessionService, CookieService cookieService, PythonClient pythonClient, VerificationService verificationService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.cookieService = cookieService;
        this.pythonClient = pythonClient;
        this.verificationService = verificationService;
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
    @GetMapping({"/",""})
    public ResponseEntity<Void> get() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/api/p/"))
                .build();
    }
    @PostMapping({"/login","/login/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> loginUser(@Valid @ModelAttribute UserLoginDTO user, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        UserResponseDTO createdUser;
        createdUser = userService.loginUser(user);
        if (createdUser == null) {
            res.setStatus(HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(res.getStatus()).body(res);
        }else {
            if (verificationService.verifyCode(createdUser.getUsermail(), user.getCode())) {
                res.setStatus(HttpStatus.OK);
                res.setContent(createdUser);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", createdUser.getId());
                userMap.put("name", createdUser.getUsername());
                res.setCurrentUser(userMap);
                return createTokenAndRemovePrevious(res, response, token, spec_cookie, createdUser);
            }
            res.setStatus(HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(res.getStatus()).body(res);
        }
    }

    private ResponseEntity<TypicalResponse<UserResponseDTO>> createTokenAndRemovePrevious(TypicalResponse<UserResponseDTO> res, HttpServletResponse response, String token, Cookie spec_cookie, UserResponseDTO createdUser) {
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
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @PostMapping({"/register","/register/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> createUser(@Valid @ModelAttribute UserRegisterDTO user, @RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        UserResponseDTO createdUser;
        createdUser = userService.createUser(user);
        if (createdUser == null) {
            res.setStatus(HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(res.getStatus()).body(res);
        }else {
            if (verificationService.verifyCode(createdUser.getUsermail(), user.getCode())) {
                res.setStatus(HttpStatus.OK);
                res.setContent(createdUser);
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", createdUser.getId());
                userMap.put("name", createdUser.getUsername());
                res.setCurrentUser(userMap);
                if (file != null && !file.isEmpty() && file.getSize() <= 5L * 1000 * 1000) {
                    try {
                        Path tempFile = Files.createTempFile("upload-", ".bin");
                        try (InputStream in = file.getInputStream()) {
                            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
                        }
                        Boolean status = validateMimeImage(tempFile);
                        if (status) {
                            pythonClient.processImage(tempFile, createdUser.getId());
                        }
                    } catch (IOException ignored) {
                    }
                }
                return createTokenAndRemovePrevious(res, response, token, spec_cookie, createdUser);
            }
            res.setStatus(HttpStatus.UNAUTHORIZED);
            return ResponseEntity.status(res.getStatus()).body(res);
        }
    }

    @PostMapping({"/logout","/logout/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> logoutUser(HttpServletRequest request, HttpServletResponse response){
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();
        Map<String, Object> cookieMap = cookieService.getCookie(request.getCookies());
        String token = (String) cookieMap.get("token");
        Cookie spec_cookie = (Cookie) cookieMap.get("spec_cookie");
        if (token!=null){
            SessionResponseDTO existingSession = sessionService.getSessionByToken(token);
            if (existingSession != null) {
                sessionService.deleteSession(existingSession.getId());
                response.addCookie(cookieService.deleteCookie(spec_cookie));
            }
        }
        res.setStatus(HttpStatus.OK);
        return ResponseEntity.status(res.getStatus()).body(res);
    }

    @PostMapping({"/code","/code/"})
    public ResponseEntity<TypicalResponse<UserResponseDTO>> createCode(@RequestBody User reqUser, HttpServletRequest request, HttpServletResponse response) {
        TypicalResponse<UserResponseDTO> res = new TypicalResponse<>();

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
        if (reqUser != null && reqUser.getUsermail() != null && userService.selectUserByNameOrEmail(reqUser.getUsermail())==null) {
            verificationService.sendVerificationCode(reqUser.getUsermail());
            res.setStatus(HttpStatus.OK);
        }else if (reqUser != null && reqUser.getUsername() != null){
            User selectedUser = userService.selectUserByNameOrEmail(reqUser.getUsername());
            if (selectedUser != null) {
                verificationService.sendVerificationCode(selectedUser.getUsermail());
                res.setStatus(HttpStatus.OK);
            }else{
                res.setStatus(HttpStatus.BAD_REQUEST);
            }
        }else{
            res.setStatus(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.status(res.getStatus()).body(res);
    }
}
