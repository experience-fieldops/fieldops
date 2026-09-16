# Cards — Telas Mobile (FieldOps)

Cards das telas do protótipo mobile que ainda não possuem issue dedicada no GitHub.
Hoje elas existem apenas como itens de checklist dentro da issue [#81 — Prototipação de telas no Figma](https://github.com/experience-fieldops/fieldops/issues/81).

Referência de telas do protótipo: Login · Home (Minhas Inspeções) · Sincronização · Perfil.

> A **Tela de Login** já possui card próprio e concluído: [#110](https://github.com/experience-fieldops/fieldops/issues/110).

---

## [Mobile] Tela Home — Minhas Inspeções

**Grupo:** Execução | **Prioridade:** 🔴 Alta
**Labels:** `mobile`, `alta`, `execucao`

### Descrição
Implementar a tela inicial do técnico, exibindo o resumo e a lista de inspeções atribuídas,
com indicadores de status e navegação para o detalhe/execução.

### Tarefas
- [ ] Implementar layout conforme o protótipo.
- [ ] Cabeçalho com marca FieldOps e indicador de modo offline.
- [ ] Cartões de resumo: total atribuídas, em andamento, atrasadas.
- [ ] Lista de inspeções com card por item (título, prazo, status/prioridade, progresso %).
- [ ] Barra de progresso por inspeção.
- [ ] Filtro/segmentação por status (atribuída, em andamento, atrasada).
- [ ] Navegação para o detalhe da inspeção ao tocar no card.
- [ ] Barra de navegação inferior (Inspeções, Sincronizar, Perfil).
- [ ] Estado vazio (nenhuma inspeção atribuída).
- [ ] Consumir a API de inspeções do técnico autenticado.

### Endpoints relacionados (backend)
- `GET /api/v1/inspections/assigned` — inspeções do técnico autenticado
- `GET /api/v1/inspections/{id}` — detalhe da inspeção
- `GET /api/v1/inspections/{id}/responses` (progresso via `getProgress`)

### Critérios de aceitação
- [ ] Lista mostra apenas as inspeções do técnico autenticado.
- [ ] Contadores de resumo refletem os status reais.
- [ ] Progresso exibido por inspeção corresponde ao backend.
- [ ] Tocar em um card abre o detalhe correto.

---

## [Mobile] Tela de Sincronização

**Grupo:** Sincronização | **Prioridade:** 🔴 Alta
**Labels:** `mobile`, `alta`, `sincronizacao`

### Descrição
Implementar a tela de sincronização offline, exibindo a fila de operações pendentes,
os itens enviados aguardando aprovação e os já aprovados, com feedback de estado por item.

### Tarefas
- [ ] Implementar layout conforme o protótipo.
- [ ] Banner/indicador de modo offline (sem conexão).
- [ ] Seção "Fila para sincronização" com itens pendentes.
- [ ] Seção "Enviados — aguardando aprovação".
- [ ] Seção "Aprovados".
- [ ] Ícone/estado por item (pendente, enviando, enviado, aprovado, erro).
- [ ] Ação de sincronizar (manual) e sincronização automática ao voltar a conexão.
- [ ] Feedback de sincronização: sucesso, parcial e erro.
- [ ] Barra de navegação inferior (Inspeções, Sincronizar, Perfil).

### Endpoints relacionados (backend)
- `POST /api/v1/sync/batch` — enviar lote de operações da outbox
- `GET /api/v1/inspections/{id}/sync-status` — status de sincronização da inspeção

### Critérios de aceitação
- [ ] Reenvio de operação já processada não duplica (idempotência).
- [ ] Lote parcialmente falho mostra sucesso/erro por item.
- [ ] Estado offline/online refletido corretamente na UI.
- [ ] Itens migram entre as seções conforme o status retornado.

---

## [Mobile] Tela de Perfil — Meu Desempenho

**Grupo:** Perfil | **Prioridade:** 🟡 Média
**Labels:** `mobile`, `media`

### Descrição
Implementar a tela de perfil do usuário, exibindo dados pessoais, indicadores de desempenho
e acesso a configurações, notificações, ajuda e logout.

### Tarefas
- [ ] Implementar layout conforme o protótipo.
- [ ] Cabeçalho com avatar/iniciais, nome, cargo e organização.
- [ ] Cartões de desempenho: inspeções concluídas, % de aprovações, reprovações.
- [ ] Itens de menu: Organização, Configurações, Notificações, Ajuda e suporte.
- [ ] Botão "Sair da conta" (logout — invalidar token local).
- [ ] Barra de navegação inferior (Inspeções, Sincronizar, Perfil).

### Endpoints relacionados (backend)
- `GET /api/user/{id}` — dados do usuário (ADMIN) *(perfil do próprio usuário: endpoint `me` a definir)*
- Métricas de desempenho (concluídas, % aprovação, reprovações) — **endpoint a definir**

### Critérios de aceitação
- [ ] Dados exibidos correspondem ao usuário autenticado.
- [ ] Indicadores de desempenho refletem os dados reais quando o endpoint existir.
- [ ] Logout limpa o token e retorna à tela de Login.

### Observações / pendências de backend
- Não há endpoint de "meu perfil" (`GET /api/auth/me` ou `GET /api/users/me`) — hoje o `UserController` é somente ADMIN.
- Não há endpoint de métricas de desempenho do técnico (concluídas, % de aprovação, reprovações).

---

## [Mobile] Modal de Detalhe da Inspeção

**Grupo:** Execução | **Prioridade:** 🔴 Alta
**Labels:** `mobile`, `alta`, `execucao`

### Descrição
Implementar o modal/detalhe que abre ao tocar em uma inspeção na Home, exibindo os dados
da ordem e as ações de iniciar a inspeção ou cancelar/fechar.

### Tarefas
- [ ] Implementar layout conforme o protótipo (modal sobre a Home).
- [ ] Exibir título, código (ex: INSP-02), prioridade e prazo.
- [ ] Exibir dados: técnico responsável, equipamento/ativo, local/área.
- [ ] Barra de progresso da inspeção.
- [ ] Botão "Cancelar" (fecha o modal / cancela conforme regra).
- [ ] Botão "Iniciar" (inicia a inspeção e navega para a execução do checklist).
- [ ] Bloquear "Iniciar" quando a inspeção não estiver em estado atribuível.

### Endpoints relacionados (backend)
- `GET /api/v1/inspections/{id}` — detalhe da inspeção (com snapshot)
- `PATCH /api/v1/inspections/{id}/start` — iniciar inspeção (técnico)
- `PATCH /api/v1/inspections/{id}/cancel` — cancelar inspeção com justificativa

### Critérios de aceitação
- [ ] Modal exibe os dados corretos da inspeção selecionada.
- [ ] "Iniciar" só é permitido para inspeções atribuídas ao técnico.
- [ ] Iniciar registra o início e leva o usuário para a execução do checklist.
- [ ] Cancelamento respeita a regra de justificativa obrigatória.

---

## [Mobile] Tela de Execução do Checklist

**Grupo:** Execução | **Prioridade:** 🔴 Alta
**Labels:** `mobile`, `alta`, `execucao`

### Descrição
Implementar a tela de execução do checklist item a item, com registro da resposta,
captura de evidências (câmera/galeria), campo de observação e navegação entre itens.

### Tarefas
- [ ] Implementar layout conforme o protótipo.
- [ ] Cabeçalho FieldOps com botão voltar.
- [ ] Exibir título e descrição do item, com marcação de obrigatório (*).
- [ ] Opções de resposta de conformidade: Conforme, Atenção, Inconforme, Não verificado.
- [ ] Destaque visual da opção selecionada (verde/amarelo/vermelho).
- [ ] Bloco de evidência (Câmera / Galeria) com preview do arquivo anexado.
- [ ] Exibir evidência como obrigatória quando o item exigir (ex: Inconforme).
- [ ] Campo de observação (texto livre).
- [ ] Rodapé com "Retornar", contador "Item X de N" e "Avançar".
- [ ] Persistir resposta ao avançar (upsert por item do snapshot).
- [ ] Renderizar tipos de resposta além de conformidade (texto, número, boolean, data, escolha).
- [ ] Indicar item obrigatório não respondido.

### Endpoints relacionados (backend)
- `GET /api/v1/inspections/{id}/responses` — respostas + progresso
- `POST /api/v1/inspections/{id}/responses` — registrar/atualizar resposta (upsert)
- `PUT /api/v1/inspections/{id}/responses/{responseId}` — atualizar resposta
- `POST /api/v1/inspections/{id}/evidences` — upload de evidência
- `POST /api/v1/inspections/{id}/non-conformities` — registrar não conformidade (item Inconforme)

### Regras de negócio (referência)
- [ ] Resposta validada conforme o `response_type` (RN-036).
- [ ] Observação obrigatória para item não conforme quando configurado (RN-038).
- [ ] Item crítico não conforme exige ao menos uma evidência (RN-039).
- [ ] Após conclusão local, respostas ficam bloqueadas (RN-043).

### Critérios de aceitação
- [ ] Seleção de conformidade é registrada e destacada corretamente.
- [ ] Evidência exigida bloqueia o avanço quando ausente.
- [ ] Observação obrigatória bloqueia o salvamento quando ausente.
- [ ] Contador "Item X de N" e navegação Retornar/Avançar funcionam.
- [ ] Progresso da inspeção é atualizado conforme os itens respondidos.
