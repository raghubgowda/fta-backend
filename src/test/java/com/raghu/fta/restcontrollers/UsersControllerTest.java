package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.User;
import com.raghu.fta.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UsersController controller;

    @Test
    void getUsersReturnsOk() {
        List<User> users = Arrays.asList(new User(), new User());
        when(userService.getUsers()).thenReturn(users);

        ResponseEntity<?> response = controller.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(users, response.getBody());
    }

    @Test
    void getUerReturnsUserById() {
        User user = new User();
        user.setId("u-1");
        when(userService.getUserById("u-1")).thenReturn(user);

        ResponseEntity<?> response = controller.getUer("u-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(user, response.getBody());
    }

    @Test
    void addUserRejectsDuplicateEmail() {
        User user = new User();
        user.setEmail("dup@test.com");
        when(userService.isEmailIdInUse("dup@test.com")).thenReturn(true);

        ResponseEntity<?> response = controller.addUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email id already in use", response.getBody());
    }

    @Test
    void addUserHashPasswordAndSave() {
        User user = new User();
        user.setEmail("new@test.com");
        user.setPassword("plain-password");
        when(userService.isEmailIdInUse("new@test.com")).thenReturn(false);
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = controller.addUser(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User saved = (User) response.getBody();
        assertNotEquals("plain-password", saved.getPassword());
        assertNotNull(saved.getPassword());
    }

    @Test
    void updateUserRejectsMissingId() {
        User user = new User();

        ResponseEntity<?> response = controller.updateUser(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(userService);
    }

    @Test
    void deleteUserReturnsNoContent() {
        ResponseEntity<?> response = controller.deleteUser("u-1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser("u-1");
    }
}
