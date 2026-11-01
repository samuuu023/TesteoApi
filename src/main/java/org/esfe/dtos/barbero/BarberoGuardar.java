package org.esfe.dtos.barbero;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BarberoGuardar {
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotBlank
  @Pattern(regexp = "[0-9+ -]{8,20}")
  private String telefono;

  @NotBlank
  @Size(max = 100)
  private String especialidad;

  @NotNull private Boolean activo;
}
