package org.esfe.controladores;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.esfe.seguridad.repositorios.UsuarioRepository;
import org.esfe.seguridad.servicios.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ApiIntegracionTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired UsuarioRepository users;
  @Autowired JwtService jwt;
  String cuerpo = "{\"nombre\":\"Corte\",\"precio\":7.50,\"duracionMinutos\":30,\"activo\":true}";

  @Test
  void saludPublica() throws Exception {
    mvc.perform(get("/api/salud"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.estado").value("OK"));
  }

  @Test
  void recursoSinToken() throws Exception {
    mvc.perform(get("/api/servicios")).andExpect(status().isUnauthorized());
  }

  @Test
  void tokenInvalido() throws Exception {
    mvc.perform(get("/api/servicios").header("Authorization", "Bearer invalido"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginCorrecto() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"login\":\"admin@barberia.com\",\"clave\":\"Admin123!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isString())
        .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"));
  }

  @Test
  void loginIncorrecto() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"login\":\"admin@barberia.com\",\"clave\":\"mala\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void jwtAutenticaDeVerdad() throws Exception {
    String token = jwt.getToken(users.findByLogin("admin@barberia.com").orElseThrow());
    mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.login").value("admin@barberia.com"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void lectorNoPuedeCrear() throws Exception {
    mvc.perform(post("/api/servicios").contentType("application/json").content(cuerpo))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "USER")
  void lectorPuedeListar() throws Exception {
    mvc.perform(get("/api/servicios")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void crudCompleto() throws Exception {
    var creado =
        mvc.perform(post("/api/servicios").contentType("application/json").content(cuerpo))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andReturn();
    int id = json.readTree(creado.getResponse().getContentAsString()).get("id").asInt();
    mvc.perform(get("/api/servicios/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Corte"));
    mvc.perform(
            put("/api/servicios/" + id)
                .contentType("application/json")
                .content(cuerpo.replace("Corte", "Barba")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Barba"));
    mvc.perform(delete("/api/servicios/" + id)).andExpect(status().isNoContent());
    mvc.perform(get("/api/servicios/" + id)).andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void precioNegativo() throws Exception {
    mvc.perform(
            post("/api/servicios")
                .contentType("application/json")
                .content(cuerpo.replace("7.50", "-1")))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void nombreVacio() throws Exception {
    mvc.perform(
            post("/api/servicios")
                .contentType("application/json")
                .content(cuerpo.replace("Corte", " ")))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void duracionInvalida() throws Exception {
    mvc.perform(
            post("/api/servicios")
                .contentType("application/json")
                .content(cuerpo.replace("30", "17")))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void duplicado() throws Exception {
    mvc.perform(post("/api/servicios").contentType("application/json").content(cuerpo))
        .andExpect(status().isCreated());
    mvc.perform(post("/api/servicios").contentType("application/json").content(cuerpo))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void idDelCuerpoNoPuedeSustituirRuta() throws Exception {
    mvc.perform(
            put("/api/servicios/1")
                .contentType("application/json")
                .content(cuerpo.replace("{", "{\"id\":99,")))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void clienteEmailInvalido() throws Exception {
    mvc.perform(
            post("/api/clientes")
                .contentType("application/json")
                .content("{\"nombre\":\"Samuel\",\"telefono\":\"71234567\",\"email\":\"mal\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void citaCamposFaltantes() throws Exception {
    mvc.perform(post("/api/citas").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void estadoInvalido() throws Exception {
    mvc.perform(
            patch("/api/citas/1/estado")
                .contentType("application/json")
                .content("{\"estado\":\"OTRO\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(roles = "USER")
  void registroRestringido() throws Exception {
    mvc.perform(
            post("/api/auth/usuarios")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Uno\",\"apellido\":\"Dos\",\"login\":\"uno@example.com\",\"clave\":\"Clave123!\",\"rol\":\"ROLE_ADMIN\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void registrarYLoginUsuario() throws Exception {
    mvc.perform(
            post("/api/auth/usuarios")
                .contentType("application/json")
                .content(
                    "{\"nombre\":\"Uno\",\"apellido\":\"Dos\",\"login\":\"uno@example.com\",\"clave\":\"Clave123!\",\"rol\":\"ROLE_USER\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.clave").doesNotExist());
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"login\":\"uno@example.com\",\"clave\":\"Clave123!\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void noEncontradoActualizar() throws Exception {
    mvc.perform(put("/api/servicios/999999").contentType("application/json").content(cuerpo))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void paginacionYBusqueda() throws Exception {
    mvc.perform(post("/api/servicios").contentType("application/json").content(cuerpo))
        .andExpect(status().isCreated());
    mvc.perform(get("/api/servicios?buscar=cort&page=0&size=5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
  }
}
