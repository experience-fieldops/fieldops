---
inclusion: manual
---

# FieldOps — Backlog Completo · Conferência de Cards

> Revise cada card abaixo nas três frentes: **Backend**, **Mobile** e **Admin Web**.
> Quando estiver de acordo, diga: **"criar todos os cards aprovados"**
> Para criar apenas uma frente: **"criar cards de backend"**, **"criar cards de mobile"** ou **"criar cards de admin"**

---

## 🟢 BACKEND — Java 17 + Spring Boot + PostgreSQL

### B01 ✅
**Título:** `[Backend] Setup inicial do projeto Spring Boot`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Configurar o projeto base com todas as dependências, estrutura de pacotes e ferramentas.
**Tarefas:**
- [ ] Adicionar dependências: Web, JPA, Security, Validation, Flyway, PostgreSQL, Lombok, SpringDoc, JJWT
- [ ] Criar estrutura de pacotes: `config`, `controller`, `dto`, `entity`, `enums`, `exception`, `repository`, `service/impl`, `security`, `util`
- [ ] Configurar `application.properties` (dev) e `application-test.properties` (testes com H2)
- [ ] Configurar CORS para origens Angular e Expo
- [ ] Configurar Swagger/OpenAPI em `/swagger-ui.html`
- [ ] Criar primeira migração Flyway `V1__create_schema.sql`

---

### B02 ✅
**Título:** `[Backend] Autenticação JWT stateless`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Implementar autenticação JWT com geração, validação e integração com Spring Security.
**Tarefas:**
- [ ] Criar `JwtUtil`, `JwtAuthenticationFilter`, `UserDetailsServiceImpl`
- [ ] Configurar `SecurityFilterChain` com rotas públicas e privadas
- [ ] `POST /api/public/auth/login` → retorna token JWT
- [ ] `POST /api/public/auth/register` → cadastro público (somente admin cria demais usuários)
- [ ] Retornar `401` para token inválido e `403` para role sem permissão

---

### B03 ✅
**Título:** `[Backend] Tratamento global de erros e padrão de resposta`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Padronizar todas as respostas da API e centralizar exceções.
**Tarefas:**
- [ ] Criar `ApiResponse<T>` com campos `data`, `message`, `timestamp`
- [ ] Criar `GlobalExceptionHandler` com `@RestControllerAdvice`
- [ ] Tratar: 404, 400, 403, 500
- [ ] Nunca expor stacktrace em produção

---

### B04 ✅
**Título:** `[Backend] CRUD de Usuários e Roles`
**Grupo:** Cadastros
**Prioridade:** 🔴 Alta
**Descrição:** Gerenciamento de usuários com perfis ADMIN, SUPERVISOR e TECNICO.
**Tarefas:**
- [ ] Entidade `User`: `id`, `name`, `email`, `password`, `role`, `createdAt`, `updatedAt`, `deletedAt`
- [ ] Enum `Role`: ADMIN, SUPERVISOR, TECNICO
- [ ] Migração Flyway para tabela `users`
- [ ] Endpoints CRUD completo (somente ADMIN)
- [ ] Senha com BCrypt, nunca retornar campo `password`

---

### B05 ✅
**Título:** `[Backend] CRUD de Clientes`
**Grupo:** Cadastros
**Prioridade:** 🟡 Média
**Descrição:** Cadastro de clientes donos dos locais de inspeção.
**Tarefas:**
- [ ] Entidade `Client`: `id`, `name`, `document`, `phone`, `email`, `createdAt`, `updatedAt`, `deletedAt`
- [ ] Migração Flyway para tabela `clients`
- [ ] Endpoints CRUD (ADMIN e SUPERVISOR)

---

### B06 ✅
**Título:** `[Backend] CRUD de Locais (Locations)`
**Grupo:** Cadastros
**Prioridade:** 🟡 Média
**Descrição:** Locais físicos vinculados a um cliente com suporte a coordenadas GPS.
**Tarefas:**
- [ ] Entidade `Location`: `id`, `name`, `address`, `city`, `state`, `latitude`, `longitude`, `client` (FK), `createdAt`, `updatedAt`, `deletedAt`
- [ ] Migração Flyway para tabela `locations`
- [ ] Endpoints CRUD + `GET /api/v1/clients/{clientId}/locations`

---

### B07 ✅
**Título:** `[Backend] CRUD de Equipamentos com QR Code`
**Grupo:** Cadastros
**Prioridade:** 🟡 Média
**Descrição:** Equipamentos vinculados a locais, com QR Code único gerado automaticamente.
**Tarefas:**
- [ ] Entidade `Equipment`: `id`, `name`, `description`, `serialNumber`, `qrCode`, `location` (FK), `createdAt`, `updatedAt`, `deletedAt`
- [ ] Gerar `qrCode` único no cadastro (UUID)
- [ ] Endpoint `GET /api/v1/equipments/qrcode/{qrCode}` (usado pelo mobile)
- [ ] Migração Flyway para tabela `equipments`

---

### B08 ✅
**Título:** `[Backend] CRUD de Templates de Inspeção e Checklist`
**Grupo:** Modelos
**Prioridade:** 🔴 Alta
**Descrição:** Criação de modelos de inspeção com itens de checklist pelo Supervisor.
**Tarefas:**
- [ ] Entidade `InspectionTemplate` com status DRAFT/PUBLISHED
- [ ] Entidade `ChecklistItem` com tipo TEXT, BOOLEAN, NUMBER, PHOTO
- [ ] Migrações Flyway para `inspection_templates` e `checklist_items`
- [ ] Endpoints CRUD + `PATCH /{id}/publish`
- [ ] Endpoints para gerenciar itens do checklist

---

### B09 ✅
**Título:** `[Backend] CRUD de Ordens de Inspeção`
**Grupo:** Planejamento
**Prioridade:** 🔴 Alta
**Descrição:** Agendamento e atribuição de inspeções a técnicos.
**Tarefas:**
- [ ] Entidade `InspectionOrder` com status PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
- [ ] Migração Flyway para `inspection_orders`
- [ ] Endpoint `PATCH /{id}/assign` para atribuir técnico
- [ ] Endpoint `GET /my-orders` para o técnico autenticado

---

### B10 ✅
**Título:** `[Backend] Execução de Inspeção e respostas do checklist`
**Grupo:** Execução
**Prioridade:** 🔴 Alta
**Descrição:** Registro da execução real da inspeção e respostas de cada item do checklist.
**Tarefas:**
- [ ] Entidade `InspectionExecution`: início, fim, status, GPS
- [ ] Entidade `ChecklistResponse`: valor por item
- [ ] Migrações Flyway
- [ ] Endpoints para iniciar, responder e finalizar execução (TECNICO)

---

### B11 ✅
**Título:** `[Backend] Upload e gestão de Evidências (fotos)`
**Grupo:** Execução
**Prioridade:** 🟡 Média
**Descrição:** Upload de fotos como evidências durante a execução da inspeção.
**Tarefas:**
- [ ] Entidade `Evidence`: fileName, fileUrl, fileSize, mimeType
- [ ] Migração Flyway para `evidences`
- [ ] Endpoint `POST /executions/{id}/evidences` (multipart)
- [ ] Validar tipos (jpg, png, webp) e tamanho máximo

