package com.example.reacttest;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableWebSecurity
class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                        (auth) -> auth
                        .anyRequest()
                        .permitAll()
                );
        return http.build();
    }
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}

@SpringBootApplication
public class ReactTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReactTestApplication.class, args);
    }
}

@Repository
class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void createTable() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                nickname VARCHAR(50) NOT NULL,
                email VARCHAR(89) UNIQUE NOT NULL,
                passhash VARCHAR(161) NOT NULL,
                amount_of_grades INT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS disciplines (
                id SERIAL PRIMARY KEY,
                code VARCHAR(100) NOT NULL,
                name VARCHAR(300) NOT NULL
            );
            CREATE TABLE IF NOT EXISTS grades (
                user_id INT,
                disc_id INT,
                year INT NOT NULL,
                semester INT NOT NULL,
                percentage REAL NOT NULL,
                letter VARCHAR(2) NOT NULL,
                GPA REAL NOT NULL,
                traditional VARCHAR(50) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (disc_id) REFERENCES disciplines(id),
                PRIMARY KEY (user_id, disc_id)
            );
            CREATE TABLE IF NOT EXISTS session (
                id SERIAL PRIMARY KEY,
                token VARCHAR(161) NOT NULL,
                user_id INT,
                expires_at BIGINT,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """);
    }
    public void insertUser(String name, String email, String pass) {
        jdbcTemplate.update("INSERT INTO users (nickname, email, amount_of_grades, passhash) VALUES (?,?,?,?)", name, email, 0, new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).encode(pass));
    }
    public Boolean doesPassMatch(Object id, String pass) {
        Map<String, Object> res = jdbcTemplate.queryForMap("select passhash from users where id = ?", id);
        return new Argon2PasswordEncoder(32, 64, 1, 15 * 1024, 2).matches(pass,(String) res.get("passhash"));
    }
    public Map<String, Object> getUserByNameorMail(String name) {
        return jdbcTemplate.queryForMap("SELECT * FROM users WHERE LOWER(nickname) = ? OR LOWER(email) = ?", name.toLowerCase(), name.toLowerCase());
    }
    public Map<String, Object> getUserById(Object id) {
        return jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", Integer.parseInt((String) id));
    }
    public void changeUserName(Object id, String newName) {
        jdbcTemplate.update("UPDATE users SET nickname = ? WHERE id = ?", newName, id);
    }
    public String newToken(Object id){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            String token_hash = hexString.toString();
            jdbcTemplate.update("INSERT INTO session (token, user_id, expires_at) VALUES (?, ?, ?)",token_hash,id,System.currentTimeMillis()+604800000);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error!";
    }
    public Map<String, Object> getUserByToken(String token){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            token = hexString.toString();
            Map<String, Object> data = jdbcTemplate.queryForMap("SELECT id, user_id, expires_at FROM session WHERE token = ?",token);
            Object session_id = data.get("id");
            Object user_id = data.get("user_id");
            long expires_at = Long.parseLong(data.get("expires_at").toString());
            long current_time = System.currentTimeMillis();
            if (current_time>expires_at){
                jdbcTemplate.update("DELETE FROM session WHERE id = ?",session_id);
                Map<String, Object> result = new HashMap<>();
                result.put("res","logout");
                return result;
            }
            return jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", user_id);

        } catch (Exception e) {
            System.out.println(e);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("res","error");
        return result;
    }
}

@Service
class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) {
        this.repo = repo;
        repo.createTable();
    }
}

@Service
class ImageService {
    private final WebClient webClient;
    private final UserRepository repo;
    public ImageService(WebClient webClient, UserRepository repo) {
        this.webClient = webClient;
        this.repo = repo;
    }
    public record ImageRequest(
            Integer user_id,
            String user_name,
            String user_email,
            double[] cords
    ) {}
    public byte[] fetchImage(Object id) {
        Map<String, Object> user = repo.getUserById(id);
        ImageRequest request = new ImageRequest((Integer) user.get("id"), (String) user.get("nickname"), (String) user.get("email"), new double[]{1.2, -6.5, 4.3, 5.6, -99.3});
        return webClient.post()
                .uri("http://localhost:8000/plot")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}

@RestController
@RequestMapping("/api/")
class APIController {
    private final UserRepository repo;
    APIController(UserRepository repo) {
        this.repo = repo;
    }
    @GetMapping("profile")
    public Map<String,Object> profile(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie spec_cookie = null;
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSessionToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    spec_cookie=cookie;
                }
            }
        }
        //Object id =session.getAttribute("id");
        Map<String,Object> map = new HashMap<>();
        if (token != null) {
            //Map<String, Object> user = repo.getUserById(id);
            Map<String, Object> user = repo.getUserByToken(token);
            if (user.getOrDefault("res",null)=="error" || user.getOrDefault("res",null)=="logout"){
                spec_cookie.setValue(null);
                spec_cookie.setMaxAge(0);
                spec_cookie.setPath("/");
                response.addCookie(spec_cookie);
                map.put("id",0);
                map.put("name","Anonymous");
            }else {
                map.put("id", user.get("id"));
                map.put("name", user.get("nickname"));
            }
        }else{
            map.put("id",0);
            map.put("name","Anonymous");
        }
        return map;
    }
    @PostMapping("profile")
    public Map<String,Boolean> profile(HttpSession session, HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String,Object> map) {
        Map<String,Boolean> res = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        Cookie spec_cookie = null;
        String token = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSessionToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    spec_cookie=cookie;
                }
            }
        }
        //Object id =session.getAttribute("id");
        try {
            //Map<String,Object> user = repo.getUserById(id);
            assert token != null;
            Map<String, Object> user = repo.getUserByToken(token);
            if (user.getOrDefault("res",null)=="error" || user.getOrDefault("res",null)=="logout"){
                spec_cookie.setValue(null);
                spec_cookie.setMaxAge(0);
                spec_cookie.setPath("/");
                response.addCookie(spec_cookie);
                res.put("success",false);
            }else {
                if (user.get("id") != map.get("nickname").toString()) {
                    repo.changeUserName(user.get("id"), map.get("nickname").toString());
                }
                res.put("success",true);
            }
        }catch (Exception e) {
            res.put("success",false);
        }
        return res;
    }
}

@Controller
@RequestMapping("/")
class THController {
    private final UserRepository repo;
    private final ImageService imageService;
    THController(UserRepository repo, ImageService imageService) {
        this.repo = repo;
        this.imageService = imageService;
    }
    @GetMapping(value = "plot/{id}", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public byte[] getImage(@PathVariable Object id) {
        try {
            return imageService.fetchImage(id);
        } catch (Exception e) {
            return new byte[0];
        }
    }
    @GetMapping("")
    public String home(HttpSession session, HttpServletRequest request, HttpServletResponse response, Model model) {
        Cookie[] cookies = request.getCookies();
        String token = null;
        Cookie spec_cookie = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JSessionToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    spec_cookie=cookie;
                }
            }
        }
        //Object id =session.getAttribute("id");
        if (token != null) {
            //Map<String, Object> user = repo.getUserById(id);
            Map<String, Object> user = repo.getUserByToken(token);
            if (user.getOrDefault("res",null)=="error" || user.getOrDefault("res",null)=="logout"){
                spec_cookie.setValue(null);
                spec_cookie.setMaxAge(0);
                spec_cookie.setPath("/");
                response.addCookie(spec_cookie);
                session.setAttribute("errorMsg","expired");
                return "redirect:/auth";
            }
            model.addAttribute("email",user.get("email"));
            model.addAttribute("imageId",user.get("id"));
        }else{
            model.addAttribute("email","Anonymous");
        }
        return "profile.html";
    }
    @GetMapping("auth")
    public String authentication(HttpSession session, @RequestParam(value="w", defaultValue = "l") String what, Model model){
        if (what.equalsIgnoreCase("r")){
            model.addAttribute("what","register");
        }else{
            model.addAttribute("what", "login");
        }
        Object errorMsg = session.getAttribute("errorMsg");
        session.removeAttribute("errorMsg");
        model.addAttribute("err",errorMsg);
        return "auth.html";
    }

    @PostMapping("register")
    public String registerUser(HttpSession session, HttpServletResponse response, @RequestParam(value = "username") String name, @RequestParam(value = "usermail") String email, @RequestParam(value = "userpass") String pass) {
        if (0<name.length() && name.length()<=50 && name.matches("^[a-zA-Z0-9_-]+$")) {
            if (8<=pass.length() && pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_-])(?=\\S+$).{8,}$")){
                if (0<email.length() && email.length()<=89 && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")){
                    try {
                        repo.insertUser(name, email, pass);
                        Object id = repo.getUserByNameorMail(name).get("id");
                        String token = repo.newToken(id);
                        if (!Objects.equals(token, "Error!")) {
                            Cookie cookie = new Cookie("JSessionToken", token);
                            cookie.setMaxAge(7 * 24 * 60 * 60);
                            cookie.setSecure(true);
                            cookie.setHttpOnly(true);
                            cookie.setPath("/");
                            response.addCookie(cookie);
                        }
                        //session.setAttribute("id", repo.getUserByNameorMail(name).get("id"));
                        return "redirect:/";
                    }catch (Exception e) {
                        e.printStackTrace();
                        session.setAttribute("errorMsg","already");
                    }
                }else{
                    session.setAttribute("errorMsg","email");
                }
            }else{
                session.setAttribute("errorMsg","pass");
            }
        }else{
            session.setAttribute("errorMsg","login");
        }
        return "redirect:/auth?w=r";
    }
    @PostMapping("login")
    public String loginUser(HttpSession session, HttpServletResponse response, @RequestParam(value = "username") String name, @RequestParam(value = "userpass") String pass) {
        if (0<name.length() && name.length()<=50) {
            if (8<=pass.length() && pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_-])(?=\\S+$).{8,}$")){
                if (name.matches("^[a-zA-Z0-9_-]+$")){
                    try {
                        Object id = repo.getUserByNameorMail(name).get("id");
                        try{
                            if (repo.doesPassMatch(id, pass)) {
                                String token = repo.newToken(id);
                                if (!Objects.equals(token, "Error!")) {
                                    Cookie cookie = new Cookie("JSessionToken", token);
                                    cookie.setMaxAge(7 * 24 * 60 * 60);
                                    cookie.setSecure(true);
                                    cookie.setHttpOnly(true);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                }
                                //session.setAttribute("id", id);
                                return "redirect:/";
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            session.setAttribute("errorMsg","pass");
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        session.setAttribute("errorMsg","who");
                    }
                }else if (name.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")){
                    try {
                        Object id = repo.getUserByNameorMail(name).get("id");
                        try{
                            if (repo.doesPassMatch(id, pass)) {
                                String token = repo.newToken(id);
                                if (!Objects.equals(token, "Error!")) {
                                    Cookie cookie = new Cookie("JSessionToken", token);
                                    cookie.setMaxAge(7 * 24 * 60 * 60);
                                    cookie.setSecure(true);
                                    cookie.setHttpOnly(true);
                                    cookie.setPath("/");
                                    response.addCookie(cookie);
                                }
                                //session.setAttribute("id", id);
                                return "redirect:/";
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            session.setAttribute("errorMsg","pass");
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        session.setAttribute("errorMsg","who");
                    }
                }else{
                    session.setAttribute("errorMsg","login");
                }
            }else{
                session.setAttribute("errorMsg","pass");
            }
        }else{
            session.setAttribute("errorMsg","login");
        }
        return "redirect:/auth?w=l";
    }
}