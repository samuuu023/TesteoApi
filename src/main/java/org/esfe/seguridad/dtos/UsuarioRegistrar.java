package org.esfe.seguridad.dtos;

import jakarta.validation.constraints.*;

public record UsuarioRegistrar(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(max = 100) String apellido,
    @NotBlank @Email @Size(max = 120) String login,
    @NotBlank @Size(min = 8, max = 72) String clave,
    @NotBlank @Pattern(regexp = "ROLE_ADMIN|ROLE_USER") String rol) {}
