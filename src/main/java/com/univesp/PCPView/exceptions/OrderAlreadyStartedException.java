package com.univesp.PCPView.exceptions;

public class OrderAlreadyStartedException extends RuntimeException {
    public OrderAlreadyStartedException (){
        super("Não é possível excluir uma ordem ou sub-ordem que já entrou em produção. Cancele-a em vez disso.");
    }
}
