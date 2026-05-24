package com.univesp.PCPView.exceptions;

public class ExecutionAlreadyFinishedException extends RuntimeException {
  public ExecutionAlreadyFinishedException() {
    super("Não é possível cancelar ou alterar uma execução que já foi finalizada.");
  }
}