package org.esfe.dtos.servicio;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioGuardar {
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotNull
  @DecimalMin("0.00")
  @DecimalMax("9999.99")
  @Digits(integer = 4, fraction = 2)
  private java.math.BigDecimal precio;

  @NotNull
  @Min(15)
  @Max(240)
  private Integer duracionMinutos;

  @NotNull private Boolean activo;
}
