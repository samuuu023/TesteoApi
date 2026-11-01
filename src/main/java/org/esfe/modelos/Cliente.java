package org.esfe.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
public class Cliente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(nullable = false, length = 100)
  private String nombre;

  @Column(nullable = false, length = 20)
  private String telefono;

  @Column(nullable = false, unique = true, length = 120)
  private String email;
}
