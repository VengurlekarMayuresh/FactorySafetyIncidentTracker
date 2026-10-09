package com.factory.safety.service;

import com.factory.safety.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User register(User user);
    Optional<User> authenticate(String username, String password);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> getAllUsers();
}
