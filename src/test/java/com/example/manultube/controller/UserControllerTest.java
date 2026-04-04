package com.example.manultube.controller;

import com.example.manultube.component.PythonClient;
import com.example.manultube.dto.User.UserResponseDTO;
import com.example.manultube.service.CookieService;
import com.example.manultube.service.PostService;
import com.example.manultube.service.SessionService;
import com.example.manultube.service.UserService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private UserService userService;
    @MockitoBean private PostService postService;
    @MockitoBean private SessionService sessionService;
    @MockitoBean private CookieService cookieService;
    @MockitoBean private PythonClient pythonClient;

    @Test
    void shouldReturnUser() throws Exception {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(1L);
        dto.setUsername("john");

        when(userService.selectUserById(1L)).thenReturn(dto);
        when(cookieService.getCookie(any())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/u/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.username").value("john"));
    }

    @Test
    void shouldReturnCurrentUserWhenTokenValid() throws Exception {
        Map<String, Object> cookies = new HashMap<>();
        cookies.put("token", "abc");

        when(cookieService.getCookie(any())).thenReturn(cookies);

        UserResponseDTO current = new UserResponseDTO();
        current.setId(1L);
        current.setUsername("john");

        when(userService.selectUserByToken("abc")).thenReturn(current);
        when(userService.selectUserById(1L)).thenReturn(current);

        mockMvc.perform(get("/api/u/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentUser.name").value("john"));
    }
}