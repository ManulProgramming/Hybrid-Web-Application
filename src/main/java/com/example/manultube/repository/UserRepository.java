package com.example.manultube.repository;

import com.example.manultube.model.User;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public UserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createTable() {
        jdbcTemplate.getJdbcOperations().execute("""
        CREATE TABLE IF NOT EXISTS users (
            id SERIAL PRIMARY KEY,
            username VARCHAR(50) NOT NULL,
            usermail VARCHAR(89) UNIQUE NOT NULL,
            userpass VARCHAR(161) NOT NULL
        )
        """);
    }
    public User insertUser(User user) {
        String sql = "INSERT INTO users (username, usermail, userpass) VALUES (:username, :usermail, :userpass)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", user.getUsername())
                .addValue("usermail", user.getUsermail())
                .addValue("userpass", user.getUserpass());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rowsAffected = jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        if (rowsAffected != 1) {
            throw new DataAccessResourceFailureException("Could not insert user");
        }
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }
    public User getUserById(long id) {
        String sql = "SELECT * FROM users WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(User.class));
    }
    public User getUserByNameOrEmail(String name) {
        String sql = "SELECT * FROM users WHERE username = :username OR usermail = :username";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("username", name);
        return jdbcTemplate.queryForObject(sql, params, new BeanPropertyRowMapper<>(User.class));
    }
    public void updateUser(User user) {
        String set_query = "";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", user.getId());
        if (user.getUsername() != null) {
            set_query+=" username = :username ";
            params.addValue("username", user.getUsername());
        }
        if (user.getUserpass() != null) {
            if (!set_query.isEmpty()) {
                set_query+=",";
            }
            set_query+=" userpass = :userpass ";
            params.addValue("userpass", user.getUserpass());
        }
        if (user.getUsermail() != null) {
            if (!set_query.isEmpty()) {
                set_query+=",";
            }
            set_query+=" usermail = :usermail ";
            params.addValue("usermail", user.getUsermail());
        }
        String sql = "";
        if (!set_query.isEmpty()) {
            sql = "UPDATE users SET" + set_query + "WHERE id = :id";
            jdbcTemplate.update(sql, params);
        }
    }
    public void deleteUser(long id) {
        String sql = "DELETE FROM users WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);
        jdbcTemplate.update(sql, params);
    }
}
