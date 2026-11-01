package org.esfe.seguridad.servicios;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.esfe.seguridad.modelos.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;

  public JwtService(@Value("${security.jwt.secret-key}") String secret) {
    key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
  }

  public String getToken(Usuario u) {
    return Jwts.builder()
        .subject(u.getUsername())
        .issuer("barberia-api")
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusSeconds(3600)))
        .claim("rol", u.getRol().getNombre())
        .signWith(key)
        .compact();
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .requireIssuer("barberia-api")
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
