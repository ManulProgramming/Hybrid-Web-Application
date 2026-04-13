package com.example.manultube.service;

import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.dto.User.UserLoginDTO;
import com.example.manultube.dto.User.UserRegisterDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.dto.User.UserUpdateDTO;
import com.example.manultube.model.User;
import com.example.manultube.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final Argon2PasswordEncoder encoder;
    private static final Path USER_DIR = Paths.get("uploads/u").toAbsolutePath();
    public UserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.encoder = new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2);
    }
    private UserResponseDTO toDto(User user){
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setUsermail(user.getUsermail());
        return dto;
    }
    public UserResponseDTO createUser(UserRegisterDTO userRegisterDTO){
        logger.info("Attempting to create user with username: {}", userRegisterDTO.getUsername());
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setUsermail(userRegisterDTO.getUsermail());
        user.setUserpass(encoder.encode(userRegisterDTO.getUserpass()));
        try {
            User saved = userRepository.save(user);
            logger.info("User created successfully with id: {}", saved.getId());
            return toDto(saved);
        }catch (DataIntegrityViolationException e){
            logger.warn("User creation failed due to data integrity violation. Username/email may already exist: {}", userRegisterDTO.getUsername());
            return null;
        }
    }
    public Boolean doesPassMatch(String rawPassword,Long userId){
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return encoder.matches(rawPassword, user.getUserpass());
        }
        return false;
    }
    public UserResponseDTO loginUser(UserLoginDTO userLoginDTO){
        logger.info("Login attempt for: {}", userLoginDTO.getUsername());
        try {
            User selectedUser = selectUserByNameOrEmail(userLoginDTO.getUsername());
            if (selectedUser == null) {
                logger.warn("Login failed: user not found: {}", userLoginDTO.getUsername());
                return null;
            }
            if (encoder.matches(userLoginDTO.getUserpass(), selectedUser.getUserpass())){
                logger.info("Login successful for user id: {}", selectedUser.getId());
                return toDto(selectedUser);
            }else{
                logger.warn("Login failed: incorrect password for user: {}", userLoginDTO.getUsername());
                return null;
            }
        }catch (Exception e){
            logger.error("Unexpected error during login for user: {}", userLoginDTO.getUsername(), e);
            return null;
        }
    }
    public UserResponseDTO selectUserById(Long id){
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            return toDto(user);
        }
        throw new EmptyResultDataAccessException("User not found!",1);
    }
    public User selectUserByNameOrEmail(String name){
        return userRepository.findByUsernameOrUsermail(name, name).orElse(null);
    }
    public UserResponseDTO selectUserByToken(String token){
        SessionResponseDTO session = sessionService.getSessionByToken(token);
        if (session != null) {
            String sessionId = session.getId();
            Long userId = session.getUserId();
            Long expiresIn = session.getExpiresIn();
            long currentTime = System.currentTimeMillis();
            if (currentTime > expiresIn) {
                sessionService.deleteSession(sessionId);
                return null;
            }
            return selectUserById(userId);
        }
        return null;
    }
    @Transactional
    public void deleteUser(Long id){
        logger.info("Deleting user with id: {}", id);
        try {
            Files.deleteIfExists(USER_DIR.resolve(id.toString()));
            logger.debug("User directory deleted for id: {}", id);
        }catch (IOException e){
            logger.warn("Failed to delete user directory for id: {}", id, e);
        }
        userRepository.deleteById(id);
        logger.info("User deleted from database: {}", id);
    }

    @Transactional
    public void updateUser(Long id, UserUpdateDTO updated) {
        logger.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updated.getUsername() != null) {
            user.setUsername(updated.getUsername());
        }
        if (updated.getUsermail() != null) {
            user.setUsermail(updated.getUsermail());
        }
        if (updated.getUserpass() != null) {
            user.setUserpass(encoder.encode(updated.getUserpass()));
        }

        userRepository.save(user);
    }
}
