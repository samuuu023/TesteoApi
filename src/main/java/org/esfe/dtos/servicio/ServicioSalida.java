package org.esfe.dtos.servicio;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ServicioSalida {
  private Integer id;
  private String nombre;
  private java.math.BigDecimal precio;
  private Integer duracionMinutos;
  private Boolean activo;
}
