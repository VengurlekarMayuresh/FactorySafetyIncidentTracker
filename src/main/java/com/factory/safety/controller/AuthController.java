package com.factory.safety.controller;

import com.factory.safety.model.Role;
import com.factory.safety.model.User;
import com.factory.safety.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String registered,
                                Model model,
                                HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return "redirect:/incidents";
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               Model model) {
        Optional<User> userOpt = userService.authenticate(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            session.setAttribute("currentUser", user);
            return "redirect:/incidents";
        } else {
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/demo-login")
    public String demoLogin(@RequestParam(defaultValue = "USER") String role, HttpSession session) {
        String targetUsername = "ADMIN".equalsIgnoreCase(role) ? "admin" : "worker1";
        Optional<User> userOpt = userService.findByUsername(targetUsername);
        userOpt.ifPresent(user -> session.setAttribute("currentUser", user));
        return "redirect:/incidents";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null) {
            return "redirect:/incidents";
        }
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String fullName,
                                  @RequestParam(defaultValue = "USER") Role role,
                                  @RequestParam(required = false) String department,
                                  Model model) {
        if (userService.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Username '" + username + "' is already taken.");
            model.addAttribute("roles", Role.values());
            return "auth/register";
        }

        User newUser = new User(username, password, fullName, role, department);
        userService.register(newUser);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        return "redirect:/incidents";
    }
}
