---
inclusion: always
---

# FieldOps — Contexto do Projeto

> Este arquivo é lido automaticamente pelo agente Kiro em toda interação.
> Seu papel é garantir que cada ação de desenvolvimento esteja alinhada com o escopo, os padrões e as decisões técnicas do projeto.

---

## 🧭 Visão geral

O **FieldOps** é uma plataforma digital de inspeções em campo que substitui formulários impressos e planilhas por um fluxo digital integrado entre três frentes:

| Frente | Tecnologia | Função |
|---|---|---|
| **Mobile** | Expo + React Native + TypeScript | Técnicos executam inspeções em campo, inclusive offline |
| **Backend** | Java 17 + Spring Boot 3 + PostgreSQL | API REST central — autenticação, regras de negócio, persistência |
| **Admin Web** | Angular + TypeScript | Supervisores e admins gerenciam dados, modelos e revisões |

---

## 🔄 Fluxo principal do produto

```
1. Configuração administrativa (usuários, clientes, locais, equipamentos)
2. Criação e publicação do modelo de inspeção (Supervisor)
3. Agendamento e atribuição da inspeção ao Técnico
4. Sincronização da inspeção para o app mobile
5. Execução em campo (checklist, fotos, QR Code, localização — offline-first)
6. Sincronização dos dados executados
7. Revisão pelo Supervisor
8. Aprovação ou reprovação (com solicitação de correção se necessário)
```

**Toda funcionalidade implementada deve se encaixar em uma das etapas acima.**

---

## ✅ Escopo do MVP (o que deve ser entregue)

- [x] Cadastro básico: usuários, clientes, locais, equipamentos
- [x] Criação de modelos de inspeção com checklist pelo Supervisor
- [x] Agendamento e atribuição de inspeção a um Técnico
- [x] Recebimento da inspeção no app mobile
- [x] Execução do checklist com fotos, QR Code e localização GPS
- [x] Modo offline com sincronização posterior
- [x] Revisão, aprovação ou reprovação pelo Supervisor

## ❌ Fora do escopo do MVP (não implementar)

- App exclusivo para clientes finais
- Portal público
- Multiempresa / multi-tenant
- Faturamento e cobrança
- Roteirização automática
- Transmissão contínua de localização (rastreamento em tempo real)
- Chamadas de vídeo
- Assinatura eletrônica
- Integração com ERPs
- IA para diagnóstico
- Integração com IoT

---

## 🏗️ Arquitetura do Backend

### Stack
- Java 17 + Spring Boot 3.3.x
- Spring Web, Data JPA, Security, Validation
- PostgreSQL (produção) / H2 (testes)
- Flyway (migrações de banco)
- JWT stateless (autenticação)
- SpringDoc OpenAPI (Swagger UI)
- Lombok

### Estrutura de pacotes obrigatória
```
com.fieldops
├── config/        # SecurityConfig, CorsConfig, OpenApiConfig
├── controller/    # Controllers REST
├── dto/           # Request e Response DTOs (nunca expor Entity diretamente)
├── entity/        # Entidades JPA
├── enums/         # Role, StatusInspecao, TipoItem, etc.
├── exception/     # GlobalExceptionHandler, ResourceNotFoundException
├── repository/    # Interfaces JpaRepository
├── service/       # Interfaces de serviço
│   └── impl/      # Implementações
├── security/      # JwtFilter, JwtUtil, UserDetailsServiceImpl
└── util/          # Classes utilitárias gerais
```

### Padrões obrigatórios no backend
- Controllers nunca contêm regra de negócio — apenas recebem, validam e delegam
- Services nunca retornam Entity — sempre retornam DTOs
- Toda entrada HTTP deve ser validada com `@Valid` + Bean Validation
- Exceções de domínio lançadas no Service, capturadas pelo `GlobalExceptionHandler`
- Toda entidade principal deve ter: `id`, `createdAt`, `updatedAt`, `deletedAt` (soft delete)
- Auditoria via `@EntityListeners(AuditingEntityListener.class)`
- Roles: `ADMIN`, `SUPERVISOR`, `TECNICO` — controle via `@PreAuthorize`

### Padrão de resposta da API
```json
// Sucesso
{ "data": { ... }, "message": "...", "timestamp": "..." }

// Erro
{ "status": 404, "message": "...", "timestamp": "..." }
```

---

## 🗂️ Modelo de domínio (entidades principais)

| Entidade | Descrição |
|---|---|
| `User` | Usuário do sistema com Role |
| `Client` | Cliente dono dos locais |
| `Location` | Local físico (vinculado ao Client) |
| `Equipment` | Equipamento com QR Code único (vinculado ao Location) |
| `InspectionTemplate` | Modelo de inspeção criado pelo Supervisor |
| `ChecklistItem` | Item do checklist de um template |
| `InspectionOrder` | Ordem de inspeção agendada e atribuída |
| `InspectionExecution` | Execução real pelo Técnico |
| `ChecklistResponse` | Resposta de cada item durante execução |
| `Evidence` | Foto/arquivo anexado na execução |
| `Review` | Revisão do Supervisor sobre a execução |
| `SyncLog` | Registro de sincronização offline→online |

---

## 📐 Padrões de código

### Nomenclatura
- Classes: `PascalCase`
- Métodos e variáveis: `camelCase`
- Constantes: `UPPER_SNAKE_CASE`
- Endpoints REST: `kebab-case` (ex: `/inspection-orders`)
- Branches Git: `feature/nome-da-funcionalidade`, `fix/descricao-do-bug`
- Títulos de issues: `[Backend] Título`, `[Mobile] Título`, `[Admin] Título`

### Endpoints REST — padrão de URLs
```
GET    /api/v1/{recurso}          → listar
GET    /api/v1/{recurso}/{id}     → buscar por id
POST   /api/v1/{recurso}          → criar
PUT    /api/v1/{recurso}/{id}     → atualizar completo
PATCH  /api/v1/{recurso}/{id}     → atualizar parcial
DELETE /api/v1/{recurso}/{id}     → remover (soft delete)
```

### Segurança
- Endpoints públicos apenas em `/api/public/**`
- Todo endpoint privado requer JWT válido no header `Authorization: Bearer <token>`
- Nunca expor stacktrace em respostas de produção
- Nunca logar senhas, tokens ou dados sensíveis

---

## ✔️ Checklist de conformidade — verificar a cada implementação

Antes de considerar qualquer funcionalidade concluída, confirmar:

- [ ] A funcionalidade está dentro do escopo do MVP?
- [ ] A estrutura de pacotes foi respeitada?
- [ ] Controller delega para Service sem conter regra de negócio?
- [ ] Service retorna DTO (não Entity)?
- [ ] Entrada validada com `@Valid`?
- [ ] Exceções tratadas pelo `GlobalExceptionHandler`?
- [ ] Endpoint segue o padrão `/api/v1/{recurso}`?
- [ ] Autorização por Role configurada (`@PreAuthorize`)?
- [ ] Entidade tem `createdAt`, `updatedAt`, `deletedAt`?
- [ ] Migração Flyway criada para alterações no banco?
- [ ] Endpoint documentado no Swagger (SpringDoc)?
- [ ] Branch segue o padrão `feature/` ou `fix/`?

---

## 🔗 Referências rápidas

- Board Backend: `https://github.com/orgs/experience-fieldops/projects/2`
- Repositório: `https://github.com/experience-fieldops/fieldops`
- Padrão de issue: `[Backend] Título do card` → status inicial: **Backlog**
