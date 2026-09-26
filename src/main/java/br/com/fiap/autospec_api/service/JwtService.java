package br.com.fiap.autospec_api.service;

import br.com.fiap.autospec_api.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import br.com.fiap.autospec_api.security.CryptoService;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration; // milliseconds for access token

    @Value("${jwt.refreshExpiration:604800000}")
    private Long refreshExpiration; // default 7 days in ms

    @Value("${jwt.issuer:autospec}")
    private String issuer;

    private SecretKey getChave() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    private final CryptoService cryptoService;

    public JwtService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public String gerarToken(Usuario usuario) {

        Date agora = Date.from(Instant.now());
        Date expiracao = new Date(agora.getTime() + expiration);

        String nome = usuario.getNome();
        try {
            // se o nome estiver criptografado em repouso, tente decriptografar; se falhar, use o valor original
            nome = cryptoService.decrypt(usuario.getNome());
        } catch (Exception ignored) {
        }

        return Jwts.builder()
            .setSubject(usuario.getEmail())
            .claim("nome", nome)
                .claim("perfil", usuario.getPerfil().name())
                .setIssuer(issuer)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(getChave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String gerarRefreshToken(Usuario usuario) {
        Date agora = Date.from(Instant.now());
        Date expiracao = new Date(agora.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuer(issuer)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(getChave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extrairEmail(String token) {
        Claims claims = extrairClaims(token);
        return claims.getSubject();
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token).get("perfil", String.class);
    }

    public boolean tokenValido(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(getChave())
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token);

            Date expirationDate = jws.getBody().getExpiration();
            return expirationDate != null && expirationDate.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extrairClaims(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(getChave())
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token);

        return jws.getBody();
    }

    public Long getExpiration() {
        return expiration;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }
}