---

### B12 ✅
**Título:** `[Backend] Endpoint de sincronização offline`
**Grupo:** Sincronização
**Prioridade:** 🔴 Alta
**Descrição:** Receber em lote dados coletados offline pelo app mobile.
**Tarefas:**
- [ ] Entidade `SyncLog` com status SUCCESS/PARTIAL/FAILED
- [ ] Migração Flyway para `sync_logs`
- [ ] `POST /api/v1/sync` → processa payload com execuções, respostas e evidências
- [ ] Retornar relatório `{ synced: [], failed: [] }`
- [ ] `GET /my-orders/pending-sync` → ordens disponíveis para download offline

---

### B13 ✅
**Título:** `[Backend] Revisão de Inspeções pelo Supervisor`
**Grupo:** Revisão
**Prioridade:** 🔴 Alta
**Descrição:** Aprovação, reprovação ou solicitação de correção de execuções concluídas.
**Tarefas:**
- [ ] Entidade `Review` com status APPROVED, REJECTED, CORRECTION_REQUESTED
- [ ] Migração Flyway para `reviews`
- [ ] Endpoints para listar pendentes, criar e atualizar revisão
- [ ] Ao reprovar com correção, retornar ordem para IN_PROGRESS

---

### B14 ✅
**Título:** `[Backend] Documentação OpenAPI completa`
**Grupo:** Qualidade
**Prioridade:** 🟡 Média
**Descrição:** Documentar todos os endpoints no Swagger com exemplos e autenticação JWT.
**Tarefas:**
- [ ] Configurar SpringDoc com título, versão e descrição
- [ ] Adicionar anotações nos controllers principais
- [ ] Configurar botão "Authorize" com JWT no Swagger
- [ ] Exportar `openapi.json` para `/docs`

---

### B15 ✅
**Título:** `[Backend] Seed de dados de demonstração`
**Grupo:** Qualidade
**Prioridade:** 🟢 Baixa
**Descrição:** Dados iniciais para facilitar testes e demos do sistema.
**Tarefas:**
- [ ] Criar `DataSeeder` com `CommandLineRunner` (perfil `dev`)
- [ ] Criar 1 ADMIN, 1 SUPERVISOR, 1 TECNICO
- [ ] Criar cliente, locais, equipamentos com QR Codes
- [ ] Criar template publicado com checklist e 1 ordem atribuída

---

## 📱 MOBILE — Expo + React Native + TypeScript

### M01 ✅
**Título:** `[Mobile] Setup inicial do projeto Expo + navegação`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Configurar o projeto base com Expo, TypeScript, sistema de navegação e estrutura de pastas.
**Tarefas:**
- [ ] Criar projeto com `npx create-expo-app` + TypeScript
- [ ] Instalar e configurar React Navigation (Stack + Tab)
- [ ] Criar estrutura de pastas: `screens/`, `components/`, `services/`, `hooks/`, `store/`, `utils/`
- [ ] Configurar ESLint + Prettier
- [ ] Criar telas placeholder para cada rota principal

---

### M02 ✅
**Título:** `[Mobile] Autenticação e gestão de sessão`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Tela de login, persistência do token JWT e proteção de rotas.
**Tarefas:**
- [ ] Criar tela de Login com formulário (e-mail e senha)
- [ ] Integrar com `POST /api/public/auth/login`
- [ ] Armazenar token JWT com `expo-secure-store`
- [ ] Criar interceptor HTTP para injetar token em todas as requisições
- [ ] Rota protegida: redirecionar para Login se não autenticado
- [ ] Tela de Logout com limpeza de sessão

---

### M03 ✅
**Título:** `[Mobile] Listagem e detalhes das ordens de inspeção`
**Grupo:** Fundação e navegação
**Prioridade:** 🔴 Alta
**Descrição:** Tela principal do técnico com suas ordens atribuídas e detalhes de cada uma.
**Tarefas:**
- [ ] Tela Home com lista de ordens do técnico (`GET /my-orders`)
- [ ] Filtros por status (Pendente, Em andamento, Concluída)
- [ ] Tela de Detalhe da Ordem com dados completos
- [ ] Indicador visual de status (cores por status)
- [ ] Pull-to-refresh para atualizar a lista

---

### M04 ✅
**Título:** `[Mobile] Leitura de QR Code para identificar equipamento`
**Grupo:** Recursos nativos
**Prioridade:** 🔴 Alta
**Descrição:** Usar a câmera para ler o QR Code do equipamento e carregar os dados da inspeção.
**Tarefas:**
- [ ] Instalar e configurar `expo-camera` ou `expo-barcode-scanner`
- [ ] Criar tela de escaneamento com preview da câmera
- [ ] Integrar com `GET /api/v1/equipments/qrcode/{qrCode}`
- [ ] Exibir dados do equipamento após leitura
- [ ] Tratar QR Code inválido ou equipamento não encontrado

---

### M05 ✅
**Título:** `[Mobile] Execução do checklist de inspeção`
**Grupo:** Checklist e formulários
**Prioridade:** 🔴 Alta
**Descrição:** Formulário dinâmico para responder cada item do checklist durante a inspeção.
**Tarefas:**
- [ ] Renderizar itens do checklist dinamicamente por tipo (TEXT, BOOLEAN, NUMBER, PHOTO)
- [ ] Campo de texto livre para tipo TEXT
- [ ] Toggle/Switch para tipo BOOLEAN
- [ ] Input numérico para tipo NUMBER
- [ ] Botão de câmera para tipo PHOTO
- [ ] Indicar itens obrigatórios não respondidos
- [ ] Salvar respostas progressivamente (auto-save local)

---

### M06 ✅
**Título:** `[Mobile] Captura de fotos como evidências`
**Grupo:** Recursos nativos
**Prioridade:** 🟡 Média
**Descrição:** Permitir ao técnico tirar fotos durante a execução e anexá-las como evidências.
**Tarefas:**
- [ ] Instalar `expo-image-picker` e `expo-camera`
- [ ] Tirar foto ou selecionar da galeria
- [ ] Preview da foto antes de confirmar
- [ ] Upload para `POST /executions/{id}/evidences`
- [ ] Armazenar foto localmente em modo offline
- [ ] Exibir galeria de evidências já adicionadas

---

### M07 ✅
**Título:** `[Mobile] Captura de localização GPS`
**Grupo:** Recursos nativos
**Prioridade:** 🟡 Média
**Descrição:** Registrar a localização GPS do técnico no início e fim de cada inspeção.
**Tarefas:**
- [ ] Instalar `expo-location`
- [ ] Solicitar permissão de localização em tempo de execução
- [ ] Capturar `latitude` e `longitude` ao iniciar a execução
- [ ] Capturar localização ao finalizar
- [ ] Tratar negação de permissão graciosamente

---

