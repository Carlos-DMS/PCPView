package com.univesp.PCPView.exceptions;

public class NonExistentExecutionException extends RuntimeException {
    public NonExistentExecutionException() {
        super("Essa sessão de sub ordem não está cadastrada na base de dados.");
    }
}
