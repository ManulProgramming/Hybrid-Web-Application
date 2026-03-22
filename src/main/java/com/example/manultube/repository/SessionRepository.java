package com.example.manultube.repository;

import com.example.manultube.model.Session;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Repository
public class SessionRepository {
    private final MongoTemplate mongoTemplate;
    public SessionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Session getSessionByToken(String token) {
        Query query = new Query(Criteria.where("token").is(token));
        return mongoTemplate.findOne(query, Session.class);
    }

    public Session getSessionById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, Session.class);
    }

    public Session insertSession(Session session) {
        Session savedSession = mongoTemplate.save(session);
        if (savedSession == null) {
            throw new DataAccessResourceFailureException("Could not create session");
        }
        return savedSession;
    }

    public void deleteSession(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, Session.class);
    }

    public void deleteAllSessionsForUserId(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        mongoTemplate.remove(query, Session.class);
    }
}