### M08 ✅
**Título:** `[Mobile] Modo offline e armazenamento local`
**Grupo:** Offline e sincronização
**Prioridade:** 🔴 Alta
**Descrição:** Permitir que o técnico trabalhe sem internet, armazenando os dados localmente.
**Tarefas:**
- [ ] Instalar e configurar `@op-engineering/op-sqlite` ou `expo-sqlite`
- [ ] Criar schema local para: ordens, execuções, respostas, evidências pendentes
- [ ] Detectar estado de conectividade com `@react-native-community/netinfo`
- [ ] Salvar todas as ações do técnico localmente quando offline
- [ ] Exibir indicador visual de modo offline

---

### M09 ✅
**Título:** `[Mobile] Sincronização de dados offline com a API`
**Grupo:** Offline e sincronização
**Prioridade:** 🔴 Alta
**Descrição:** Enviar automaticamente os dados pendentes quando a conexão for restaurada.
**Tarefas:**
- [ ] Detectar retorno de conectividade e disparar sincronização
- [ ] Enviar payload para `POST /api/v1/sync`
- [ ] Marcar itens locais como sincronizados após confirmação
- [ ] Exibir progresso e resultado da sincronização
- [ ] Tratar erros parciais e manter dados não sincronizados para retry

---

### M10 ✅
**Título:** `[Mobile] Tela de finalização e confirmação da inspeção`
**Grupo:** Checklist e formulários
**Prioridade:** 🟡 Média
**Descrição:** Tela de revisão antes de finalizar a inspeção, com resumo das respostas e evidências.
**Tarefas:**
- [ ] Exibir resumo: itens respondidos vs. total, obrigatórios pendentes
- [ ] Listar evidências adicionadas
- [ ] Botão "Finalizar Inspeção" com confirmação (alert/modal)
- [ ] Chamar `PATCH /executions/{id}/finish`
- [ ] Navegar para tela de sucesso e atualizar lista de ordens

---

## 🌐 ADMIN WEB — Angular + TypeScript

### A01 ✅
**Título:** `[Admin] Setup inicial do projeto Angular`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Configurar o projeto Angular com estrutura de módulos, roteamento, estilo e ferramentas.
**Tarefas:**
- [ ] Criar projeto com Angular CLI + TypeScript strict
- [ ] Configurar módulo de roteamento principal
- [ ] Instalar e configurar biblioteca de UI (ex: Angular Material ou PrimeNG)
- [ ] Criar estrutura de pastas: `core/`, `shared/`, `features/`, `pages/`
- [ ] Configurar `HttpClient` com interceptor para JWT
- [ ] Configurar `environments/` para dev e prod
- [ ] Configurar ESLint + Prettier

---

### A02 ✅
**Título:** `[Admin] Autenticação e proteção de rotas`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Tela de login, gestão de sessão JWT e guard de rotas por role.
**Tarefas:**
- [ ] Criar tela de Login com formulário reativo
- [ ] Criar `AuthService` integrado com `POST /api/public/auth/login`
- [ ] Armazenar token no `localStorage` / `sessionStorage`
- [ ] Criar `AuthInterceptor` para injetar Bearer token
- [ ] Criar `AuthGuard` para rotas privadas
- [ ] Criar `RoleGuard` para controle por perfil (ADMIN, SUPERVISOR)
- [ ] Tela de acesso negado (403)

---

### A03 ✅
**Título:** `[Admin] Layout principal e navegação lateral`
**Grupo:** Fundação
**Prioridade:** 🔴 Alta
**Descrição:** Shell da aplicação com sidebar, topbar, área de conteúdo e navegação responsiva.
**Tarefas:**
- [ ] Criar componente `AppShell` com sidebar retrátil
- [ ] Sidebar com itens de menu baseados no role do usuário
- [ ] Topbar com nome do usuário e botão de logout
- [ ] Rotas lazy-loaded para cada módulo
- [ ] Layout responsivo (desktop-first)

---

### A04 ✅
**Título:** `[Admin] Módulo de Usuários`
**Grupo:** Cadastros
**Prioridade:** 🔴 Alta
**Descrição:** Listagem, criação, edição e exclusão de usuários do sistema (somente ADMIN).
**Tarefas:**
- [ ] Listagem com tabela paginada e busca por nome/e-mail
- [ ] Formulário de criação com seleção de role
- [ ] Formulário de edição (não edita senha diretamente)
- [ ] Confirmação de exclusão (soft delete)
- [ ] Integrar com endpoints `GET/POST/PUT/DELETE /api/v1/users`

---

### A05 ✅
**Título:** `[Admin] Módulo de Clientes`
**Grupo:** Cadastros
**Prioridade:** 🟡 Média
**Descrição:** Gerenciamento de clientes com lista, formulário e visualização de locais vinculados.
**Tarefas:**
- [ ] Listagem com tabela paginada e filtros
- [ ] Formulário de criação e edição
- [ ] Visualizar locais vinculados ao cliente
- [ ] Integrar com endpoints de `/api/v1/clients`

---

### A06 ✅
**Título:** `[Admin] Módulo de Locais e Equipamentos`
**Grupo:** Cadastros
**Prioridade:** 🟡 Média
**Descrição:** Gerenciamento de locais físicos e equipamentos com QR Codes.
**Tarefas:**
- [ ] Listagem de locais com filtro por cliente
- [ ] Formulário de local com campos de endereço e coordenadas
- [ ] Listagem de equipamentos por local
- [ ] Formulário de equipamento com exibição do QR Code gerado
- [ ] Botão de download/impressão do QR Code
- [ ] Integrar com endpoints de `/api/v1/locations` e `/api/v1/equipments`

---

### A07 ✅
**Título:** `[Admin] Módulo de Templates de Inspeção`
**Grupo:** Modelos
**Prioridade:** 🔴 Alta
**Descrição:** Interface para criar e publicar modelos de inspeção com itens de checklist.
**Tarefas:**
- [ ] Listagem de templates com status (Rascunho / Publicado)
- [ ] Formulário de criação do template
- [ ] Editor de itens do checklist com drag-and-drop para reordenar
- [ ] Tipos de item: TEXT, BOOLEAN, NUMBER, PHOTO
- [ ] Marcar itens como obrigatórios
- [ ] Botão "Publicar" com confirmação
- [ ] Integrar com endpoints de `/api/v1/inspection-templates`

---

### A08 ✅
**Título:** `[Admin] Módulo de Ordens de Inspeção`
**Grupo:** Planejamento
**Prioridade:** 🔴 Alta
**Descrição:** Criação, agendamento e atribuição de ordens de inspeção a técnicos.
**Tarefas:**
- [ ] Listagem de ordens com filtros por status, técnico e data
- [ ] Formulário de criação: selecionar template, local, equipamento, técnico e data
- [ ] Ação de atribuir/reatribuir técnico
- [ ] Ação de cancelar ordem com confirmação
- [ ] Visualizar detalhes da ordem e status atual
- [ ] Integrar com endpoints de `/api/v1/inspection-orders`

---

