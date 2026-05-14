package com.univesp.PCPView.exceptions;

public class NonExistentSubOrderException extends RuntimeException {
    public NonExistentSubOrderException() {
        super("A etapa da sub ordem não está cadastrada na base de dados.");
    }
}
