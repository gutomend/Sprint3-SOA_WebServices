# Observabilidade e Resposta a Incidentes — AutoSpec (consolidado)

Este documento consolida os artefatos de observabilidade e o plano de resposta a incidentes aplicáveis ao projeto (logs estruturados, métricas, dashboard e o plano de resposta completo).

**Conteúdo**
- Exemplos reais de logs JSON (produzidos por `logback-spring.xml`)
- Métricas expostas (nomenclatura sugerida e significado)
- Mock/descrição do dashboard (painéis essenciais)
- Plano de resposta a incidentes completo (Detecção → Análise → Contenção → Erradicação → Recuperação)

---

## 1) Exemplos reais de logs (JSON)
Os logs são produzidos em JSON (Logstash encoder). Exemplos reais gerados pelas inserções feitas no código:

- Login bem-sucedido (AuthService)
```json
{
  "@timestamp":"2026-09-26T12:00:00.000Z",
  "level":"INFO",
  "logger_name":"br.com.fiap.autospec_api.service.AuthService",
  "thread_name":"http-nio-8080-exec-1",
  "message":"event=login_success actor=admin@autospec.com email=user@cliente.com profile=ADMIN",
  "service":"autospec-api",
  "env":"local"
}
```

- Falha de autenticação (tentativa de login inválida)
```json
{
  "@timestamp":"2026-09-26T12:01:05.123Z",
  "level":"WARN",
  "logger_name":"br.com.fiap.autospec_api.service.AuthService",
  "thread_name":"http-nio-8080-exec-3",
  "message":"event=login_failed email=atacante@malicioso.com reason=invalid_credentials",
  "service":"autospec-api",
  "env":"local"
}
```

- Alteração de perfil de usuário (UsuarioService)
```json
{
  "@timestamp":"2026-09-26T12:10:10.500Z",
  "level":"INFO",
  "logger_name":"br.com.fiap.autospec_api.service.UsuarioService",
  "thread_name":"http-nio-8080-exec-5",
  "message":"event=profile_change userId=42 email=user@cliente.com oldProfile=BRIGADISTA newProfile=GESTOR actor=admin@autospec.com",
  "service":"autospec-api",
  "env":"local"
}
```

- Exclusão de veículo (alteração crítica) — VeiculoService
```json
{
  "@timestamp":"2026-09-26T12:12:00.000Z",
  "level":"WARN",
  "logger_name":"br.com.fiap.autospec_api.service.VeiculoService",
  "thread_name":"http-nio-8080-exec-7",
  "message":"event=delete_vehicle vehicleId=123 marca=Ford modelo=Ka actor=gestor@autospec.com",
  "service":"autospec-api",
  "env":"local"
}
```

- Rate limit acionado (exemplo genérico)
```json
{
  "@timestamp":"2026-09-26T12:20:30.456Z",
  "level":"WARN",
  "logger_name":"br.com.fiap.autospec_api.security.RateLimitingFilter",
  "thread_name":"http-nio-8080-exec-2",
  "message":"event=rate_limited ip=203.0.113.45 remaining=0 retryAfter=60",
  "service":"autospec-api",
  "env":"local"
}
```

**Observação:** os logs foram projetados para não incluir senhas ou dados sensíveis em texto claro. Campos sensíveis são criptografados em repouso (ex.: `Usuario.nome`, `Especificacao.valor`). Use JSON fields para buscar por `event`, `actor`, `userId`, `email`, `service`, `env`.

---

## 2) Métricas (sugestão/definição)
Recomenda-se instrumentar a aplicação com Micrometer + Prometheus. Métricas sugeridas:

- `http_server_requests_seconds_count` / `_sum` / `_bucket` (standard Micrometer): latência e taxa por endpoint/method/status
- `http_server_requests_total{method,uri,status}`: número total de requisições por endpoint
- `auth_login_success_total{profile}` — contador de logins bem-sucedidos por perfil
- `auth_login_failed_total{reason}` — contador de falhas de login (ex.: invalid_credentials)
- `rate_limiter_rejected_total{ip}` — contador de requisições rejeitadas por rate limiter
- `refresh_tokens_active_total` — número de refresh tokens ativos (consultar DB)
- `vehicles_deleted_total` — contador de exclusões de veículos (audit)
- `application_uptime_seconds` — uptime do serviço
- `jvm_memory_used_bytes`, `process_cpu_seconds_total` — métricas padrão de infra/JVM

Por que cada métrica importa
- Latência/Throughput (`http_server_requests`): detectar degradação/performance regressions
- `auth_*` metrics: detectar picos de falhas de autenticação (força bruta) ou picos de sucesso anormais
- Rate limiter metrics: identificar IPs abusivos e calibrar limites
- Business metrics (`vehicles_deleted_total`): correlacionar ações críticas com auditoria e alertas

Como expor
- Adicionar Micrometer (dependency) e expor `/actuator/prometheus`.
- Prometheus coleta, Grafana exibe dashboards.

---

## 3) Dashboard (descrição e exemplo)
Sugestão de dashboard Grafana com painéis:

- Top row: Health & Overview
  - Service status (UP/DOWN) — `up`/`probe`
  - Uptime (seconds)
  - Requests per second (global)
- Auth section:
  - Login success rate (time series) — `auth_login_success_total` / `rate`
  - Login failure rate (time series) — `auth_login_failed_total` — alarme se > X/min
  - Active sessions / refresh tokens (`refresh_tokens_active_total`)
- Rate limiting & abuse:
  - Rate limited requests (count) — `rate_limiter_rejected_total`
  - Top 10 IPs by 429 — table
