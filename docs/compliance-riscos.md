# Compliance & Análise de Riscos — AutoSpec

Documento consolidado com a matriz STRIDE, mapeamentos OWASP (ASVS nível 1, API Top 10, Mobile Top 10), LGPD, plano de segurança contínua e checklist de conformidade final.

---

**SUMÁRIO**
- STRIDE (tabela)
- OWASP API Top 10 (mapeamento)
- OWASP ASVS Level 1 (mapeamento focado)
- OWASP Mobile Top 10 (pontos de atenção para mobile)
- LGPD — mapeamento de dados, proteções e pendências
- Plano de Segurança Contínua (rotinas)
- Checklist de conformidade final (marcável)

---

## 1) STRIDE — Riscos, Mitigações Implementadas e Risco Residual

| Categoria | Risco identificado | Mitigação implementada | Risco residual |
|---|---|---|---|
| Spoofing | Credenciais roubadas / tokens forjados (acesso por identidade) | Senhas com BCrypt; JWT assinados com verificação de issuer/exp; refresh tokens persistidos/rotacionados; RateLimitingFilter (Bucket4j); logs estruturados. | Compromisso de `jwt.secret`/`crypto.secret`; falta de MFA; ausência de blacklist para access tokens; segredos em configs/backups. |
| Tampering | Modificação não autorizada de dados (veículos/especificações) | AES‑GCM para campos sensíveis (`Usuario.nome`, `Especificacao.valor`); validação `@Valid`/`@Size`; `@PreAuthorize`; audit logs. | Cobertura parcial da criptografia; ausência de assinaturas de payloads; possibilidade de trânsito sem TLS forçado em deploy. |
| Repudiation | Usuário nega ações críticas (ex.: delete/profile_change) | Logs estruturados com `actor`, `userId`, timestamps; persistência de refresh tokens para correlação. | Logs não imutáveis (WORM) nem cadeia de custódia formalizada; retenção/regras não declaradas. |
| Information Disclosure | Vazamento de PII, tokens, segredos | Criptografia de alguns campos; Gitleaks/Snyk/Dependabot na pipeline; logs evitam senhas. | Cobertura parcial; secrets em `application.properties`; logs centralizados precisam controle de acesso; claims JWT podem expor dados. |
| Denial of Service | Exaustão por requisições massivas | RateLimitingFilter por IP; métricas propostas (Prometheus). | Ataques distribuídos (DDoS) não cobertos; falta WAF/CDN/edge protection; circuit breakers/timeouts downstream ausentes. |
| Elevation of Privilege | Escalada indevida (Brigadista -> Gestor/Admin) | `@PreAuthorize` por endpoint; roles modeladas; audit logs. | Possíveis gaps se checks existirem só no controller e não em serviços/DAO; falta de testes que validem autorização em profundidade. |

---

## 2) OWASP API Top 10 — Mapeamento (resumo)
- API1 — Broken Object Level Authorization (BOLA): atende parcialmente — `@PreAuthorize` aplicado; falta verificação consistente a nível de objeto/row.
- API2 — Broken User Authentication: atende parcialmente — BCrypt, JWT verify, refresh tokens e rate limiting; falta MFA e blacklist de access tokens.
- API3 — Excessive Data Exposure: atende parcialmente — criptografia de campos, mas falta projeção consistente antes de retornar dados.
- API4 — Lack of Resources & Rate Limiting: atende parcialmente — Bucket4j implementado; falta proteção distribuída/edge (WAF/CDN).
- API5 — Broken Function Level Authorization: atende parcialmente — `@PreAuthorize` presente; falta validação em camadas inferiores e testes automatizados.
- API6 — Mass Assignment: atende parcialmente — uso de DTOs e validação; falta whitelist centralizada contra binding indiscriminado.
- API7 — Security Misconfiguration: atende parcialmente — pipeline DevSecOps presente; falta padronizar secrets e TLS em ambiente de produção; Trivy não ativo sem `Dockerfile`.
- API8 — Injection: atende parcialmente — validação e uso de Spring Data JPA reduzem risco; auditar queries nativas necessárias.
- API9 — Improper Inventory Management: atende parcialmente — documentação do pipeline existe; falta inventário completo de endpoints e imagens.
- API10 — Insufficient Logging & Monitoring: atende parcialmente — logs JSON e métricas propostas; falta centralização, retenção e alerting já ativado.

---

## 3) OWASP ASVS (Level 1) — Mapeamento focalizado
Foco: autenticação, sessão, validação de entrada e criptografia.

