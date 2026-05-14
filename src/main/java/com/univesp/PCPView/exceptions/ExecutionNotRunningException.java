package com.univesp.PCPView.exceptions;

public class ExecutionNotRunningException extends RuntimeException {
    public ExecutionNotRunningException() {
        super("Não é possível finalizar a sessão, pois seu status está diferente de RODANDO");
    }
}
