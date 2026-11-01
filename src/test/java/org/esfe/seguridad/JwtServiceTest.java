package org.esfe.seguridad;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import org.esfe.seguridad.modelos.*;
import org.esfe.seguridad.servicios.JwtService;
import org.junit.jupiter.api.*;

class JwtServiceTest {
  String secret = "QmFyYmVyaWFBUElDbGF2ZUxvY2FsRGVtbzIwMjZDYW1iaWFybGE=";
  JwtService jwt;

  @BeforeEach
  void inicio() {
    jwt = new JwtService(secret);
  }

  @Test
  void tokenContieneUsuario() {
    Usuario u =
        Usuario.builder()
            .login("admin@barberia.com")
            .rol(Rol.builder().nombre("ROLE_ADMIN").build())
            .build();
    assertEquals(u.getLogin(), jwt.getUsernameFromToken(jwt.getToken(u)));
  }

  @Test
  void tokenVencido() {
    String t =
        Jwts.builder()
            .subject("admin")
            .issuer("barberia-api")
            .expiration(Date.from(Instant.now().minusSeconds(60)))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .compact();
    assertThrows(ExpiredJwtException.class, () -> jwt.getUsernameFromToken(t));
  }

  @Test
  void emisorIncorrecto() {
    String t =
        Jwts.builder()
            .subject("admin")
            .issuer("otra-api")
            .expiration(Date.from(Instant.now().plusSeconds(60)))
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
            .compact();
    assertThrows(JwtException.class, () -> jwt.getUsernameFromToken(t));
  }

  @Test
  void firmaAjena() {
    String t =
        Jwts.builder()
            .subject("admin")
            .issuer("barberia-api")
            .signWith(Jwts.SIG.HS256.key().build())
            .compact();
    assertThrows(JwtException.class, () -> jwt.getUsernameFromToken(t));
  }

  @Test
  void tokenMalFormado() {
    assertThrows(JwtException.class, () -> jwt.getUsernameFromToken("invalido"));
  }

  @Test
  void claveDebil() {
    assertThrows(io.jsonwebtoken.security.WeakKeyException.class, () -> new JwtService("Y29ydGE="));
  }
}
