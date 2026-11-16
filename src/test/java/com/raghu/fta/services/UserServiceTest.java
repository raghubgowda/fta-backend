package com.raghu.fta.services;

import com.raghu.fta.dao.UsersRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UsersRepo usersRepo;

    @InjectMocks
    private UserService userService;

    @Test
    void getUsersReturnsRepositoryList() {
        List<User> users = Collections.singletonList(new User());
        when(usersRepo.findAll()).thenReturn(users);

        assertSame(users, userService.getUsers());
    }

    @Test
    void getUsersWrapsRepositoryFailure() {
        when(usersRepo.findAll()).thenThrow(new RuntimeException("db error"));

        assertThrows(ServiceException.class, userService::getUsers);
    }

    @Test
    void getUserByIdReturnsUserWhenFound() {
        User user = new User();
        when(usersRepo.findById("u-1")).thenReturn(Optional.of(user));

        assertSame(user, userService.getUserById("u-1"));
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(usersRepo.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById("missing"));
    }

    @Test
    void getUserByEmailReturnsFirstMatch() {
        User user = new User();
        user.setEmail("user@test.com");
        when(usersRepo.findUsersByEmail("user@test.com")).thenReturn(Collections.singletonList(user));

        assertSame(user, userService.getUserByEmail("user@test.com"));
    }

    @Test
    void getUserByEmailThrowsWhenNotFound() {
        when(usersRepo.findUsersByEmail("missing@test.com")).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserByEmail("missing@test.com"));
    }

    @Test
    void isEmailIdInUseChecksRepository() {
        when(usersRepo.findUsersByEmail("email@test.com")).thenReturn(Collections.singletonList(new User()));

        assertTrue(userService.isEmailIdInUse("email@test.com"));
        assertFalse(userService.isEmailIdInUse("unused@test.com"));
    }

    @Test
    void updateUserRequiresExistingUser() {
        User existing = new User();
        existing.setId("u-1");
        when(usersRepo.findById("u-1")).thenReturn(Optional.of(existing));
        when(usersRepo.save(existing)).thenReturn(existing);

        assertSame(existing, userService.updateUser(existing));
    }

    @Test
    void saveUserReturnsSavedUser() {
        User user = new User();
        when(usersRepo.save(user)).thenReturn(user);

        assertSame(user, userService.saveUser(user));
    }

    @Test
    void deleteUserDelegatesToRepository() {
        userService.deleteUser("u-1");

        verify(usersRepo).deleteById("u-1");
    }

    @Test
    void loadUserByUsernameReturnsUserDetailsWithAdminRole() {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPassword("secret");
        when(usersRepo.findUsersByEmail("admin@test.com")).thenReturn(Collections.singletonList(user));

        UserDetails details = userService.loadUserByUsername("admin@test.com");

        assertEquals("admin@test.com", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(usersRepo.findUsersByEmail("missing@test.com")).thenReturn(Collections.emptyList());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("missing@test.com"));
    }
}
