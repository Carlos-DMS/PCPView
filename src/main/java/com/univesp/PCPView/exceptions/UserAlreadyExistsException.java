package com.univesp.PCPView.exceptions;

public class UserAlreadyExistsException extends RuntimeException{
    public UserAlreadyExistsException() {
        super("Email já cadastrado.");
    }
}
