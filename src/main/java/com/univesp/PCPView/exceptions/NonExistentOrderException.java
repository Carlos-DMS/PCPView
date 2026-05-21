package com.univesp.PCPView.exceptions;

public class NonExistentOrderException extends RuntimeException {
    public NonExistentOrderException() {
        super("A ordem não está cadastrada na base de dados.");
    }
}
