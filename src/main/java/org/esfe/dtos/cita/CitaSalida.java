package org.esfe.dtos.cita;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.esfe.modelos.EstadoCita;

public record CitaSalida(
    Integer id,
    Integer clienteId,
    String cliente,
    Integer barberoId,
    String barbero,
    Integer servicioId,
    String servicio,
    LocalDateTime inicio,
    LocalDateTime fin,
    BigDecimal precio,
    EstadoCita estado,
    String observaciones) {}
