> O Front-end do FieldOps é composto por duas interfaces distintas que consomem a mesma API REST: o **Aplicativo Mobile** (técnico em campo) e o **Painel Admin Web** (gestão e supervisão).

### 1. Módulo Mobile: Aplicativo do Técnico (`Expo + React Native`)
> *Foco operacional: Experiência de campo robusta, conectividade intermitente (Offline-First) e integração de hardware.*

* **Autenticação, Sessão e Segurança Local**
  * Login integrado a tokens JWT com armazenamento seguro (`Secure Store`).
  * Tratamento visual para sessões expiradas ou bloqueios de usuário (`RN-008`).
* **Gestão de Tarefas e Sincronização Local (Engine Offline)**
  * Home e listagem de inspeções filtradas estritamente para o técnico logado (`RN-004`).
  * Persistência em **SQLite** garantindo acesso offline (`RN-065`) e sobrevivência de dados ao fechar o app (`RN-066`).
  * **Outbox Visual**: Indicador da quantidade de operações pendentes (`RN-071`) e status da última sincronização (`RN-072`).
* **Fluxo de Execução e Recursos Nativos**
  * **Scanner de QR Code**: Identificação única de equipamento (`RN-011`) com alerta visual imediato em caso de divergência de local (`RN-064`).
  * **Câmera e GPS**: Solicitação de permissão de hardware (`RN-058`), captura pontual de coordenadas (lat, long, precisão, timestamp no início/conclusão - `RN-061/062`) e evidências fotográficas.
* **Formulário Dinâmico e Checklist Inteligente**
  * Renderizador baseado em *snapshot* da versão do modelo baixada.
  * Suporte a múltiplos tipos: `TEXT_SHORT`, `TEXT_LONG`, `NUMBER`, `BOOLEAN`, `CONFORMITY`, `SINGLE_CHOICE`, `DATE`, fotografia.
  * **Validações em Tempo Real**: Itens obrigatórios (`RN-037`), observação automática para não conformidades (`RN-038`) e exigência mandatória de fotos para itens críticos (`RN-039`).
  * Barra de progresso proporcional aos itens aplicáveis (`RN-040`).
* **Bloqueio Pós-Conclusão**
  * Transição visual para **Somente Leitura / Bloqueada** (`RN-043`) pós-finalização local, liberada apenas via reprovação de supervisor.

---

### 2. Módulo Web: Painel Administrativo (`Angular`)
> *Foco gerencial: Governança de dados, criação de templates, agendamento estratégico e revisão de conformidade.*

* **Dashboard e Indicadores Gerais**
  * Resumos consolidados, métricas de andamento, atrasos e pendências de revisão.
* **Módulo de Cadastros Base (CRUDs Operacionais)**
  * Gestão de Usuários (perfis `ADMIN`/`SUPERVISOR`), Clientes, Locais (vinculados a clientes - `RN-009`) e Equipamentos (vinculados a locais com QR Code único - `RN-010/011`).
* **Construtor de Modelos de Inspeção (Template Builder)**
  * Interface para rascunhos (título, categoria, seções e itens tipados).
  * **Versionamento**: Bloqueio de alterações destrutivas em modelos publicados, exigindo nova versão (`RN-019`, `RN-020`).
* **Módulo de Planejamento e Atribuição**
  * Vinculação de modelo publicado, cliente, local, data prevista e atribuição a técnico ativo (`RN-024`, `RN-025`, `RN-027`).
* **Central de Revisão e Aprovação**
  * Visão analítica por abas: cabeçalho, dados do técnico, progresso, checklist detalhado, galeria de evidências e não conformidades.
  * **Ações de Decisão**:
    * **Aprovação**: Registra supervisor, data e comentário opcional (`RN-081`).
    * **Reprovação**: Abre modal obrigatório de justificativa (`RN-080`).
  * **Proteção Visual**: Selo de proteção definitiva e bloqueio de edição comum em inspeções aprovadas (`RN-082`).
