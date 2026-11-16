package com.raghu.fta.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void runtimeExceptionMapsToInternalServerError() {
        var response = handler.handleRunTimeException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("boom", response.getBody());
    }

    @Test
    void badCredentialsMapsToUnauthorized() {
        var response = handler.handleBadCredentialsException(new BadCredentialsException("bad"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void entityNotFoundMapsToNotFound() {
        var response = handler.handleNotFoundException(new EntityNotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody());
    }

    @Test
    void serviceExceptionMapsToInternalServerError() {
        var response = handler.handleServiceException(new ServiceException("issue"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("issue", response.getBody());
    }
}
