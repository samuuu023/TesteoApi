package org.esfe.modelos;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "citas",
    indexes = {
      @Index(name = "idx_cita_agenda", columnList = "barbero_id,inicio,fin"),
      @Index(name = "idx_cita_cliente", columnList = "cliente_id,inicio")
    })
@Getter
@Setter
@NoArgsConstructor
public class Cita {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Version
  @Column(nullable = false)
  private Long version;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id", nullable = false)
  private Cliente cliente;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "barbero_id", nullable = false)
  private Barbero barbero;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "servicio_id", nullable = false)
  private Servicio servicio;

  @Column(nullable = false)
  private LocalDateTime inicio;

  @Column(nullable = false)
  private LocalDateTime fin;

  @Column(nullable = false, precision = 6, scale = 2)
  private BigDecimal precio;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private EstadoCita estado;

  @Column(length = 300)
  private String observaciones;
}
