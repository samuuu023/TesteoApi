package org.esfe.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "barberos")
@Getter
@Setter
@NoArgsConstructor
public class Barbero {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, unique = true, length = 20)
  private String telefono;

  @Column(nullable = false, length = 100)
  private String especialidad;

  @Column(nullable = false)
  private Boolean activo;
}
