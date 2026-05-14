package com.univesp.PCPView.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException() {
        super("Erro ao buscar dados. Por gentileza, verificar a integridade dos dados relacionados junto ao administrador do banco de dados");
    }
}
