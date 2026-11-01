package org.esfe.dtos.cliente;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ClienteSalida {
  private Integer id;
  private String nombre;
  private String telefono;
  private String email;
}
