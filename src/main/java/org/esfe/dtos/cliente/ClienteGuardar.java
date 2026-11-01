package org.esfe.dtos.cliente;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteGuardar {
  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotBlank
  @Pattern(regexp = "[0-9+ -]{8,20}")
  private String telefono;

  @NotBlank
  @Email
  @Size(max = 120)
  private String email;
}
