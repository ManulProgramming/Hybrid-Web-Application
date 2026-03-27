package com.example.manultube.service;

import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.dto.User.UserLoginDTO;
import com.example.manultube.dto.User.UserRegisterDTO;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.dto.User.UserUpdateDTO;
import com.example.manultube.model.User;
import com.example.manultube.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final Argon2PasswordEncoder encoder;
    private static final Path USER_DIR = Paths.get("uploads/u").toAbsolutePath();
    public UserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        userRepository.createTable();
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
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername());
        user.setUsermail(userRegisterDTO.getUsermail());
        user.setUserpass(encoder.encode(userRegisterDTO.getUserpass()));
        try {
            return toDto(userRepository.insertUser(user));
        }catch (DataIntegrityViolationException e){
            return null;
        }
    }
    public Boolean doesPassMatch(String rawPassword,Long userId){
        return encoder.matches(rawPassword, userRepository.getUserById(userId).getUserpass());
    }
    public UserResponseDTO loginUser(UserLoginDTO userLoginDTO){
        try {
            User selectedUser = selectUserByNameOrEmail(userLoginDTO.getUsername());
            if (encoder.matches(userLoginDTO.getUserpass(), selectedUser.getUserpass())){
                return toDto(selectedUser);
            }else{
                return null;
            }
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }
    public UserResponseDTO selectUserById(Long id){
        return toDto(userRepository.getUserById(id));
    }
    public User selectUserByNameOrEmail(String name){
        return userRepository.getUserByNameOrEmail(name);
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
    public void updateUser(Long id, UserUpdateDTO userUpdateDTO){
        User user = new User();
        user.setUsername(userUpdateDTO.getUsername());
        if (userUpdateDTO.getUserpass() != null) {
            user.setUserpass(encoder.encode(userUpdateDTO.getUserpass()));
        }else{
            user.setUserpass(null);
        }
        user.setUsermail(userUpdateDTO.getUsermail());
        user.setId(id);
        userRepository.updateUser(user);
    }
    public void deleteUser(Long id){
        try {
            Files.deleteIfExists(USER_DIR.resolve(id.toString()));
        }catch (IOException ignored){
        }
        userRepository.deleteUser(id);
    }
}