### A09 ✅
**Título:** `[Admin] Módulo de Revisão de Inspeções`
**Grupo:** Revisão
**Prioridade:** 🔴 Alta
**Descrição:** Interface para o Supervisor revisar execuções concluídas, visualizar respostas e evidências.
**Tarefas:**
- [ ] Listagem de execuções pendentes de revisão
- [ ] Tela de detalhes com respostas do checklist e evidências (fotos)
- [ ] Ações: Aprovar, Reprovar, Solicitar Correção
- [ ] Campo de comentário obrigatório ao reprovar
- [ ] Indicar visualmente o resultado da revisão na lista de ordens
- [ ] Integrar com endpoints de `/api/v1/executions`

---

### A10 ✅
**Título:** `[Admin] Dashboard com indicadores gerais`
**Grupo:** Qualidade
**Prioridade:** 🟢 Baixa
**Descrição:** Tela inicial com visão geral do sistema para ADMIN e SUPERVISOR.
**Tarefas:**
- [ ] Cards com totais: ordens pendentes, em andamento, concluídas, reprovadas
- [ ] Gráfico de inspeções por status (últimos 30 dias)
- [ ] Lista das ordens mais recentes
- [ ] Filtro por técnico ou período

---

## 📄 DOCUMENTAÇÃO — Projeto Transversal

> Cards de documentação não pertencem a uma frente específica — cobrem o projeto inteiro.
> Responsabilidade compartilhada entre PO, Tech Leads e equipe.

---

### D01 ✅
**Título:** `[Docs] Diagrama de Classes do Domínio`
**Grupo:** Modelagem
**Prioridade:** 🔴 Alta
**Descrição:** Mapear todas as entidades do sistema, seus atributos, tipos e relacionamentos em um diagrama UML formal.
**Tarefas:**
- [ ] Listar todas as entidades do MVP: User, Client, Location, Equipment, InspectionTemplate, ChecklistItem, InspectionOrder, InspectionExecution, ChecklistResponse, Evidence, Review, SyncLog
- [ ] Definir atributos e tipos de dados de cada entidade
- [ ] Definir relacionamentos com multiplicidade (1:1, 1:N, N:M)
- [ ] Gerar diagrama em Mermaid, PlantUML ou draw.io
- [ ] Publicar em `/docs/diagrama-classes.md` ou linkar na issue
- [ ] Revisar com o time de backend antes de iniciar implementação

---

### D02 ✅
**Título:** `[Docs] Arquitetura geral do sistema`
**Grupo:** Arquitetura
**Prioridade:** 🔴 Alta
**Descrição:** Documentar a arquitetura completa da plataforma: componentes, camadas, integrações e decisões técnicas.
**Tarefas:**
- [ ] Diagrama de componentes: Mobile ↔ API REST ↔ PostgreSQL / Admin ↔ API REST
- [ ] Documentar camadas do backend (Controller → Service → Repository → DB)
- [ ] Documentar estratégia de autenticação JWT
- [ ] Documentar fluxo de sincronização offline
- [ ] Listar decisões técnicas com justificativa (ADRs simplificados)
- [ ] Publicar em `/docs/architecture.md`

---

### D03 ✅
**Título:** `[Docs] Contrato da API REST (OpenAPI/Swagger)`
**Grupo:** API
**Prioridade:** 🔴 Alta
**Descrição:** Gerar e manter atualizado o contrato formal da API para consumo pelo time de Mobile e Admin.
**Tarefas:**
- [ ] Exportar `openapi.json` gerado pelo SpringDoc após implementação dos endpoints
- [ ] Publicar em `/docs/openapi.json`
- [ ] Documentar autenticação (Bearer JWT) no contrato
- [ ] Incluir exemplos de request e response em cada operação
- [ ] Versionar o contrato a cada alteração de endpoints
- [ ] Linkar o contrato no README principal

---

### D04 ✅
**Título:** `[Docs] Modelo de dados (Diagrama ER do banco)`
**Grupo:** Banco de dados
**Prioridade:** 🟡 Média
**Descrição:** Diagrama Entidade-Relacionamento do banco PostgreSQL com todas as tabelas, colunas e chaves.
**Tarefas:**
- [ ] Mapear todas as tabelas geradas pelas migrações Flyway
- [ ] Incluir PKs, FKs, índices e constraints relevantes
- [ ] Gerar diagrama com dbdiagram.io, DBeaver ou similar
- [ ] Publicar em `/docs/diagrama-er.md` ou `/docs/er.png`
- [ ] Manter sincronizado com as migrações Flyway

---

### D05 ✅
**Título:** `[Docs] Guia de configuração e execução local`
**Grupo:** Onboarding
**Prioridade:** 🔴 Alta
**Descrição:** Documentar passo a passo para qualquer integrante configurar e rodar o projeto do zero na sua máquina.
**Tarefas:**
- [ ] Pré-requisitos: versões de Java, Node, npm, Expo CLI, Angular CLI, PostgreSQL
- [ ] Passos para clonar e configurar variáveis de ambiente
- [ ] Como criar o banco PostgreSQL e executar as migrações
- [ ] Como rodar backend, admin e mobile individualmente
- [ ] Credenciais de demonstração (admin, supervisor, técnico)
- [ ] Seção de erros comuns e como resolver
- [ ] Publicar nos READMEs de cada subprojeto (`/api/README.md`, `/mobile/README.md`, `/admin/README.md`)

---

### D06 ✅
**Título:** `[Docs] Fluxo de contribuição e padrões de Git`
**Grupo:** Processo
**Prioridade:** 🟡 Média
**Descrição:** Padronizar o fluxo de trabalho com Git para toda a equipe: branches, commits, PRs e revisões.
**Tarefas:**
- [ ] Documentar estratégia de branches: `main`, `develop`, `feature/*`, `fix/*`
- [ ] Definir padrão de mensagens de commit (ex: Conventional Commits)
- [ ] Documentar processo de PR: título, descrição, critérios de revisão
- [ ] Definir quem aprova e faz merge de cada frente
- [ ] Criar template de Pull Request (`.github/pull_request_template.md`)
- [ ] Publicar em `CONTRIBUTING.md` na raiz do repositório

---

### D07 ✅
**Título:** `[Docs] Mapeamento de rotas e permissões da API`
**Grupo:** API
**Prioridade:** 🟡 Média
**Descrição:** Tabela completa com todos os endpoints, métodos HTTP, roles permitidas e descrição de cada operação.
**Tarefas:**
- [ ] Listar todos os endpoints por módulo (Auth, Users, Clients, Locations, Equipments, Templates, Orders, Executions, Reviews, Sync)
- [ ] Indicar método HTTP, URL, roles autorizadas e descrição
- [ ] Indicar quais endpoints são públicos (`/api/public/**`)
- [ ] Publicar em `/docs/api-routes.md`
- [ ] Atualizar a cada novo endpoint implementado

---

