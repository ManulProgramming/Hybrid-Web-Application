package com.example.manultube.service;

import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.dto.User.UserLoginDTO;
import com.example.manultube.dto.User.UserRegisterDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.model.User;
import com.example.manultube.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private UserService service;

    @Test
    void shouldCreateUser() {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("john");
        dto.setUsermail("john@mail.com");
        dto.setUserpass("asdQWE123!@#");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("john");
        saved.setUsermail("john@mail.com");

        when(repo.save(any(User.class))).thenReturn(saved);

        UserResponseDTO result = service.createUser(dto);

        assertNotNull(result);
        assertEquals("john", result.getUsername());
    }

    @Test
    void shouldReturnNullWhenDuplicate() {
        when(repo.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("dup"));

        UserResponseDTO result = service.createUser(new UserRegisterDTO());

        assertNull(result);
    }

    @Test
    void shouldMatchPassword() {
        User user = new User();
        user.setUserpass(new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode("asdQWE123!@#"));

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        boolean result = service.doesPassMatch("asdQWE123!@#", 1L);

        assertTrue(result);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = new User();
        user.setUsername("john");
        user.setUsermail("john@mail.com");
        user.setUserpass(new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode("asdQWE123!@#"));

        when(repo.findByUsernameOrUsermail("john","john"))
                .thenReturn(Optional.of(user));

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("john");
        dto.setUserpass("asdQWE123!@#");

        UserResponseDTO result = service.loginUser(dto);

        assertNotNull(result);
    }

    @Test
    void shouldReturnNullWhenWrongPassword() {
        User user = new User();
        user.setUserpass(new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode("asdQWE123!@#"));

        when(repo.findByUsernameOrUsermail(any(), any()))
                .thenReturn(Optional.of(user));

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUserpass("fghRTY456$%^");

        assertNull(service.loginUser(dto));
    }

    @Test
    void shouldReturnUserWhenTokenValid() {
        SessionResponseDTO session = new SessionResponseDTO();
        session.setUserId(1L);
        session.setExpiresIn(System.currentTimeMillis() + 10000);

        when(sessionService.getSessionByToken("token")).thenReturn(session);

        User user = new User();
        user.setId(1L);
        user.setUsername("john");

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = service.selectUserByToken("token");

        assertNotNull(result);
    }

    @Test
    void shouldReturnNullWhenExpired() {
        SessionResponseDTO session = new SessionResponseDTO();
        session.setUserId(1L);
        session.setExpiresIn(System.currentTimeMillis() - 1000);

        when(sessionService.getSessionByToken("token")).thenReturn(session);

        UserResponseDTO result = service.selectUserByToken("token");

        assertNull(result);
        verify(sessionService).deleteSession(any());
    }
}