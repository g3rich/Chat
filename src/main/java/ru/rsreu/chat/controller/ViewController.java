package ru.rsreu.chat.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.rsreu.chat.security.UserPrincipal;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) String token,
                       HttpServletResponse response) {
        if (token != null) {
            // Устанавливаем токен в cookie для последующих запросов
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return "chat";
    }

    @GetMapping("/home")
    public String home(@RequestParam(required = false) String token,
                       HttpServletResponse response) {
        if (token != null) {
            // Устанавливаем токен в cookie для последующих запросов
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return "home";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String token,
                       HttpServletResponse response) {
        if (token != null) {
            // Устанавливаем токен в cookie для последующих запросов
            Cookie cookie = new Cookie("token", token);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
        return "admin";
    }

}