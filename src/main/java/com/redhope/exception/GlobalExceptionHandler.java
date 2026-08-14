package com.redhope.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Access Denied");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }
        return new ModelAndView("error/403");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public Object handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Bad Credentials");
            body.put("message", "Invalid email or password");
            return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
        }
        return new ModelAndView("public/login")
                .addObject("errorMessage", "Invalid email or password");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Bad Request");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        return new ModelAndView("public/signup")
                .addObject("errorMessage", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/")) {
            Map<String, String> body = new HashMap<>();
            body.put("error", "Internal Server Error");
            body.put("message", ex.getMessage());
            return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ModelAndView("error/500");
    }
}
