package com.univesp.PCPView.exceptions;

public class NonExistentProductException extends RuntimeException {
    public NonExistentProductException() {
        super("O produto não está cadastrado na base de dados.");
    }
}
