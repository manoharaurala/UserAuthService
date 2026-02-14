package org.ruby.userauthservice.controllers;

import org.ruby.userauthservice.exceptions.IncorrectPasswordException;
import org.ruby.userauthservice.exceptions.UserAlreadyExistException;
import org.ruby.userauthservice.exceptions.UserNotRegisteredException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthControllerAdvice {
    @ExceptionHandler(IncorrectPasswordException.class)
    public String handleIncorrectPasswordException(IncorrectPasswordException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public String handleUserAlreadyExistException(UserAlreadyExistException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(UserNotRegisteredException.class)
    public String handleUserNotRegisteredException(UserNotRegisteredException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex) {
        return "An unexpected error occurred: " + ex.getMessage();
    }
}
