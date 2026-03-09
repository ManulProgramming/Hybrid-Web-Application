package com.example.manultube.model;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class TypicalResponse<T> {
    private HttpStatus status;
    private Map<String, Object> currentUser;
    private T content;
    private Map<String, Object> params;

    public HttpStatus getStatus() {
        return status;
    }
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
    public Map<String, Object> getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(Map<String, Object> currentUser) {
        this.currentUser = currentUser;
    }
    public T getContent() {
        return content;
    }
    public void setContent(T content) {
        this.content = content;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
