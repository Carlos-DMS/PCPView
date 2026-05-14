package com.univesp.PCPView.exceptions;

public class ClosedSubOrderException extends RuntimeException {
    public ClosedSubOrderException() {
        super("Não é possível realizar essa operação para etapas de sub ordem finalizadas ou canceladas");
    }
}