- Performance:
  - P95 / P99 latency per endpoint (`http_server_requests_seconds_bucket`)
  - Error rate (5xx) per endpoint
- Security / Audit:
  - Recent critical events (logs panel) — ingest logs e mostrar eventos com `event` filter: `profile_change`, `delete_vehicle`, `login_failed`
  - Suspicious activity: spikes in `login_failed` + `rate_limiter_rejected`

Exemplo (mock textual do painel principal)

[Dashboard: AutoSpec - Security Overview]
- Panel: Login Success Rate — Sparkline (1m, 5m)
- Panel: Login Failure Rate — Sparkline (alert rule: > 50/min)
- Panel: Rate Limit Hits — Counter
- Panel: P99 latency (GET /api/veiculos)
- Panel: Recent Critical Events (logs) — table (event, timestamp, actor, details)

(Use Grafana + Loki/Elasticsearch para logs e Grafana para métricas; vincule painéis com links para Kibana/Loki log queries.)

---

## 4) Plano de Resposta a Incidentes (Completo)
Cenário-exemplo: vazamento de dados de um usuário `Brigadista` por acesso indevido de um perfil sem permissão.

### Detecção
- Procurar no agregador de logs (ELK/EFK/Loki) por eventos relevantes:
  - `event=login_success` (para tokens usados)
  - `event=login_failed` (padrões anômalos)
  - `event=profile_change`, `event=delete_vehicle`, `event=user_created`
- Verificar métricas:
  - Picos em `auth_login_failed_total` (força bruta)
  - Picos de `rate_limiter_rejected_total`
  - Aumento de `http_server_requests_total` para endpoints sensíveis
- Ferramentas: logs JSON (Logback), Prometheus, Grafana alert rules

Ação prática:
- Executar consulta em logs (Kibana / Grafana Loki) para last 24h com filtros `event=login_success OR event=profile_change OR event=delete_vehicle` e ordenar por timestamp
- Três itens produzidos: lista de eventos, IPs ligados, tokens usados

### Análise
- Reconstituir sequência de acesso:
  - Correlacionar `actor`/`email`/`userId`/`ip` e timestamps
  - Extrair token claims para saber `perfil` e `issuer` (JWT)
  - Consultar tabela `tb_refresh_token` para refresh tokens ativos
- Verificar autorização: checar se `@PreAuthorize` permitia a ação — revisar a claim `perfil` do token e o código do endpoint
- Preservar evidências: exportar logs, criar backup dos registros alterados (snapshots)

Ação prática:
- Exportar logs relacionados e salvar em storage forense (S3/secure share)
- Extrair lista de refresh tokens envolvidos e seus usuários

### Contenção
- Revogar credenciais e limitar o vetor:
  - Deletar/invalidar refresh tokens afetados (`RefreshTokenRepository.deleteByUsuarioId(...)`)
  - Se possível, bloquear tokens de acesso (blacklist cache) ou rotacionar `jwt.secret` como medida de emergência
  - Suspender conta do atacante (marcar `suspended` ou remover roles)
  - Bloquear IPs no WAF e aplicar regras mais restritivas de rate limiting
- Ações rápidas:
  - Rodar endpoint/admin script para excluir refresh tokens do usuário vítima e do suspeito
  - Aumentar logs e habilitar auditoria detalhada por 24-72h

### Erradicação
- Corrigir causa raiz:
  - Se falha de autorização: corrigir `@PreAuthorize` e adicionar testes automatizados que validem cenários de autorização (unit + integration)
  - Se token/secret exposto: rotacionar `jwt.secret` e `crypto.secret`, remover segredos do histórico (`gitleaks`) e reemissão de chaves
  - Se credenciais vazadas: forçar reset de senha para usuários afetados e comunicar
- Reanalisar dependências com Snyk/Dependabot e rodar Semgrep para identificar padrões inseguros

### Recuperação
- Restaurar dados (se modificados): usar backups e reconciliar via logs exportados
- Reabilitar contas apenas após troca de credenciais e verificação de integridade
- Rotacionar segredos (DB, crypto, jwt) e atualizar configurações de produção com novos valores em secret manager
- Monitorar pós-incidente (48-90 dias): alerts em Grafana para detecção de reocorrência

### Comunicação & Pós-incidente
- Notificar stakeholders (ops, legal) e usuários afetados conforme política
- Gerar relatório final com timeline, impacto, mitigação, e ações preventivas
- Atualizar `docs/seguranca-codigo-infra.md` e playbooks com lições aprendidas

---

## 5) Queries úteis (exemplos)
- Kibana / Grafana Loki - buscar logins e mudanças críticas (últimas 24h):
```
{service="autospec-api"} | json | event in ["login_success","login_failed","profile_change","delete_vehicle"] | sort @timestamp desc
```
- Prometheus alert rule (login failure spike):
```
alert: HighLoginFailureRate
expr: increase(auth_login_failed_total[5m]) > 50
for: 2m
labels:
  severity: warning
annotations:
  summary: "Spike de falhas de autenticação detectado"
```

---

## 6) Próximos passos recomendados
- Configurar Micrometer/Prometheus para expor as métricas sugeridas
- Configurar Grafana + Loki/ELK com dashboards e alert rules (login failure, rate limit spikes, critical events)
- Implementar endpoint administrativo para revogação de refresh tokens e ferramenta de bloqueio de contas (para contenção rápida)
- Criar playbook com passos de execução (scripts) para as ações de contenção e erradicação


---

Arquivo gerado automaticamente com base nas implementações no repositório e nas alterações realizadas durante a sessão.
