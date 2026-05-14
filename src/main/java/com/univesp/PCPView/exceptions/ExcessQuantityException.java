package com.univesp.PCPView.exceptions;

public class ExcessQuantityException extends RuntimeException {
    public ExcessQuantityException() {
        super("A quantidade inserida excede o limite da etapa/ordem de produção");
    }
}
