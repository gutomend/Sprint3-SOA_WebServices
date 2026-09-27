# Segurança: Código e Infraestrutura — AutoSpec (resumo de implementações)

Este documento lista as melhorias de segurança já implementadas no código-fonte do projeto, com trechos de código e explicações curtas. Só foram documentadas as funcionalidades já presentes no repositório — onde algo é referenciado na documentação mas não existe no código, isso é indicado.

## 1) Hardening de API

- Validação de entrada (exemplos)

- `VeiculoController` / `VeiculoDTO`

Trecho (arquivo: [src/main/java/br/com/fiap/autospec_api/controller/VeiculoController.java](src/main/java/br/com/fiap/autospec_api/controller/VeiculoController.java)):

```java
    @PostMapping
    public ResponseEntity<Veiculo> criar(
            @RequestBody @Valid VeiculoDTO dto) {
        ...
    }
```

Trecho (arquivo: [src/main/java/br/com/fiap/autospec_api/dto/VeiculoDTO.java](src/main/java/br/com/fiap/autospec_api/dto/VeiculoDTO.java)):

```java
    @NotBlank(message = "A marca é obrigatória")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "A versão é obrigatória")
    private String versao;

    @Valid
    private List<EspecificacaoDTO> especificacoes;
```

Trecho (arquivo: [src/main/java/br/com/fiap/autospec_api/dto/EspecificacaoDTO.java](src/main/java/br/com/fiap/autospec_api/dto/EspecificacaoDTO.java)):

```java
    @NotBlank(message = "O atributo é obrigatório")
    private String atributo;

    @NotBlank(message = "O valor é obrigatório")
    private String valor;
```

Explicação: validação server-side (`@Valid`, `@NotBlank`) impede dados malformados e entradas vazias/insuportadas, reduzindo risco de injeção, mass-assignment e erro lógico ao processar objetos.

- Rate limiting (Bucket4j)

Observação: a documentação do projeto menciona um `RateLimitingFilter` e uso de Bucket4j, porém não há uma classe implementada `RateLimitingFilter` nem referências de implementação do Bucket4j no código fonte atual. Portanto não há trecho de código para mostrar aqui. Se implementado, um rate-limiter por IP ou por token reduz risco de DoS e ataques de brute-force.

- JWT — geração e validação

Trecho (arquivo: [src/main/java/br/com/fiap/autospec_api/service/JwtService.java](src/main/java/br/com/fiap/autospec_api/service/JwtService.java)):

```java
    public String gerarToken(Usuario usuario) {

        Date agora = new Date();

        Date expiracao = new Date(
                agora.getTime() + expiration
        );

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(getChave())
                .compact();
    }

    public boolean tokenValido(String token) {

        try {

            Claims claims = extrairClaims(token);

            return claims.getExpiration()
                    .after(new Date());

        } catch (Exception e) {

            return false;
        }
    }
```

Explicação: o `JwtService` gera tokens com claims (`perfil`, `nome`), `issuedAt` e `expiration`, e assina usando uma chave HMAC derivada de `jwt.secret` (`Keys.hmacShaKeyFor(...)`). A expiração e assinatura mitigam token replay e uso de tokens forjados.

Além disso, o filtro de autenticação `JwtAuthenticationFilter` valida o token em cada requisição e popula o `SecurityContext` com a autoridade derivada de `perfil` (arquivo: [src/main/java/br/com/fiap/autospec_api/security/JwtAuthenticationFilter.java](src/main/java/br/com/fiap/autospec_api/security/JwtAuthenticationFilter.java)).

```java
    if (!jwtService.tokenValido(token)) { ... }

    String email = jwtService.extrairEmail(token);
    Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
    SimpleGrantedAuthority autoridade = new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name());
```

Explicação curta: autenticação centralizada via filtro impede acesso não autenticado e transforma claims do JWT em roles utilizáveis pelo Spring Security.

## 2) Controle de acesso por perfil

- Trecho de configuração (authorization)

Arquivo: [src/main/java/br/com/fiap/autospec_api/config/SecurityConfig.java](src/main/java/br/com/fiap/autospec_api/config/SecurityConfig.java)

```java
    .requestMatchers(
            HttpMethod.GET,
            "/api/veiculos/**"
    )
    .hasAnyRole(
            "ADMIN",
            "ANALISTA"
    )

    .requestMatchers(
            HttpMethod.POST,
            "/api/veiculos/**"
    )
    .hasRole("ADMIN")

    .requestMatchers(
            HttpMethod.PUT,
            "/api/veiculos/**"
    )
    .hasRole("ADMIN")

    .requestMatchers(
            HttpMethod.DELETE,
            "/api/veiculos/**"
    )
    .hasRole("ADMIN")
```