### D08 ✅
**Título:** `[Docs] Glossário e definições do domínio`
**Grupo:** Produto
**Prioridade:** 🟢 Baixa
**Descrição:** Dicionário com os termos do domínio do FieldOps para alinhar vocabulário entre todas as frentes.
**Tarefas:**
- [ ] Definir termos: Inspeção, Template, Ordem, Execução, Checklist, Evidência, Revisão, Técnico, Supervisor, Admin
- [ ] Mapear termos em português (negócio) para inglês (código): ex. "Ordem de Inspeção" → `InspectionOrder`
- [ ] Documentar ciclo de vida de uma inspeção com os status possíveis
- [ ] Publicar em `/docs/glossario.md`

---

### D09 ✅
**Título:** `[Docs] Documento de escopo do MVP e critérios de aceite`
**Grupo:** Produto
**Prioridade:** 🟡 Média
**Descrição:** Formalizar o escopo do MVP com histórias de usuário e critérios de aceite para cada funcionalidade.
**Tarefas:**
- [ ] Escrever histórias de usuário no formato: "Como [perfil], quero [ação], para [benefício]"
- [ ] Cobrir: Administrador, Supervisor e Técnico
- [ ] Definir critérios de aceite mensuráveis para cada história
- [ ] Listar explicitamente o que está fora do escopo do MVP
- [ ] Publicar em `/docs/escopo-mvp.md`
- [ ] Revisar com todo o time antes de iniciar a Sprint 1

---

### D10 ✅
**Título:** `[Docs] README dos subprojetos (api, mobile, admin)`
**Grupo:** Onboarding
**Prioridade:** 🟡 Média
**Descrição:** Criar READMEs específicos para cada componente do repositório com instruções detalhadas.
**Tarefas:**
- [ ] `/api/README.md`: stack, como rodar, variáveis de ambiente, migrações, endpoints principais
- [ ] `/mobile/README.md`: pré-requisitos, como rodar no emulador/dispositivo, estrutura do projeto
- [ ] `/admin/README.md`: pré-requisitos, como rodar, variáveis de ambiente, estrutura de módulos
- [ ] Cada README deve ter seções: Sobre, Tecnologias, Pré-requisitos, Como executar, Variáveis de ambiente, Estrutura de pastas

---

## 🧪 TESTES — Qualidade e Confiabilidade

> Cards de testes cobrem as três frentes e garantem a estabilidade do MVP em todas as camadas.

---

### T01 ✅
**Título:** `[Testes] Testes unitários — Backend (Services e Utils)`
**Grupo:** Teste Unitário
**Prioridade:** 🔴 Alta
**Descrição:** Validar a lógica de negócio isolada em cada Service do backend, sem dependência de banco ou rede.
**Tarefas:**
- [ ] Configurar JUnit 5 + Mockito no projeto Spring Boot
- [ ] Escrever testes unitários para `UserService`: criação, busca, soft delete, validação de role
- [ ] Escrever testes para `InspectionTemplateService`: criar, publicar, rejeitar publicação de rascunho
- [ ] Escrever testes para `InspectionOrderService`: atribuir técnico, cancelar, transições de status
- [ ] Escrever testes para `InspectionExecutionService`: iniciar, finalizar, validar obrigatórios
- [ ] Escrever testes para `ReviewService`: aprovar, reprovar, solicitar correção
- [ ] Escrever testes para `JwtUtil`: geração, validação e expiração de token
- [ ] Cobertura mínima: 70% das classes de Service
- [ ] Executar com `mvn test`

---

### T02 ✅
**Título:** `[Testes] Testes de integração — Backend (Controllers e Repositórios)`
**Grupo:** Teste Funcional
**Prioridade:** 🔴 Alta
**Descrição:** Testar os endpoints REST completos com banco H2 em memória, validando request, response, status HTTP e persistência.
**Tarefas:**
- [ ] Configurar `@SpringBootTest` + `MockMvc` + banco H2 com perfil `test`
- [ ] Testes para `POST /api/public/auth/login`: credenciais válidas, inválidas, usuário inexistente
- [ ] Testes para endpoints de `Users`: CRUD completo, acesso negado por role
- [ ] Testes para endpoints de `InspectionTemplates`: criar, editar, publicar, listar
- [ ] Testes para endpoints de `InspectionOrders`: criar, atribuir, cancelar
- [ ] Testes para endpoints de `Executions`: iniciar, responder checklist, finalizar
- [ ] Testes para `POST /api/v1/sync`: payload válido, parcialmente inválido, offline batch
- [ ] Validar retorno `401` para requisições sem token e `403` para role incorreta
- [ ] Validar formato padrão de resposta `ApiResponse<T>`

---

### T03 ✅
**Título:** `[Testes] Testes unitários — Mobile (componentes e hooks)`
**Grupo:** Teste Unitário
**Prioridade:** 🟡 Média
**Descrição:** Testar componentes React Native e hooks customizados de forma isolada.
**Tarefas:**
- [ ] Configurar Jest + React Native Testing Library
- [ ] Testes para tela de Login: renderização, preenchimento de formulário, submit, erro de credenciais
- [ ] Testes para componente de checklist: renderização por tipo (TEXT, BOOLEAN, NUMBER, PHOTO)
- [ ] Testes para hook `useAuth`: login, logout, persistência de token
- [ ] Testes para hook `useConnectivity`: detectar online/offline
- [ ] Testes para utilitários de sincronização: montagem do payload, marcação de itens sincronizados
- [ ] Testes para hook `useInspectionOrders`: listar, filtrar por status
- [ ] Executar com `npm test -- --watchAll=false`

---

### T04 ✅
**Título:** `[Testes] Testes unitários — Admin Web (componentes e serviços Angular)`
**Grupo:** Teste Unitário
**Prioridade:** 🟡 Média
**Descrição:** Testar componentes Angular e serviços de forma isolada com mocks da API.
**Tarefas:**
- [ ] Configurar Karma + Jasmine (padrão Angular CLI)
- [ ] Testes para `AuthService`: login, logout, armazenamento de token, expiração
- [ ] Testes para `AuthGuard` e `RoleGuard`: redirecionar sem token, bloquear por role
- [ ] Testes para componente de listagem de usuários: renderização, paginação, busca
- [ ] Testes para formulário de template: adicionar item, reordenar, marcar obrigatório
- [ ] Testes para componente de revisão: aprovar, reprovar, validação de comentário obrigatório
- [ ] Usar `HttpClientTestingModule` para mockar chamadas à API
- [ ] Executar com `ng test --watch=false`

---

### T05 ✅
**Título:** `[Testes] Testes funcionais — Fluxo principal da inspeção`
**Grupo:** Teste Funcional
**Prioridade:** 🔴 Alta
**Descrição:** Testar o fluxo completo de uma inspeção de ponta a ponta, validando a integração entre Admin Web, API e Mobile.
**Tarefas:**
- [ ] **Cenário 1 — Cadastro e configuração:**
  - Admin cria usuário Supervisor e Técnico via API
  - Admin cria cliente, local e equipamento com QR Code
- [ ] **Cenário 2 — Criação e publicação de template:**
  - Supervisor cria template com 5 itens (TEXT, BOOLEAN, NUMBER, PHOTO, obrigatório)
  - Supervisor publica o template
- [ ] **Cenário 3 — Agendamento:**
  - Supervisor cria ordem de inspeção com template, local, equipamento e técnico
  - Verificar que ordem aparece na lista do técnico no mobile
