package com.example.manultube.service;

import com.example.manultube.dto.Session.SessionResponseDTO;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CookieService {
    public Cookie createCookie(SessionResponseDTO session) {
        Cookie cookie = new Cookie("JWT", session.getToken());
        cookie.setMaxAge(7*24*60*60);
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        return cookie;
    }
    public Cookie createThemeCookie(String theme) {
        Cookie cookie = new Cookie("theme", theme);
        cookie.setSecure(true);
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        return cookie;
    }
    public Map<String,Object> getCookie(Cookie[] cookies){
        String token=null;
        Cookie spec_cookie=null;
        String theme=null;
        if (cookies != null){
            for (Cookie cookie : cookies){
                if (cookie.getName().equals("JWT")){
                    token = cookie.getValue();
                    spec_cookie = cookie;
                }else if (cookie.getName().equals("theme")){
                    theme = (cookie.getValue().equalsIgnoreCase("light")) ? "light" : "dark";
                }
            }
        }
        Map<String,Object> cookieMap = new HashMap<>();
        cookieMap.put("token",token);
        cookieMap.put("spec_cookie",spec_cookie);
        cookieMap.put("theme",theme);
        return cookieMap;
    }
    public Cookie deleteCookie(Cookie spec_cookie){
        spec_cookie.setValue(null);
        spec_cookie.setMaxAge(0);
        spec_cookie.setPath("/");
        return spec_cookie;
    }
}
