package com.example.manultube.service;

import com.example.manultube.dto.Session.SessionRequestDTO;
import com.example.manultube.dto.Session.SessionResponseDTO;
import com.example.manultube.model.Session;
import com.example.manultube.repository.SessionRepository;
import jakarta.validation.Valid;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    private SessionResponseDTO toDto(Session session) {
        SessionResponseDTO dto = new SessionResponseDTO();
        dto.setId(session.getId());
        dto.setToken(session.getToken());
        dto.setUserId(session.getUserId());
        dto.setExpiresIn(session.getExpiresIn());
        return dto;
    }
    public SessionResponseDTO insertSession(@Valid SessionRequestDTO sessionRequestDTO) {
        Session session = new Session();
        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02X", b));
            }
            String token_hash = hexString.toString();
            session.setToken(token_hash);
            session.setUserId(sessionRequestDTO.getUserId());
            session.setExpiresIn(System.currentTimeMillis()+7*24*60*60*1000);
            Session createdSession = sessionRepository.insertSession(session);
            createdSession.setToken(token);
            return toDto(createdSession);
        }catch(NoSuchAlgorithmException ex){
            return null;
        }
    }
    public SessionResponseDTO getSessionByToken(String token) {
        try{
            Session session = sessionRepository.getSessionByToken(token);
            return toDto(session);
        }catch (EmptyResultDataAccessException ex){
            return null;
        }
    }
    public void deleteSession(Long id) {
        sessionRepository.deleteSession(id);
    }
    public void deleteAllSessions(Long userId) {
        sessionRepository.deleteAllSessionsForUserId(userId);
    }
}