- V2.1 Password storage: atende — BCrypt.
- V2.2 Password policy & complexity: atende parcialmente — `@Size` aplicado; falta política de complexidade e checks de senha fraca.
- V2.3 Multi-factor auth (MFA): não atende — não implementado.
- V2.4 Account lockout / anti-brute-force: atende parcialmente — rate limit por IP implementado; falta lockout por conta.
- V2.6 Token-based auth (refresh flow): atende — refresh tokens persistidos e rotacionados.
- V2.8 Token revocation / logout: atende parcialmente — refresh tokens podem ser removidos; falta blacklist para access tokens.
- V3.1 Token integrity & signing: atende — JWT assinados e validados (issuer/exp).
- V3.2 Token lifetime & rotation: atende parcialmente — rotation implementado; access-token blacklist ausente.
- V3.3 Secure storage of session tokens: atende parcialmente — refresh tokens no DB; acesso tokens stateless.
- V5.1 Server-side input validation: atende — DTOs com `@Valid` e anotações.
- V5.2 Whitelist input model: atende parcialmente — DTOs reduzem binding; falta defesa centralizada contra mass assignment.
- V5.3 Output encoding / response filtering: atende parcialmente — falta camada de projeção para remover campos sensíveis sistematicamente.
- V6.1 Use of approved algorithms: atende parcialmente — AES‑GCM e BCrypt usados; JWT HS256 usado (aceitável se chave segura); RS256 não implementado.
- V6.2 Key management: não atende — segredos em `application.properties`; falta secret manager e rotação automática.
- V6.3 Data encryption at rest: atende parcialmente — campos específicos encriptados; cobertura parcial.
- V6.4 Avoid logging secrets: atende — logs projetados para não incluir senhas.

---

## 4) OWASP Mobile Top 10 — Pontos de Atenção para a equipe Mobile
(Observação: código mobile não está no repositório; abaixo recomendações para integração segura com a API.)

- M1 Improper Platform Usage: usar Keystore/Keychain, EncryptedSharedPreferences, evitar embedding de secrets.
- M2 Insecure Data Storage: armazenar tokens em Keystore/Keychain; encriptar DB local; evitar logs de tokens/PII.
- M3 Insecure Communication: TLS 1.2+, validar certificado, considerar certificate pinning com plano de rotação.
- M4 Insecure Authentication: usar OAuth2/OIDC com PKCE, biometria local para ações sensíveis, não embutir client_secret.
- M5 Insufficient Cryptography: usar primitives do SO (no custom crypto), hardware-backed keys.
- M6 Insecure Authorization: não confiar no cliente para decisões de autorização; backend deve aplicar checks object-level.
- M7 Client Code Quality / Reverse Engineering: obfuscação (ProGuard/R8), remover símbolos, detectar root/jailbreak e mitigar.
- M8 Code Tampering: checks de integridade, validar assinatura do app, monitorar clients tampered.
- M9 Reverse Engineering Sensitive Info: nunca embutir secrets; servidor assume cliente público.
- M10 Extraneous Functionality: remover debug flags/endpoints, evitar admin hidden in releases.

---

## 5) LGPD — Mapeamento de Dados, Proteções e Pendências

### Dados Pessoais coletados / possíveis
- Nome, email, senha (hash), userId, perfil/role, telefone (se aplicado).
- Dados de autenticação: tokens (access/refresh), logs de login, IPs.
- Telemetria / IoT: especificações técnicas de veículos, status técnico, event logs enviados por dispositivos.
- Localização: latitude/longitude e histórico de rotas (se mobile/IoT enviar).
- Metadados: IP, user-agent, timestamps, endpoints acessados, erros.

### Proteções implementadas (resumo)
- Senhas: `BCrypt`.
- Criptografia: AES‑GCM aplicado a `Usuario.nome` e `Especificacao.valor` (parcial).
- Tokens: refresh tokens persistidos/rotacionados; JWTs assinados com verificação de issuer/exp.
- Pipeline: Gitleaks/Snyk/Dependabot para reduzir exposição via dependências e secrets.
- Logs: projetados para não registrar senhas; logs estruturados para auditoria.

### Pendências a formalizar / implementar
- Inventário completo de PII no banco (data map) e cobertura de criptografia.
- Termo de consentimento / base legal para telemetria e localização.
- Política de retenção e eliminação de dados (retention policy) e processos para direitos do titular (acesso/correção/eliminação).
- Mover segredos para secret manager; documentar rotação e acesso.
- Armazenamento e controle de acesso a logs centralizados; retenção e anonimização quando aplicável.

---

## 6) Plano de Segurança Contínua (rotinas executáveis)

1) Revisão de dependências
- Frequência: Dependabot PRs triadas diariamente; Snyk full scan semanal.
- Responsáveis: Tech Lead / Security Owner (triage) + On‑call devs.
- SLA: Critical/High 24h; Medium 3 dias; Low 14 dias.
- Ação: aplicar patch + CI (mvn test + SAST) ou abrir issue para correção.

2) Testes de segurança
- On PR: Semgrep, Snyk, Gitleaks — bloquear merge se Critical/High.
- Scheduled: full Semgrep + Snyk nightly; Trivy semanal se Dockerfile existir.
- Manual pentest: semestralmente/antes de major release; contratar externo anualmente se possível.
- Security regression tests: adicionar testes unit/integration que verifiquem autorização e cenários críticos.

