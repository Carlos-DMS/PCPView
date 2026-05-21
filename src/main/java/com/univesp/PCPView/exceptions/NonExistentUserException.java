package com.univesp.PCPView.exceptions;

public class NonExistentUserException extends RuntimeException {
    public NonExistentUserException() {
        super("O usuário não está cadastrado na base de dados.");
    }
}
