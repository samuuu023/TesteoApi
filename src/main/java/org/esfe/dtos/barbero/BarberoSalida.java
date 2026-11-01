package org.esfe.dtos.barbero;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class BarberoSalida {
  private Integer id;
  private String nombre;
  private String telefono;
  private String especialidad;
  private Boolean activo;
}
