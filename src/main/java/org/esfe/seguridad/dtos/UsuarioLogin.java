package org.esfe.seguridad.dtos;

import jakarta.validation.constraints.*;

public record UsuarioLogin(@NotBlank @Email String login, @NotBlank String clave) {}
