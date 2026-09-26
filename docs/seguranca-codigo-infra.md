# Segurança de Código e Infraestrutura — Melhorias aplicadas

Este documento lista as mudanças realizadas no código e infraestrutura do projeto, com trechos antes/depois e justificativas de segurança.

**Validação de Entrada**
- O que foi feito: Adicionadas anotações Bean Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Size`, `@Email`) nos DTOs e nos parâmetros `@RequestBody` dos controllers (ex.: `AuthController`, `VeiculoController`, `PadronizacaoController`).

- Antes (exemplo em `AuthController`):

```java
@PostMapping("/login")
public ResponseEntity<TokenDTO> login(
        @RequestBody @Valid LoginDTO login) {
    return ResponseEntity.ok(authService.login(login));
}
```

- Depois:

```java
@PostMapping("/login")
public ResponseEntity<TokenDTO> login(
        @RequestBody @NotNull @Valid LoginDTO login) {
    return ResponseEntity.ok(authService.login(login));
}
```

- Por que aumenta a segurança: Validação de entrada impede payloads nulos ou mal formados, reduz risco de erros de lógica, injeção de dados e melhora as respostas de erro tratando dados inválidos antes de executar lógica sensível.

**Reforço nas regras nos DTOs**
- O que foi feito: adição de limites e mensagens (`@Size`) em campos como `senha`, `marca`, `modelo`, `versao`, `atributo`, `valor`.

- Antes (`LoginDTO`):

```java
@NotBlank
private String senha;
```

- Depois:

```java
@NotBlank
@Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
private String senha;
```

- Por que aumenta a segurança: reduz superfícies de ataque através de validação de tamanho e padrões, evitando armazenamento/exposição de dados inesperados.

**Rate Limiting (Bucket4j)**
- O que foi feito: adicionada dependência `bucket4j-core`, criado `RateLimitingFilter` que aplica limite por IP (ex.: 60 requisições/min) e registrado no `SecurityConfig` antes do filtro JWT.

- Antes (sem filtro): nenhuma limitação de taxa; endpoints expostos a abuso por requisições em massa.

- Depois (arquivo principal `RateLimitingFilter`):

```java
String ip = Optional.ofNullable(httpRequest.getHeader("X-Forwarded-For"))
        .map(h -> h.split(",")[0].trim())
        .orElse(httpRequest.getRemoteAddr());
Bucket bucket = resolveBucket(ip);
ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
if (probe.isConsumed()) {
    httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
    chain.doFilter(request, response);
} else {
    httpResponse.setStatus(429);
    httpResponse.setHeader("Retry-After", String.valueOf(waitSeconds));
    httpResponse.getWriter().write(...);
}
```

- Por que aumenta a segurança: protege contra DoS/abuso, scraping e tentativas de força bruta simples; reduz carga e exposição de endpoints sensíveis.

**Autenticação JWT e Refresh Tokens**
- O que foi feito: refatoração do `JwtService` para usar `Jwts.parserBuilder()` com `requireIssuer`, assinatura explícita HS256, separação de access token e refresh token (com tempos de expiração distintos), e armazenamento/rotação de refresh tokens (`RefreshToken` entity + `RefreshTokenRepository`). `AuthService` passou a gerar e persistir refresh token e a rota `/api/auth/refresh` foi adicionada.

- Antes (`JwtService.gerarToken`):

```java
return Jwts.builder()
    .subject(usuario.getEmail())
    .claim("nome", usuario.getNome())
    .claim("perfil", usuario.getPerfil().name())
    .issuedAt(agora)
    .expiration(expiracao)
    .signWith(getChave())
    .compact();
```

- Depois (trecho principal):

```java
return Jwts.builder()
    .setSubject(usuario.getEmail())
    .claim("nome", nome)
    .claim("perfil", usuario.getPerfil().name())
    .setIssuer(issuer)
    .setIssuedAt(agora)
    .setExpiration(expiracao)
    .signWith(getChave(), SignatureAlgorithm.HS256)
    .compact();
