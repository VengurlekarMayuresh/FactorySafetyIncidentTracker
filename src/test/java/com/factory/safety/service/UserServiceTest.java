package com.factory.safety.service;

import com.factory.safety.model.Role;
import com.factory.safety.model.User;
import com.factory.safety.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        User user = new User("testworker", "pass123", "Test Worker", Role.USER, "Welding");
        when(userRepository.existsByUsername("testworker")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User registered = userService.register(user);

        assertNotNull(registered);
        assertEquals("testworker", registered.getUsername());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testRegisterDuplicateThrowsException() {
        User user = new User("testworker", "pass123", "Test Worker", Role.USER, "Welding");
        when(userRepository.existsByUsername("testworker")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.register(user));
    }

    @Test
    void testAuthenticateSuccess() {
        User user = new User("admin", "admin123", "Admin User", Role.ADMIN, "Safety");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        Optional<User> authUser = userService.authenticate("admin", "admin123");

        assertTrue(authUser.isPresent());
        assertEquals("admin", authUser.get().getUsername());
        assertTrue(authUser.get().isAdmin());
    }

    @Test
    void testAuthenticateWrongPassword() {
        User user = new User("admin", "admin123", "Admin User", Role.ADMIN, "Safety");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        Optional<User> authUser = userService.authenticate("admin", "wrongpass");

        assertFalse(authUser.isPresent());
    }
}
