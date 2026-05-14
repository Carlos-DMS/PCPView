package com.univesp.PCPView.exceptions;

public class ClosedOrderException extends RuntimeException {
    public ClosedOrderException() {
        super("A ordem principal está com status finalizado ou cancelado, portanto essa operação será cancelada e todos suas sub ordens atualizadas para o mesmo status.");
    }
}