```

- Por que aumenta a segurança: validação de `issuer` evita tokens forjados de outras fontes; assinatura explícita e parserBuilder reduzem problemas na verificação; refresh tokens com rotação reduzem risco de exposição do access token e permitem tempos de expiração curtos para access tokens.

**Controle de Acesso por Perfil (@PreAuthorize)**
- O que foi feito: habilitado `@EnableMethodSecurity(prePostEnabled = true)` e adicionadas anotações `@PreAuthorize` em controllers (ex.: `VeiculoController`, `ConsultaTecnicaController`, `PadronizacaoController`) para restringir endpoints por papel (ex.: `BRIGADISTA`, `GESTOR`, `ADMINISTRADOR`, mantendo compatibilidade com `ADMIN` e `ANALISTA`).

- Antes (`VeiculoController.criar`):

```java
@PostMapping
public ResponseEntity<Veiculo> criar(@RequestBody @NotNull @Valid VeiculoDTO dto) { ... }
```

- Depois:

```java
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR','GESTOR')")
public ResponseEntity<Veiculo> criar(@RequestBody @NotNull @Valid VeiculoDTO dto) { ... }
```

- Por que aumenta a segurança: garante controle de autorização no nível de método, evitando que usuários sem privilégio executem operações CRUD sensíveis.

**Criptografia de Dados Sensíveis em Repouso (AES-GCM)**
- O que foi feito: adicionado `CryptoService` (AES-GCM) e aplicado:
  - `Usuario.nome` é criptografado antes de persistir (em `UsuarioService.salvar`) e `CargaInicial` grava nomes criptografados para manter consistência.
  - `Especificacao.valor` é criptografado ao salvar/atualizar `Veiculo` e decriptado ao ler/listar.
  - `JwtService` tentará decriptar `usuario.getNome()` ao incluir `nome` nas claims.

- Antes (`UsuarioService.salvar`):

```java
usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
return repository.save(usuario);
```

- Depois (trecho):

```java
usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
if (usuario.getNome() != null) {
    usuario.setNome(cryptoService.encrypt(usuario.getNome()));
}
return repository.save(usuario);
```

- Antes (`VeiculoService.salvar`):

```java
associarEspecificacoes(veiculo);
return repository.save(veiculo);
```

- Depois (trecho):

```java
if (veiculo.getEspecificacoes() != null) {
    veiculo.getEspecificacoes().forEach(e -> {
        if (e.getValor() != null) {
            e.setValor(cryptoService.encrypt(e.getValor()));
        }
    });
}
associarEspecificacoes(veiculo);
return repository.save(veiculo);
```

- Por que aumenta a segurança: protege dados pessoais e sensíveis em repouso; mesmo que o banco de dados seja comprometido, os campos criptografados não estarão em texto claro. AES-GCM fornece confidencialidade e integridade.

**Outras alterações relevantes**
- Dependências: adição de `bucket4j-core` no `pom.xml`.
- DTOs estendidos: `TokenDTO` agora inclui `refreshToken` e `refreshExpiraEm` para fluxo de refresh.

**Recomendações operacionais**
- Definir `crypto.secret` em `application.properties` com 32 bytes (não commitar no repositório) e armazená-lo em um secret manager.
- Definir tempos de expiração seguros no `application.properties`, por exemplo:

```
jwt.expiration=900000           # 15 minutos
jwt.refreshExpiration=604800000 # 7 dias
crypto.secret=CHAVE_SECRETA_SEGURA_32_BYTES
```

- Considerar migração dos dados existentes: criar um script/rota de migração que decripta valores legados (se existentes em texto claro) e recriptografa com `CryptoService`.
- Avaliar rate limiting por usuário autenticado (extraindo subject do token) além do limite por IP, para ambientes por trás de proxy/reverse-proxy.

---

Se quiser, eu:
- adiciono as configurações recomendadas em `application.properties` (valores e placeholders),
- gero um script de migração para dados existentes,
- executo `mvn test` e corrijo falhas de compilação/tests.

Indique qual desses próximos passos prefere que eu execute. 