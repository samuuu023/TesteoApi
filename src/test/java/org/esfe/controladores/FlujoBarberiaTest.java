package org.esfe.controladores;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class FlujoBarberiaTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  String token;
  int cliente, barbero, servicio;
  LocalDateTime fecha;

  String cuerpo(Object v) throws Exception {
    return json.writeValueAsString(v);
  }

  int crear(String ruta, Object datos) throws Exception {
    var r =
        mvc.perform(
                post(ruta)
                    .header("Authorization", "Bearer " + token)
                    .contentType("application/json")
                    .content(cuerpo(datos)))
            .andExpect(status().isCreated())
            .andReturn();
    return json.readTree(r.getResponse().getContentAsString()).get("id").asInt();
  }

  @BeforeEach
  void preparar() throws Exception {
    var r =
        mvc.perform(
                post("/api/auth/login")
                    .contentType("application/json")
                    .content(cuerpo(Map.of("login", "admin@barberia.com", "clave", "Admin123!"))))
            .andExpect(status().isOk())
            .andReturn();
    token = json.readTree(r.getResponse().getContentAsString()).get("token").asText();
    cliente =
        crear(
            "/api/clientes",
            Map.of("nombre", "Samuel", "telefono", "71234567", "email", "samuel@example.com"));
    barbero =
        crear(
            "/api/barberos",
            Map.of(
                "nombre",
                "Eduard",
                "telefono",
                "76543210",
                "especialidad",
                "Barba",
                "activo",
                true));
    servicio =
        crear(
            "/api/servicios",
            Map.of("nombre", "Corte", "precio", 8.50, "duracionMinutos", 30, "activo", true));
    LocalDate f = LocalDate.now(ZoneId.of("America/El_Salvador")).plusDays(4);
    while (f.getDayOfWeek() == DayOfWeek.SUNDAY) f = f.plusDays(1);
    fecha = f.atTime(10, 0);
  }

  Map<String, Object> cita() {
    return Map.of(
        "clienteId",
        cliente,
        "barberoId",
        barbero,
        "servicioId",
        servicio,
        "inicio",
        fecha.toString(),
        "observaciones",
        "Corte clásico");
  }

  @Test
  void reservaConfirmacionCancelacionYEliminacion() throws Exception {
    int id = crear("/api/citas", cita());
    mvc.perform(get("/api/citas/" + id).header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cliente").value("Samuel"))
        .andExpect(jsonPath("$.precio").value(8.50));
    mvc.perform(
            get("/api/citas/agenda?fecha=" + fecha.toLocalDate())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(id));
    for (String estado : List.of("CONFIRMADA", "CANCELADA"))
      mvc.perform(
              patch("/api/citas/" + id + "/estado")
                  .header("Authorization", "Bearer " + token)
                  .contentType("application/json")
                  .content(cuerpo(Map.of("estado", estado))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.estado").value(estado));
    mvc.perform(delete("/api/citas/" + id).header("Authorization", "Bearer " + token))
        .andExpect(status().isNoContent());
  }

  @Test
  void noEliminarClienteReferenciado() throws Exception {
    crear("/api/citas", cita());
    mvc.perform(delete("/api/clientes/" + cliente).header("Authorization", "Bearer " + token))
        .andExpect(status().isConflict());
  }

  @Test
  void actualizarBarberoYCliente() throws Exception {
    mvc.perform(
            put("/api/barberos/" + barbero)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(
                    cuerpo(
                        Map.of(
                            "nombre",
                            "Eduard actualizado",
                            "telefono",
                            "76543210",
                            "especialidad",
                            "Degradado",
                            "activo",
                            true))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Eduard actualizado"));
    mvc.perform(
            put("/api/clientes/" + cliente)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(
                    cuerpo(
                        Map.of(
                            "nombre",
                            "Samuel actualizado",
                            "telefono",
                            "71234567",
                            "email",
                            "samuel@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Samuel actualizado"));
  }

  @Test
  void reprogramarCita() throws Exception {
    int id = crear("/api/citas", cita());
    var nuevo = new HashMap<>(cita());
    nuevo.put("inicio", fecha.plusHours(1).toString());
    mvc.perform(
            put("/api/citas/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content(cuerpo(nuevo)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.inicio").value(fecha.plusHours(1).toString() + ":00"));
  }

  @Test
  void resumenDisponible() throws Exception {
    crear("/api/citas", cita());
    mvc.perform(
            get("/api/citas/resumen?fecha=" + fecha.toLocalDate())
                .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.pendiente").value(1))
        .andExpect(jsonPath("$.ingresos").value(0));
  }

  @Test
  void documentacionDisponible() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paths['/api/citas']").exists());
  }
}