- [ ] **Cenário 4 — Execução:**
  - Técnico faz login no mobile
  - Técnico escaneia QR Code do equipamento
  - Técnico responde todos os itens do checklist
  - Técnico anexa foto como evidência
  - Técnico finaliza a inspeção
- [ ] **Cenário 5 — Revisão:**
  - Supervisor visualiza execução concluída no Admin Web
  - Supervisor aprova a inspeção
  - Verificar status final da ordem como COMPLETED
- [ ] Documentar resultado de cada cenário com print ou log

---

### T06 ✅
**Título:** `[Testes] Testes funcionais — Fluxo offline e sincronização`
**Grupo:** Teste Funcional
**Prioridade:** 🔴 Alta
**Descrição:** Validar que o técnico consegue executar uma inspeção completa sem internet e sincronizar ao reconectar.
**Tarefas:**
- [ ] Técnico faz login com internet e baixa ordens pendentes
- [ ] Simular perda de conexão (modo avião)
- [ ] Técnico responde checklist completo offline
- [ ] Técnico tira foto offline (evidência armazenada localmente)
- [ ] Técnico finaliza inspeção offline
- [ ] Restaurar conexão — sincronização automática dispara
- [ ] Verificar via API que execução, respostas e evidências chegaram corretamente
- [ ] Verificar `SyncLog` com status SUCCESS
- [ ] Testar cenário de sincronização parcial com falha em um item

---

### T07 ✅
**Título:** `[Testes] Testes E2E — Admin Web com Cypress`
**Grupo:** Teste E2E
**Prioridade:** 🟡 Média
**Descrição:** Testes de ponta a ponta automatizados na interface web do administrador.
**Tarefas:**
- [ ] Instalar e configurar Cypress no projeto Angular
- [ ] Criar fixture de dados de teste (usuários, clientes, templates)
- [ ] **Spec: Login** — login com credenciais válidas, inválidas, redirect para dashboard
- [ ] **Spec: Usuários** — criar usuário, editar, excluir, validar campos obrigatórios
- [ ] **Spec: Template** — criar template, adicionar itens, publicar
- [ ] **Spec: Ordem** — criar ordem, atribuir técnico, cancelar
- [ ] **Spec: Revisão** — listar pendentes, aprovar, reprovar com comentário
- [ ] Configurar execução no pipeline CI (`cypress run --headless`)

---

### T08 ✅
**Título:** `[Testes] Testes E2E — Mobile com Detox`
**Grupo:** Teste E2E
**Prioridade:** 🟢 Baixa
**Descrição:** Testes automatizados de ponta a ponta no app mobile simulando interações reais do técnico.
**Tarefas:**
- [ ] Instalar e configurar Detox no projeto Expo
- [ ] **Spec: Login** — login com credenciais válidas e inválidas
- [ ] **Spec: Lista de ordens** — visualizar ordens, filtrar por status
- [ ] **Spec: Execução** — iniciar execução, responder checklist, finalizar
- [ ] **Spec: QR Code** — mock de leitura de QR Code
- [ ] **Spec: Offline** — simular modo avião e verificar armazenamento local
- [ ] Executar em emulador Android e/ou simulador iOS

---

### T09 ✅
**Título:** `[Testes] Testes de regressão — API REST`
**Grupo:** Teste Regressivo
**Prioridade:** 🟡 Média
**Descrição:** Suíte de testes de regressão para garantir que novas implementações não quebram comportamentos existentes.
**Tarefas:**
- [ ] Criar coleção no Postman/Insomnia com todos os endpoints implementados
- [ ] Organizar por módulo: Auth, Users, Clients, Locations, Equipments, Templates, Orders, Executions, Reviews, Sync
- [ ] Definir variáveis de ambiente: `baseUrl`, `tokenAdmin`, `tokenSupervisor`, `tokenTecnico`
- [ ] Adicionar assertions em cada requisição: status HTTP, campos obrigatórios no response, tipos de dado
- [ ] Exportar coleção para `/docs/postman-collection.json`
- [ ] Executar coleção completa a cada merge na branch `develop` com Newman:
  ```bash
  newman run postman-collection.json -e environment.json
  ```
- [ ] Documentar quais endpoints fazem parte da suíte de regressão

---

### T10 ✅
**Título:** `[Testes] Testes de regressão — Fluxos críticos do produto`
**Grupo:** Teste Regressivo
**Prioridade:** 🟡 Média
**Descrição:** Lista de fluxos críticos que devem ser re-testados manualmente a cada entrega, garantindo que nenhuma regressão quebre o MVP.
**Tarefas:**
- [ ] Definir e documentar os fluxos críticos em `/docs/regressao.md`:
  - Login com cada perfil (Admin, Supervisor, Técnico)
  - Criação e publicação de template de inspeção
  - Criação e atribuição de ordem de inspeção
  - Execução completa do checklist no mobile
  - Upload de foto como evidência
  - Execução offline + sincronização
  - Revisão e aprovação pelo Supervisor
  - Revisão com reprovação e correção solicitada
- [ ] Criar planilha ou checklist de regressão manual
- [ ] Executar suíte completa antes de cada entrega/sprint review
- [ ] Registrar bugs encontrados como issues com label `regression`

---

## 🔒 CIBERSEGURANÇA — Proteção da Plataforma

> Cards de segurança são transversais e devem ser aplicados em todas as frentes antes da entrega do MVP.

---

### S01 ✅
**Título:** `[Segurança] Autenticação segura e controle de sessão JWT`
**Grupo:** Autenticação
**Prioridade:** 🔴 Alta
**Descrição:** Garantir que o mecanismo de autenticação JWT seja seguro contra ataques comuns.
**Tarefas:**
- [ ] Usar algoritmo de assinatura `HS256` ou `RS256` com chave de no mínimo 256 bits
- [ ] Configurar expiração do token (ex: 1h para access token)
- [ ] Nunca armazenar o token JWT no `localStorage` do Admin — usar `httpOnly cookie` ou `sessionStorage` com cuidado
- [ ] Implementar invalidação de token no logout (blocklist em memória ou Redis)
- [ ] Validar `iss`, `exp` e `sub` em todo token recebido
- [ ] Nunca logar o valor do token em arquivos de log
- [ ] Configurar header `Authorization` obrigatório em todos os endpoints privados

---

### S02 ✅
**Título:** `[Segurança] Controle de acesso por Role (RBAC)`
**Grupo:** Autorização
**Prioridade:** 🔴 Alta
**Descrição:** Garantir que cada endpoint só seja acessível pelos perfis autorizados.
**Tarefas:**
- [ ] Mapear e revisar todas as rotas com suas roles permitidas
- [ ] Aplicar `@PreAuthorize` em todos os endpoints sensíveis
- [ ] Garantir que TECNICO não acessa endpoints de ADMIN/SUPERVISOR e vice-versa
- [ ] Testar tentativa de acesso cross-role (ex: técnico tentando criar template)
- [ ] Retornar `403 Forbidden` (nunca `404`) para acesso negado por role
- [ ] Garantir que o mobile só acessa endpoints pertinentes ao técnico autenticado
- [ ] Documentar matriz de permissões em `/docs/api-routes.md`

