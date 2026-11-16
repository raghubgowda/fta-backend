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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private com.raghu.fta.config.security.JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private com.raghu.fta.restcontrollers.AuthenticationController controller;

    @Test
    void generateTokenReturnsJwtWhenCredentialsAreValid() throws Exception {
        User loginUser = new User();
        loginUser.setEmail("login@test.com");
        loginUser.setPassword("secret");

        User user = new User();
        user.setEmail("login@test.com");
        user.setRole("ADMIN");

        when(userService.getUserByEmail("login@test.com")).thenReturn(user);
        when(jwtTokenUtil.generateToken(user)).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.register(loginUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
