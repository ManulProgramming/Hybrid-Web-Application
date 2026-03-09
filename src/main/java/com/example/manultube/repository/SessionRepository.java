package com.example.manultube.repository;

import com.example.manultube.model.Session;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class SessionRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public SessionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createTable(){
        jdbcTemplate.getJdbcOperations().execute("""
        CREATE TABLE IF NOT EXISTS session (
            id SERIAL PRIMARY KEY,
            token VARCHAR(161) NOT NULL,
            userId BIGINT NOT NULL,
            expiresIn BIGINT NOT NULL,
            FOREIGN KEY (userId) references users(id)
        );
        """);
    }
    public Session getSessionByToken(String token){
        String sql = "SELECT * FROM session WHERE token = :token";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", token);
        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(Session.class));
    }
    public Session insertSession(Session session){
        String sql = "INSERT INTO session(token, userId, expiresIn) VALUES (:token, :userId, :expiresIn)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("token", session.getToken())
                .addValue("userId", session.getUserId())
                .addValue("expiresIn", session.getExpiresIn());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        if (rowsAffected != 1){
            throw new DataAccessResourceFailureException("Could not create session");
        }
        session.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return session;
    }
    public void deleteSession(Long id){
        String sql = "DELETE FROM session WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, params);
    }
    public void deleteAllSessionsForUserId(Long userId){
        String sql = "DELETE FROM session WHERE userId = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);
        jdbcTemplate.update(sql, params);
    }
}