---

### S03 ✅
**Título:** `[Segurança] Proteção contra injeção e validação de entrada`
**Grupo:** Input Validation
**Prioridade:** 🔴 Alta
**Descrição:** Prevenir SQL Injection, XSS e outros ataques via entrada de dados maliciosa.
**Tarefas:**
- [ ] Usar apenas queries parametrizadas via JPA/JPQL — nunca concatenar SQL manualmente
- [ ] Aplicar `@Valid` + Bean Validation em todos os DTOs de entrada
- [ ] Sanitizar campos de texto livre antes de persistir (remover scripts, HTML)
- [ ] Validar e limitar tamanho de campos string (ex: nome máx. 255 chars)
- [ ] Validar tipo e tamanho de arquivos no upload de evidências (somente imagens, máx. 10MB)
- [ ] Rejeitar payloads JSON com campos inesperados (`@JsonIgnoreProperties(ignoreUnknown = false)` onde necessário)
- [ ] Adicionar validação de formato de e-mail, CPF/CNPJ e coordenadas GPS

---

### S04 ✅
**Título:** `[Segurança] Configuração segura de CORS`
**Grupo:** Configuração
**Prioridade:** 🔴 Alta
**Descrição:** Restringir origens que podem consumir a API para evitar requisições cross-origin não autorizadas.
**Tarefas:**
- [ ] Configurar `CorsConfiguration` com lista explícita de origens permitidas (não usar `*` em produção)
- [ ] Permitir apenas métodos necessários: GET, POST, PUT, PATCH, DELETE, OPTIONS
- [ ] Configurar `allowedHeaders` explicitamente (Authorization, Content-Type)
- [ ] Definir `maxAge` para cache do preflight
- [ ] Validar configuração em ambiente de desenvolvimento com origem diferente
- [ ] Documentar origens permitidas por ambiente (dev, staging, prod)

---

### S05 ✅
**Título:** `[Segurança] Proteção de senhas e dados sensíveis`
**Grupo:** Dados sensíveis
**Prioridade:** 🔴 Alta
**Descrição:** Garantir que senhas e dados sensíveis sejam armazenados e transmitidos de forma segura.
**Tarefas:**
- [ ] Armazenar senhas com `BCryptPasswordEncoder` (custo mínimo 10)
- [ ] Nunca retornar campo `password` em nenhuma resposta da API
- [ ] Nunca logar senhas, tokens ou dados pessoais
- [ ] Usar variáveis de ambiente para secrets (JWT secret, credenciais do banco) — nunca hardcoded
- [ ] Criar arquivo `.env.example` sem valores reais e adicionar `.env` ao `.gitignore`
- [ ] Verificar que nenhum secret está comitado no histórico do Git (`git log` + `git grep`)
- [ ] No mobile, armazenar token com `expo-secure-store` (Keychain/Keystore nativo)

---

