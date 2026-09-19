> O Back-end do FieldOps concentra a inteligência de validação, segurança, integridade de dados e persistência oficial via API REST.

### 1. Arquitetura da API REST (`Java + Spring Boot`)
* **Estrutura de Contratos (`/api/v1`)**
  * Endpoints versionados por domínio (Auth, Usuários, Clientes, Locais, Equipamentos, Modelos, Inspeções, Evidências, Não Conformidades, Revisões).
  * Payloads de erro padronizados (`timestamp`, `status`, `code`, `message`, `path`, `requestId`, `fieldErrors`).
* **Segurança e Controle de Acesso (AuthN / AuthZ)**
  * Autenticação via **JWT (Bearer Token)** e hash de senha (`BCrypt`).
  * **Validação de Perfis na API**: Proteção rigorosa de rotas por perfil (`RN-003`, `RN-004`, `RN-005`).
  * *Política de Log*: Credenciais e tokens estritamente proibidos em logs (`RN-007`).

---

### 2. Mecanismo de Sincronização e Confiabilidade Offline-First
* **Endpoints Bidirecionais (`/mobile/sync/push` e `/mobile/sync/pull`)**
  * Processamento em lote de operações do campo.
  * **Idempotência**: Uso de `operationId` único para evitar duplicação em reenvios de rede (`RN-067`, `RN-068`).
* **Dependências, Falhas e Conflitos**
  * Processamento respeitando árvore de dependências (`RN-069`); falha pontual não apaga o restante da fila (`RN-070`).
  * **Detecção de Conflitos**: Servidor identifica divergências (`RN-075`); dados aprovados no servidor prevalecem sobre atualizações tardias de campo (`RN-076`).

---

### 3. Banco de Dados e Modelagem de Persistência (`PostgreSQL`)
* **Identificadores Distribuídos**
  * Uso massivo de **UUIDs** para geração autônoma offline sem colisão de chave primária.
* **Imutabilidade e Snapshots**
  * Versões de modelos publicadas são imutáveis (`RN-019`).
  * Cada inspeção gera um **Snapshot** estático dos itens utilizados (`RN-021`), protegendo o histórico contra alteração posterior do template.
* **Integridade Histórica e Soft Delete**
  * Aplicação de exclusão lógica (`ativo = false` / status inativo): entidades históricas **nunca são excluídas fisicamente** via fluxo comum (`RN-002`, `RN-013`, `RN-088`).
* **Auditoria e Armazenamento Externo**
  * Trilha de auditoria para alterações críticas (usuário, data, ação, entidade - `RN-086`).
  * Arquivos pesados (evidências/fotos) desacoplados em Object Storage (S3/equivalente), mantendo metadados no relacional.