3) Auditoria de permissões
- Periodicidade: revisão trimestral de usuários e permissões; admins review mensal.
- Processo: exportar inventário (users/roles), Security Owner + Product Owner aprovam mudanças.
- Critérios: menor privilégio; bloquear contas inativas >90 dias.

4) Backup & Recovery (runbook padrão)
- Backups DB: full diários + incrementais; snapshots cada 15 min quando suportado.
- Retenção: diários 90 dias; semanais/mensais até 1 ano (ajustar conforme legal).
- Criptografia: backups encriptados (AES‑256); chaves em secret manager.
- Testes: restore mensal em staging; DR drill semestral.
- RTO/RPO alvo (sugestão): RTO <4h, RPO <1h (ajustar conforme SLA).
- Controles: acesso a backups restrito e com approvals documentados.

Observação: incorpore detalhes da Fase 3 de backup (ferramentas, periodicidade real, RTO/RPO reais) quando fornecidos para ajustar runbook.

---

## 7) Checklist de Conformidade Final (marcável)

**STRIDE**
- [x] Spoofing — BCrypt, JWT signature, refresh tokens, rate limiter.
- [ ] Spoofing — MFA e blacklist de access tokens.
- [x] Tampering — AES‑GCM (campos), validação DTOs, `@PreAuthorize`.
- [ ] Tampering — Cobertura completa de criptografia e assinatura de payloads.
- [x] Repudiation — Logs estruturados com `actor`/`userId`.
- [ ] Repudiation — Logs imutáveis e cadeia de custódia.
- [x] Information Disclosure — Gitleaks/Snyk/Dependabot; logs sem senhas.
- [ ] Information Disclosure — Secret manager e migração de claims sensíveis.
- [x] Denial of Service — RateLimitingFilter implementado.
- [ ] Denial of Service — WAF/CDN e proteção distribuída.
- [x] Elevation of Privilege — `@PreAuthorize` e roles.
- [ ] Elevation of Privilege — Checks objeto/DAO e testes.

**OWASP API Top 10**
- [x] API1 BOLA — parcial (`@PreAuthorize`).
- [x] API2 Auth — parcial (JWT+refresh+BCrypt).
- [x] API3 Excessive Data Exposure — parcial (criptografia parcial).
- [x] API4 Rate Limiting — parcial (Bucket4j).
- [x] API5 Function Level Auth — parcial (`@PreAuthorize`).
- [x] API6 Mass Assignment — parcial (DTOs/validation).
- [x] API7 Misconfiguration — parcial (pipeline exists).
- [x] API8 Injection — parcial (validation + JPA).
- [x] API9 Inventory — parcial (docs exist; inventário faltando).
- [x] API10 Logging & Monitoring — parcial (logs JSON; alerting pending).

**ASVS Level 1**
- [x] Password storage: BCrypt.
- [ ] MFA for privileged users.
- [x] JWT signing & validation.
- [ ] Key management / secret manager.
- [x] Server-side validation (DTOs).
- [ ] Output filtering / projection systematic.

**Mobile**
- [ ] Keystore/Keychain secure token storage (mobile team).
- [ ] TLS+certificate validation + pinning plan.
- [ ] Obfuscation and tamper detection for mobile builds.

**LGPD & Dados**
- [x] Identificação inicial de PII e telemetria.
- [x] Proteção técnica parcial (hash/some encryption).
- [ ] Data Map formalizado e publicado.
- [ ] Privacy Policy / Consent / Retention Policy.
- [ ] Procedures for Data Subject Rights (access, deletion).

**Continuous Security & Ops**
- [x] SAST/SCA/Secret scan in pipeline.
- [x] Rate limiter and logs implemented.
- [ ] Secret manager integration.
- [ ] Centralized alerting & dashboards (Grafana/Loki).
- [ ] Scheduled pentest and restore drills.

**Backups**
- [ ] Backup automation & documented RTO/RPO (incorporar Fase 3).
- [x] Runbook template exists in docs.
- [ ] Monthly restore tests scheduled and executed.

---

## 8) Próximos passos recomendados (prioridade)
1. Mover segredos (`jwt.secret`, `crypto.secret`, DB creds) para secret manager e documentar rotação.
2. Implementar blacklist para access tokens e endpoint administrativo para revogação de refresh tokens.
3. Incluir MFA para perfis com maior privilégio (Gestor/Admin).
4. Adicionar testes de autorização em nível de serviço/DAO e cobrir BOLA com testes automatizados.
5. Centralizar logs e ativar alerting no Grafana/Loki; criar playbooks acionáveis.
6. Formalizar Data Map, Privacy Policy e Retention Policy; definir DPO/contato.
7. Programar pentest semestral e DR drills para backups.

---

Arquivo gerado automaticamente com base na sessão de trabalho e nos documentos existentes em `docs/`. Para incorporar detalhes exatos de backup da Fase 3, forneça o texto e eu atualizo o runbook com periodicidade, RTO/RPO e ferramentas reais.