### S06 ✅
**Título:** `[Segurança] Headers de segurança HTTP`
**Grupo:** Configuração
**Prioridade:** 🟡 Média
**Descrição:** Adicionar headers HTTP de segurança para proteger contra ataques como clickjacking, MIME sniffing e XSS.
**Tarefas:**
- [ ] Configurar via Spring Security:
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-XSS-Protection: 1; mode=block`
  - `Strict-Transport-Security` (HSTS) — para produção com HTTPS
  - `Content-Security-Policy` básico
- [ ] Remover header `X-Powered-By` e `Server` das respostas
- [ ] Verificar headers com ferramenta como [securityheaders.com](https://securityheaders.com) em staging
- [ ] No Admin Angular, configurar `meta` CSP no `index.html`

---

### S07 ✅
**Título:** `[Segurança] Rate limiting e proteção contra força bruta`
**Grupo:** Disponibilidade
**Prioridade:** 🟡 Média
**Descrição:** Limitar requisições repetidas para proteger endpoints críticos contra ataques de força bruta e abuso.
**Tarefas:**
- [ ] Aplicar rate limiting no endpoint `POST /api/public/auth/login` (ex: máx. 5 tentativas por minuto por IP)
- [ ] Retornar `429 Too Many Requests` ao exceder o limite
- [ ] Implementar com Bucket4j ou Spring Boot Rate Limiter
- [ ] Aplicar rate limiting geral na API (ex: 100 req/min por token autenticado)
- [ ] Logar tentativas bloqueadas para auditoria
- [ ] Documentar configuração de limites por endpoint

---

### S08 ✅
**Título:** `[Segurança] Auditoria e logs de segurança`
**Grupo:** Auditoria
**Prioridade:** 🟡 Média
**Descrição:** Registrar eventos de segurança relevantes para rastreabilidade e detecção de anomalias.
**Tarefas:**
- [ ] Logar toda tentativa de login (sucesso e falha) com IP, e-mail e timestamp
- [ ] Logar acessos negados (`403`) com usuário, endpoint e timestamp
- [ ] Logar criação, edição e exclusão de entidades críticas (User, InspectionOrder, Review)
- [ ] Registrar sincronizações offline com usuário e resultado
- [ ] Garantir que logs não contêm dados sensíveis (senha, token completo)
- [ ] Configurar nível de log adequado por ambiente: `INFO` em prod, `DEBUG` em dev
- [ ] Estruturar logs em JSON para facilitar análise futura (Logback + logstash-logback-encoder)

---

### S09 ✅
**Título:** `[Segurança] Proteção de arquivos e upload seguro`
**Grupo:** Upload
**Prioridade:** 🟡 Média
**Descrição:** Garantir que o upload de evidências (fotos) não seja um vetor de ataque.
**Tarefas:**
- [ ] Validar extensão do arquivo: permitir apenas `jpg`, `jpeg`, `png`, `webp`
- [ ] Validar MIME type real do arquivo (não confiar apenas na extensão)
- [ ] Limitar tamanho máximo por arquivo (ex: 10MB) e por requisição
- [ ] Renomear arquivo no servidor (nunca usar nome original do cliente)
- [ ] Armazenar arquivos fora do diretório público web
- [ ] Gerar URL assinada ou token temporário para acesso a evidências
- [ ] Configurar `spring.servlet.multipart.max-file-size` e `max-request-size`

---

### S10 ✅
**Título:** `[Segurança] Revisão de dependências e gestão de vulnerabilidades`
**Grupo:** Supply Chain
**Prioridade:** 🟡 Média
**Descrição:** Garantir que bibliotecas de terceiros não introduzam vulnerabilidades conhecidas no projeto.
**Tarefas:**
- [ ] Configurar OWASP Dependency-Check no `pom.xml` do backend:
  ```xml
  <plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
  </plugin>
  ```
- [ ] Executar `npm audit` no mobile e no admin e corrigir vulnerabilidades críticas
- [ ] Usar versões fixas (pinadas) de dependências — sem ranges abertos (`^`, `~`)
- [ ] Revisar e atualizar dependências a cada sprint
- [ ] Nunca adicionar dependência sem avaliar manutenção ativa e reputação do pacote
- [ ] Documentar dependências críticas e suas versões em `/docs/dependencies.md`

---

## 📋 RESUMO GERAL DO BACKLOG

| # | Frente | Título | Prioridade |
|---|---|---|---|
| B01 | 🟢 Backend | Setup inicial Spring Boot | 🔴 Alta |
| B02 | 🟢 Backend | Autenticação JWT | 🔴 Alta |
| B03 | 🟢 Backend | Tratamento global de erros | 🔴 Alta |
| B04 | 🟢 Backend | CRUD Usuários e Roles | 🔴 Alta |
| B05 | 🟢 Backend | CRUD Clientes | 🟡 Média |
| B06 | 🟢 Backend | CRUD Locais | 🟡 Média |
| B07 | 🟢 Backend | CRUD Equipamentos + QR Code | 🟡 Média |
| B08 | 🟢 Backend | Templates de Inspeção e Checklist | 🔴 Alta |
| B09 | 🟢 Backend | Ordens de Inspeção | 🔴 Alta |
| B10 | 🟢 Backend | Execução e respostas do checklist | 🔴 Alta |
| B11 | 🟢 Backend | Upload de Evidências | 🟡 Média |
| B12 | 🟢 Backend | Sincronização offline | 🔴 Alta |
| B13 | 🟢 Backend | Revisão pelo Supervisor | 🔴 Alta |
| B14 | 🟢 Backend | Documentação OpenAPI | 🟡 Média |
| B15 | 🟢 Backend | Seed de dados demo | 🟢 Baixa |
| M01 | 📱 Mobile | Setup Expo + navegação | 🔴 Alta |
| M02 | 📱 Mobile | Autenticação e sessão | 🔴 Alta |
| M03 | 📱 Mobile | Listagem de ordens | 🔴 Alta |
| M04 | 📱 Mobile | Leitura de QR Code | 🔴 Alta |
| M05 | 📱 Mobile | Execução do checklist | 🔴 Alta |
| M06 | 📱 Mobile | Captura de fotos/evidências | 🟡 Média |
| M07 | 📱 Mobile | Captura de localização GPS | 🟡 Média |
| M08 | 📱 Mobile | Modo offline e armazenamento local | 🔴 Alta |
| M09 | 📱 Mobile | Sincronização de dados offline | 🔴 Alta |
| M10 | 📱 Mobile | Tela de finalização da inspeção | 🟡 Média |
| A01 | 🌐 Admin | Setup Angular | 🔴 Alta |
| A02 | 🌐 Admin | Autenticação e guard de rotas | 🔴 Alta |
| A03 | 🌐 Admin | Layout e navegação lateral | 🔴 Alta |
| A04 | 🌐 Admin | Módulo de Usuários | 🔴 Alta |
| A05 | 🌐 Admin | Módulo de Clientes | 🟡 Média |
| A06 | 🌐 Admin | Módulo de Locais e Equipamentos | 🟡 Média |
| A07 | 🌐 Admin | Módulo de Templates de Inspeção | 🔴 Alta |
| A08 | 🌐 Admin | Módulo de Ordens de Inspeção | 🔴 Alta |
| A09 | 🌐 Admin | Módulo de Revisão de Inspeções | 🔴 Alta |
| A10 | 🌐 Admin | Dashboard com indicadores | 🟢 Baixa |
| D01 | 📄 Docs | Diagrama de Classes do Domínio | 🔴 Alta |
| D02 | 📄 Docs | Arquitetura geral do sistema | 🔴 Alta |
| D03 | 📄 Docs | Contrato da API REST (OpenAPI) | 🔴 Alta |
| D04 | 📄 Docs | Modelo de dados (Diagrama ER) | 🟡 Média |
| D05 | 📄 Docs | Guia de configuração e execução local | 🔴 Alta |
| D06 | 📄 Docs | Fluxo de contribuição e padrões de Git | 🟡 Média |
| D07 | 📄 Docs | Mapeamento de rotas e permissões da API | 🟡 Média |
| D08 | 📄 Docs | Glossário e definições do domínio | 🟢 Baixa |
| D09 | 📄 Docs | Escopo do MVP e critérios de aceite | 🟡 Média |
| D10 | 📄 Docs | READMEs dos subprojetos (api, mobile, admin) | 🟡 Média |
| T01 | 🧪 Testes | Testes unitários — Backend | 🔴 Alta |
| T02 | 🧪 Testes | Testes de integração — Backend | 🔴 Alta |
| T03 | 🧪 Testes | Testes unitários — Mobile | 🟡 Média |
| T04 | 🧪 Testes | Testes unitários — Admin Web | 🟡 Média |
| T05 | 🧪 Testes | Testes funcionais — Fluxo principal da inspeção | 🔴 Alta |
| T06 | 🧪 Testes | Testes funcionais — Fluxo offline e sincronização | 🔴 Alta |
| T07 | 🧪 Testes | Testes E2E — Admin Web com Cypress | 🟡 Média |
| T08 | 🧪 Testes | Testes E2E — Mobile com Detox | 🟢 Baixa |
| T09 | 🧪 Testes | Testes de regressão — API REST | 🟡 Média |
| T10 | 🧪 Testes | Testes de regressão — Fluxos críticos do produto | 🟡 Média |
| S01 | 🔒 Segurança | Autenticação segura e controle de sessão JWT | 🔴 Alta |
| S02 | 🔒 Segurança | Controle de acesso por Role (RBAC) | 🔴 Alta |
| S03 | 🔒 Segurança | Proteção contra injeção e validação de entrada | 🔴 Alta |
| S04 | 🔒 Segurança | Configuração segura de CORS | 🔴 Alta |
| S05 | 🔒 Segurança | Proteção de senhas e dados sensíveis | 🔴 Alta |
| S06 | 🔒 Segurança | Headers de segurança HTTP | 🟡 Média |
| S07 | 🔒 Segurança | Rate limiting e proteção contra força bruta | 🟡 Média |
| S08 | 🔒 Segurança | Auditoria e logs de segurança | 🟡 Média |
| S09 | 🔒 Segurança | Proteção de arquivos e upload seguro | 🟡 Média |
| S10 | 🔒 Segurança | Revisão de dependências e vulnerabilidades | 🟡 Média |

**Total: 65 cards** · Backend: 15 · Mobile: 10 · Admin: 10 · Docs: 10 · Testes: 10 · Segurança: 10

---

## ✅ Instruções de confirmação

Após revisar, escolha uma das opções:

- **"criar todos os cards aprovados"** → cria os 65 cards no board
- **"criar cards de backend"** → cria apenas B01–B15
- **"criar cards de mobile"** → cria apenas M01–M10
- **"criar cards de admin"** → cria apenas A01–A10
- **"criar cards de documentação"** → cria apenas D01–D10
- **"criar cards de testes"** → cria apenas T01–T10
- **"criar cards de segurança"** → cria apenas S01–S10
- **"criar cards B01, T02, S03"** → cria cards específicos por código
