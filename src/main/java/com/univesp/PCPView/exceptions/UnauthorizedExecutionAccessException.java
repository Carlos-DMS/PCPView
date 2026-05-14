package com.univesp.PCPView.exceptions;

public class UnauthorizedExecutionAccessException extends RuntimeException {
    public UnauthorizedExecutionAccessException() {
        super("Apenas o operador responsável pela sessão ou um usuário com privilégios de ADMINISTRADOR pode alterar essa sessão de sub ordem.");
    }
}
