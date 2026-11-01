package org.esfe.dtos.cita;

import jakarta.validation.constraints.NotNull;
import org.esfe.modelos.EstadoCita;

public record CitaCambiarEstado(@NotNull EstadoCita estado) {}
