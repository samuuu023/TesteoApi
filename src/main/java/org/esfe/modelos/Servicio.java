package org.esfe.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false, unique = true, length = 100)
  private String nombre;

  @Column(nullable = false, precision = 6, scale = 2)
  private java.math.BigDecimal precio;

  @Column(nullable = false)
  private Integer duracionMinutos;

  @Column(nullable = false)
  private Boolean activo;
}