Explicação: as regras declarativas mapeiam endpoints a roles, permitindo que o framework negue automaticamente requisições de perfis não autorizados.

- Lista de endpoints por perfil (conforme `SecurityConfig`)

- ADMIN:
  - POST /api/veiculos (criar veículo)
  - PUT /api/veiculos/{id} (atualizar veículo)
  - DELETE /api/veiculos/{id} (excluir veículo)
  - GET /api/veiculos/** (consultas) — também permitido

- ANALISTA:
  - GET /api/veiculos/** (consultas somente)

- Públicos (sem autenticação):
  - /api/auth/** (login)
  - /swagger-ui/**, /v3/api-docs/**

## 3) Criptografia

- AES‑GCM (campos sensíveis)

Observação: a documentação do projeto refere-se ao uso de AES‑GCM para campos como `Usuario.nome` e `Especificacao.valor`, porém no código-fonte atual não há implementações (nenhuma classe `CryptoService`, `AttributeConverter` ou uso de javax.crypto/Cipher foram encontradas). Portanto não há trecho de código a ser mostrado. A documentação aponta essa proteção como parcial — se existir em outro branch/ambiente, não está presente aqui.

- BCrypt — hashing de senhas (cadastro / login)

Trecho (bean de password encoder, arquivo: [src/main/java/br/com/fiap/autospec_api/config/SecurityConfig.java](src/main/java/br/com/fiap/autospec_api/config/SecurityConfig.java)):

```java
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
```

Trecho (uso no startup — cadastro inicial, arquivo: [src/main/java/br/com/fiap/autospec_api/config/CargaInicial.java](src/main/java/br/com/fiap/autospec_api/config/CargaInicial.java)):

```java
admin.setSenha(
    passwordEncoder.encode("Admin123")
);

analista.setSenha(
    passwordEncoder.encode("Analista123")
);
```

Trecho (uso no login, arquivo: [src/main/java/br/com/fiap/autospec_api/service/AuthService.java](src/main/java/br/com/fiap/autospec_api/service/AuthService.java)):

```java
boolean senhaCorreta =
    passwordEncoder.matches(
        login.getSenha(),
        usuario.getSenha()
    );
```

Explicação: BCrypt fornece hashing com salt e fator de custo adaptativo, protegendo senhas em caso de vazamento do banco de dados; `matches` compara a senha informada com o hash armazenado.

## Evidências de execução (curl)

Observação importante: os comandos abaixo assumem que a aplicação está rodando localmente em `http://localhost:8080`.

1) Obter JWT via login (admin)

```bash
# login admin e extrair token (requer jq)
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@autospec.com","senha":"Admin123"}' | jq -r '.token')

echo "ADMIN_TOKEN=$ADMIN_TOKEN"
```

2) Decodificar visualmente o payload do JWT

```bash
# decodificar payload (campo 2) e formatar com jq (se disponível)
printf '%s' "$ADMIN_TOKEN" | cut -d '.' -f2 | base64 --decode | jq .

# se base64 ou jq não estiver disponível, cole o token em https://jwt.io/
```

3) Teste de rate-limit (apenas se RateLimitingFilter estiver implementado)

Observação: o repositório atual não contém a implementação do RateLimitingFilter. O comando abaixo demonstra como testar rapidamente — se um rate-limiter estiver ativo, espere códigos `429` aparecerem.

```bash
# executar 20 requisições rápidas e mostrar códigos HTTP
for i in {1..20}; do \
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/veiculos; \
done
```

4) Falha de autorização: perfil ANALISTA tentando criar veículo (esperado 403)

```bash
# login analista e extrair token
ANALISTA_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"analista@autospec.com","senha":"Analista123"}' | jq -r '.token')

# analista tenta criar um veículo (endpoint restrito a ADMIN)
curl -i -X POST http://localhost:8080/api/veiculos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ANALISTA_TOKEN" \
  -d '{"marca":"Fiat","modelo":"Uno","versao":"1.0","especificacoes":[{"atributo":"motor","valor":"1.0"}]}'
```

Resposta esperada: `403 Forbidden` com o corpo JSON de erro gerado pelo `accessDeniedHandler` configurado em `SecurityConfig`.

---

Se desejar, posso procurar em branches remotos por implementações de `RateLimitingFilter` ou `CryptoService`, ou documentar como seria a implementação segura destes componentes. Quer que eu procure por essas implementações em branches remotos?
