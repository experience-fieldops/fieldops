---
inclusion: always
---

# Configuração dos GitHub Projects

---

## 📋 Projeto Principal — Board FieldOps (project/1)

- **URL**: `https://github.com/orgs/experience-fieldops/projects/1`
- **Nome**: Board FieldOps
- **Project ID**: `PVT_kwDOEr55Rc4Bfuhc`
- **Uso**: Board geral do projeto — todas as frentes (Backend, Mobile, Admin, Docs, Testes, Segurança)

### Campo: Status
**Field ID**: `PVTSSF_lADOEr55Rc4BfuhczhZ_Yi4`

| Nome | ID |
|---|---|
| Sprint Backlog | `f07c4832` |
| Todo | `f75ad846` |
| In Progress | `47fc9ee4` |
| Code Review | `98f9a1dc` |
| Blocked | `1f386d49` |
| Done | `98236657` |

### Campo: Área
**Field ID**: `PVTSSF_lADOEr55Rc4BfuhczhaT9Uw`

| Nome | ID |
|---|---|
| Frontend | `1e1eca1a` |
| Backend | `b85ed028` |
| Banco de dados | `217e7c6f` |
| UX/UI | `064b8b84` |
| QA/TESTS | `89f6ebbe` |
| Documentation | `1db0e567` |
| Segurança | `849a8992` |

### Outros campos disponíveis
| Campo | Field ID |
|---|---|
| Title | `PVTF_lADOEr55Rc4BfuhczhZ_Yiw` |
| Assignees | `PVTF_lADOEr55Rc4BfuhczhZ_Yi0` |
| Labels | `PVTF_lADOEr55Rc4BfuhczhZ_Yi8` |
| Linked pull requests | `PVTF_lADOEr55Rc4BfuhczhZ_YjA` |
| Milestone | `PVTF_lADOEr55Rc4BfuhczhZ_YjE` |
| Repository | `PVTF_lADOEr55Rc4BfuhczhZ_YjI` |
| Reviewers | `PVTF_lADOEr55Rc4BfuhczhZ_YjQ` |
| Parent issue | `PVTF_lADOEr55Rc4BfuhczhZ_YjU` |
| Sub-issues progress | `PVTF_lADOEr55Rc4BfuhczhZ_YjY` |
| Start date | `PVTF_lADOEr55Rc4BfuhczhaT9Uw` |
| Priority | `PVTSSF_lADOEr55Rc4BfuhczhaT8yY` |

---

## 🟢 Project FieldOps (project/2)

- **URL**: `https://github.com/orgs/experience-fieldops/projects/2`
- **Nome**: Project FieldOps *(anteriormente: Backend)*
- **Project ID**: `PVT_kwDOEr55Rc4BgGI5`
- **Uso**: Board principal do projeto FieldOps — cards de todas as frentes

### Campo: Status
**Field ID**: `PVTSSF_lADOEr55Rc4BgGI5zhaURmI`

| Nome | ID |
|---|---|
| Backlog | `f75ad846` |
| Ready | `61e4505c` |
| In progress | `47fc9ee4` |
| In review | `df73e18b` |
| Done | `98236657` |

---

## 🏷️ Labels do repositório

Labels disponíveis em `experience-fieldops/fieldops`. **Sempre aplicar ao criar uma issue.**

### Por frente
| Label | Cor | Quando usar |
|---|---|---|
| `backend` | `#0075ca` | Issues de Backend (Spring Boot) |
| `mobile` | `#e4e669` | Issues de Mobile (Expo/React Native) |
| `admin` | `#d93f0b` | Issues de Admin Web (Angular) |
| `docs` | `#0052cc` | Issues de Documentação |
| `testes` | `#bfd4f2` | Issues de Testes (unitário, E2E, regressão) |
| `seguranca` | `#b60205` | Issues de Segurança/Cibersegurança |

### Por prioridade
| Label | Cor | Quando usar |
|---|---|---|
| `alta` | `#ee0701` | Prioridade 🔴 Alta |
| `media` | `#fbca04` | Prioridade 🟡 Média |
| `baixa` | `#0e8a16` | Prioridade 🟢 Baixa |

### Por grupo/tema
| Label | Cor | Quando usar |
|---|---|---|
| `fundacao` | `#c5def5` | Setup e configuração inicial |
| `cadastro` | `#fef2c0` | CRUD de entidades base |
| `autenticacao` | `#f9d0c4` | Auth, JWT, sessão |
| `planejamento` | `#c2e0c6` | Agendamento e atribuição |
| `execucao` | `#e99695` | Execução da inspeção |
| `sincronizacao` | `#7057ff` | Offline e sincronização |
| `revisao` | `#006b75` | Revisão e aprovação |

### Mapeamento automático: prefixo do título → labels
| Prefixo | Labels a aplicar |
|---|---|
| `[Backend]` | `backend` + prioridade + grupo |
| `[Mobile]` | `mobile` + prioridade + grupo |
| `[Admin]` | `admin` + prioridade + grupo |
| `[Docs]` | `docs` + prioridade |
| `[Testes]` | `testes` + prioridade |
| `[Segurança]` | `seguranca` + prioridade |

### Exemplo de criação completa de issue com labels
```powershell
$body = @{
  title  = "[Backend] CRUD de Usuários e Roles"
  body   = "..."
  labels = @("backend", "alta", "cadastro")
} | ConvertTo-Json
Invoke-RestMethod -Uri "https://api.github.com/repos/experience-fieldops/fieldops/issues" `
  -Method POST -Headers $headers -Body $body
