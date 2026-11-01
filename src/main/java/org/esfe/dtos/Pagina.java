package org.esfe.dtos;

import java.util.List;
import org.springframework.data.domain.Page;

public record Pagina<T>(List<T> content, int number, int size, long totalElements, int totalPages) {
  public static <T> Pagina<T> desde(Page<T> p) {
    return new Pagina<>(
        p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
  }
}
