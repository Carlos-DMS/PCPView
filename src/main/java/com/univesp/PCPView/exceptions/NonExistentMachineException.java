package com.univesp.PCPView.exceptions;

public class NonExistentMachineException extends RuntimeException {
    public NonExistentMachineException() {
        super("A máquina não está cadastrada na base de dados.");
    }
}