```

---

## 🏢 Organização e repositório

- **Organização**: `experience-fieldops`
- **Repositório principal**: `experience-fieldops/fieldops`

---

## 📐 Regras para criação de cards

- Toda issue deve ser criada em `experience-fieldops/fieldops`
- Após criar a issue, adicionar ao projeto correto via GraphQL
- **Project FieldOps (project/2)** `PVT_kwDOEr55Rc4BgGI5` — board principal, status padrão: **Backlog** (`f75ad846`)
- **Board FieldOps (project/1)** `PVT_kwDOEr55Rc4Bfuhc` — board secundário/geral, status padrão: **Sprint Backlog** (`f07c4832`)

### Mapeamento frente → Área (project/1)
| Frente | Área | ID da opção |
|---|---|---|
| `[Backend]` | Backend | `b85ed028` |
| `[Mobile]` | Frontend | `1e1eca1a` |
| `[Admin]` | Frontend | `1e1eca1a` |
| `[Docs]` | Documentation | `1db0e567` |
| `[Testes]` | QA/TESTS | `89f6ebbe` |
| `[Segurança]` | Segurança | `849a8992` |

---

## ✅ Checklist obrigatório ao criar um novo card

Toda vez que for solicitada a criação de um card/issue, seguir **exatamente** esta ordem:

1. **Criar a issue** no repositório `experience-fieldops/fieldops`
   - Incluir `title`, `body` com descrição e tarefas
   - Incluir `labels` com: frente + prioridade + tema (ver tabelas acima)

2. **Adicionar ao project/1** via GraphQL (`addProjectV2ItemById`)
   - Project ID: `PVT_kwDOEr55Rc4Bfuhc`

3. **Definir Status** = `Sprint Backlog` (`f07c4832`)
   - Field ID: `PVTSSF_lADOEr55Rc4BfuhczhZ_Yi4`

4. **Definir Área** conforme o prefixo do título
   - Field ID: `PVTSSF_lADOEr55Rc4BfuhczhaT9Uw`
   - `[Backend]` → `b85ed028` | `[Mobile]` / `[Admin]` → `1e1eca1a` | `[Docs]` → `1db0e567` | `[Testes]` → `89f6ebbe` | `[Segurança]` → `849a8992`

### Exemplo completo de criação (PowerShell)
```powershell
$token = (Get-Content "$env:USERPROFILE\.kiro\settings\mcp.json" | ConvertFrom-Json).mcpServers.github.env.GITHUB_PERSONAL_ACCESS_TOKEN
$headers = @{ "Authorization" = "Bearer $token"; "Content-Type" = "application/json" }

# 1. Criar issue com labels
$issue = @{
  title  = "[Backend] Título do card"
  body   = "## Grupo: Fundação | Prioridade: Alta`n`n### Tarefas`n- [ ] tarefa 1"
  labels = @("backend", "alta", "fundacao")
} | ConvertTo-Json
$resp = Invoke-RestMethod "https://api.github.com/repos/experience-fieldops/fieldops/issues" -Method POST -Headers $headers -Body $issue
$nodeId = $resp.node_id

# 2. Adicionar ao project/1
$add = @{ query = "mutation { addProjectV2ItemById(input: {projectId: `"PVT_kwDOEr55Rc4Bfuhc`" contentId: `"$nodeId`"}) { item { id } } }" } | ConvertTo-Json
$item = (Invoke-RestMethod "https://api.github.com/graphql" -Method POST -Headers $headers -Body $add).data.addProjectV2ItemById.item.id

# 3. Status = Sprint Backlog
$status = @{ query = "mutation { updateProjectV2ItemFieldValue(input: {projectId: `"PVT_kwDOEr55Rc4Bfuhc`" itemId: `"$item`" fieldId: `"PVTSSF_lADOEr55Rc4BfuhczhZ_Yi4`" value: {singleSelectOptionId: `"f07c4832`"}}) { projectV2Item { id } } }" } | ConvertTo-Json
Invoke-RestMethod "https://api.github.com/graphql" -Method POST -Headers $headers -Body $status | Out-Null

# 4. Área = Backend
$area = @{ query = "mutation { updateProjectV2ItemFieldValue(input: {projectId: `"PVT_kwDOEr55Rc4Bfuhc`" itemId: `"$item`" fieldId: `"PVTSSF_lADOEr55Rc4BfuhczhaT9Uw`" value: {singleSelectOptionId: `"b85ed028`"}}) { projectV2Item { id } } }" } | ConvertTo-Json
Invoke-RestMethod "https://api.github.com/graphql" -Method POST -Headers $headers -Body $area | Out-Null

Write-Host "Card criado: #$($resp.number)"
```

---

## 🔧 Mutations GraphQL de referência

### Adicionar issue ao projeto
```graphql
mutation {
  addProjectV2ItemById(input: {
    projectId: "<PROJECT_ID>"
    contentId: "<NODE_ID_DA_ISSUE>"
  }) {
    item { id }
  }
}
```

### Definir Status
```graphql
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "<PROJECT_ID>"
    itemId: "<ITEM_ID>"
    fieldId: "<STATUS_FIELD_ID>"
    value: { singleSelectOptionId: "<OPTION_ID>" }
  }) {
    projectV2Item { id }
  }
}
```

### Definir Área (somente project/1)
```graphql
mutation {
  updateProjectV2ItemFieldValue(input: {
    projectId: "PVT_kwDOEr55Rc4Bfuhc"
    itemId: "<ITEM_ID>"
    fieldId: "PVTSSF_lADOEr55Rc4BfuhczhaT9Uw"
    value: { singleSelectOptionId: "<AREA_OPTION_ID>" }
  }) {
    projectV2Item { id }
  }
}
```
