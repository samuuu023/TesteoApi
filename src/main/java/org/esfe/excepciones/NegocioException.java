package org.esfe.excepciones;

import lombok.Getter;

@Getter
public class NegocioException extends RuntimeException {
  private final int estado;

  public NegocioException(int estado, String mensaje) {
    super(mensaje);
    this.estado = estado;
  }
}
