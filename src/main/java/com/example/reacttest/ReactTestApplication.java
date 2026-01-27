package com.example.reacttest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

import java.util.HashMap;
import java.util.Map;

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
            )
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
        return jdbcTemplate.queryForMap("SELECT * FROM users WHERE id = ?", id);
    }
    public void changeUserName(Object id, String newName) {
        jdbcTemplate.update("UPDATE users SET nickname = ? WHERE id = ?", newName, id);
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

@RestController
@RequestMapping("/api/")
class APIController {
    private final UserRepository repo;
    APIController(UserRepository repo) {
        this.repo = repo;
    }
    @GetMapping("profile")
    public Map<String,Object> profile(HttpSession session) {
        Object id =session.getAttribute("id");
        Map<String,Object> map = new HashMap<>();
        if (id != null) {
            Map<String, Object> user = repo.getUserById(id);
            map.put("id",user.get("id"));
            map.put("name",user.get("nickname"));
        }else{
            map.put("id",0);
            map.put("name","Anonymous");
        }
        return map;
    }
    @PostMapping("profile")
    public Map<String,Boolean> profile(HttpSession session, @RequestBody Map<String,Object> map) {
        Map<String,Boolean> res = new HashMap<>();
        Object id =session.getAttribute("id");
        try {
            Map<String,Object> user = repo.getUserById(id);
            if (user.get("id") != map.get("nickname").toString()) {
                repo.changeUserName(id, map.get("nickname").toString());
            }
            res.put("success",true);
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
    THController(UserRepository repo) {
        this.repo = repo;
    }
    @GetMapping("")
    public String home(HttpSession session, Model model) {
        Object id =session.getAttribute("id");
        if (id != null) {
            Map<String, Object> user = repo.getUserById(id);
            model.addAttribute("email",user.get("email"));
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
        System.out.println(errorMsg);
        session.removeAttribute("errorMsg");
        model.addAttribute("err",errorMsg);
        return "auth.html";
    }

    @PostMapping("register")
    public String registerUser(HttpSession session, @RequestParam(value = "username") String name, @RequestParam(value = "usermail") String email, @RequestParam(value = "userpass") String pass) {
        if (0<name.length() && name.length()<=50 && name.matches("^[a-zA-Z0-9_-]+$")) {
            if (8<=pass.length() && pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_-])(?=\\S+$).{8,}$")){
                if (0<email.length() && email.length()<=89 && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")){
                    try {
                        repo.insertUser(name, email, pass);
                        session.setAttribute("id", repo.getUserByNameorMail(name).get("id"));
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
    public String loginUser(HttpSession session, @RequestParam(value = "username") String name, @RequestParam(value = "userpass") String pass) {
        if (0<name.length() && name.length()<=50) {
            if (8<=pass.length() && pass.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_-])(?=\\S+$).{8,}$")){
                if (name.matches("^[a-zA-Z0-9_-]+$")){
                    try {
                        Object id = repo.getUserByNameorMail(name).get("id");
                        try{
                            if (repo.doesPassMatch(id, pass)) {
                                session.setAttribute("id", id);
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
                                session.setAttribute("id", id);
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