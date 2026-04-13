package com.example.manultube;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.model.User;
import com.example.manultube.repository.SessionRepository;
import com.example.manultube.repository.UserRepository;
import com.example.manultube.repository.VerificationCodeRepository;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.JwtService;
import com.example.manultube.service.SessionService;
import com.example.manultube.service.VerificationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@EnableAutoConfiguration(exclude = MongoAutoConfiguration.class)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VerificationService verificationService;
    @MockitoBean
    private VerificationCodeRepository verificationCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private PythonClient pythonClient;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private SessionRepository sessionRepository;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JavaMailSender javaMailSender;
    @MockitoBean
    private MongoTemplate mongoTemplate;

    @Test
    void shouldReturnUserById() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass("pass");

        user = userRepository.save(user);

        when(cookieService.getCookie(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/u/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.username").value("john"));
    }

    @Test
    void shouldReturnCurrentUserWhenTokenValid() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass("pass");

        user = userRepository.save(user);

        Map<String, Object> cookies = new HashMap<>();
        cookies.put("token", "valid-token");

        when(cookieService.getCookie(any())).thenReturn(cookies);

        SessionResponseDTO session = new SessionResponseDTO();
        session.setUserId(user.getId());
        session.setExpiresIn(System.currentTimeMillis() + 10000);

        when(sessionService.getSessionByToken("valid-token"))
                .thenReturn(session);

        mockMvc.perform(get("/api/u/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUser.name").value("john"));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass(new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode("asdQWE123!@#"));

        user = userRepository.save(user);

        Map<String, Object> cookies = new HashMap<>();
        cookies.put("token", "valid");

        when(cookieService.getCookie(any())).thenReturn(cookies);

        SessionResponseDTO session = new SessionResponseDTO();
        session.setUserId(user.getId());
        session.setExpiresIn(System.currentTimeMillis() + 10000);

        when(sessionService.getSessionByToken("valid"))
                .thenReturn(session);

        String body = """
        {
            "username": "newName",
            "oldUserpass": "asdQWE123!@#"
        }
    """;

        mockMvc.perform(patch("/api/u/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.username").value("newName"));
    }

    //Returns 401
    @Test
    void shouldDeleteUser() throws Exception {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass(new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode("asdQWE123!@#"));

        user = userRepository.save(user);

        Map<String, Object> cookies = new HashMap<>();
        cookies.put("token", "valid");

        when(cookieService.getCookie(any())).thenReturn(cookies);

        SessionResponseDTO session = new SessionResponseDTO();
        session.setUserId(user.getId());
        session.setExpiresIn(System.currentTimeMillis() + 10000);

        when(sessionService.getSessionByToken("valid"))
                .thenReturn(session);

        String body = """
        {
            "oldUserpass": "asdQWE123!@#"
        }
    """;

        mockMvc.perform(delete("/api/u/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        assertFalse(userRepository.findById(user.getId()).isPresent());
    }
}