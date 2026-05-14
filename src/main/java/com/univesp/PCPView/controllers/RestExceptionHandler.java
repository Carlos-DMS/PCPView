package com.univesp.PCPView.controllers;

import com.univesp.PCPView.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    private ResponseEntity<String> usuarioExistenteHandler(UserAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(NonExistentProductException.class)
    private ResponseEntity<String> produtoNaoExistenteHandler(NonExistentProductException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(NonExistentMachineException.class)
    private ResponseEntity<String> maquinaNaoExistenteHandler(NonExistentMachineException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(NonExistentExecutionException.class)
    private ResponseEntity<String> execucaoNaoExistenteHandler(NonExistentExecutionException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(NonExistentSubOrderException.class)
    private ResponseEntity<String> subOrdemNaoExistenteHandler(NonExistentSubOrderException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(ExcessQuantityException.class)
    private ResponseEntity<String> quantidadeExcedenteHandler(ExcessQuantityException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(ClosedOrderException.class)
    private ResponseEntity<String> ordemFechadaHandler(ClosedOrderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(ClosedSubOrderException.class)
    private ResponseEntity<String> subOrdemFechadaHandler(ClosedSubOrderException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(ExecutionNotRunningException.class)
    private ResponseEntity<String> execucaoNaoRodandoHandler(ExecutionNotRunningException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedExecutionAccessException.class)
    private ResponseEntity<String> acessoNaoAutorizadoHandler(UnauthorizedExecutionAccessException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    private ResponseEntity<String> erroDeBancoDeDadosHandler(DatabaseException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<String> excecaoGenericaHandler(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado: " + exception.getMessage());
    }
}
