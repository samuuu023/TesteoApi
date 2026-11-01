package org.esfe.dtos.cita;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CitaModificar(
    @NotNull @Positive Integer clienteId,
    @NotNull @Positive Integer barberoId,
    @NotNull @Positive Integer servicioId,
    @NotNull LocalDateTime inicio,
    @Size(max = 300) String observaciones) {}
