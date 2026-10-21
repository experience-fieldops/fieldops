# Dependências Críticas — FieldOps

> Documento criado para o issue #80 — Revisão de dependências e gestão de vulnerabilidades.
>
> **Atualizar a cada sprint** ao adicionar, atualizar ou remover dependências.
> Antes de adicionar qualquer dependência nova, verifique:
>   1. Última atualização do pacote (manutenção ativa)
>   2. Histórico de CVEs no [NVD](https://nvd.nist.gov/) ou [Snyk Advisor](https://snyk.io/advisor/)
>   3. Número de downloads/estrelas (reputação do pacote)
>   4. Licença compatível com o projeto

---

## Backend (Java / Maven)

Gerenciado via `api/pom.xml`. Versões controladas pelo BOM do Spring Boot parent — não
sobrescrever versões de dependências gerenciadas pelo BOM sem motivo justificado.

| Dependência | Versão | Escopo | Finalidade | CVE Verificado em |
|-------------|--------|--------|------------|-------------------|
| spring-boot-starter-parent | 3.3.5 | BOM | Gerenciamento de versões e dependências | Out/2026 |
| spring-boot-starter-web | (BOM) | compile | API REST com Spring MVC | Out/2026 |
| spring-boot-starter-data-jpa | (BOM) | compile | Persistência com Hibernate/JPA | Out/2026 |
| spring-boot-starter-security | (BOM) | compile | Segurança, autenticação e autorização | Out/2026 |
| spring-boot-starter-validation | (BOM) | compile | Validação de entrada com Bean Validation | Out/2026 |
| spring-boot-devtools | (BOM) | runtime/dev | Hot reload em desenvolvimento | Out/2026 |
| com.h2database:h2 | (BOM) | runtime/dev | Banco de dados em memória para desenvolvimento | Out/2026 |
| org.projectlombok:lombok | (BOM) | compile/apt | Redução de boilerplate (getters, setters, construtores) | Out/2026 |
| spring-boot-starter-test | (BOM) | test | JUnit 5, Mockito, AssertJ | Out/2026 |
| spring-security-test | (BOM) | test | Utilitários de teste para Spring Security | Out/2026 |
| **com.bucket4j:bucket4j-core** | **8.10.1** | compile | Rate limiting — algoritmo Token Bucket (issue #77) | Out/2026 |

### Como verificar vulnerabilidades (backend)

```bash
# Executa o OWASP Dependency-Check e gera relatório em target/dependency-check-report.html
mvn dependency-check:check
```

> Configure a variável de ambiente `NVD_API_KEY` para evitar throttling na API do NVD.
> Obtenha em: https://nvd.nist.gov/developers/request-an-api-key

---

## Frontend (TypeScript / npm)

Gerenciado via `Front/package.json`. Todas as versões são **fixas** (sem `^` ou `~`) — issue #80.

| Dependência | Versão | Tipo | Finalidade | CVE Verificado em |
|-------------|--------|------|------------|-------------------|
| react | 18.3.1 | dep | Biblioteca principal de UI | Out/2026 |
| react-dom | 18.3.1 | dep | Renderização React no DOM | Out/2026 |
| react-router-dom | 6.30.6 | dep | Roteamento client-side (SPA) | Out/2026 |
| axios | 1.20.0 | dep | Cliente HTTP para comunicação com a API | Out/2026 |
| react-hook-form | 7.54.2 | dep | Gerenciamento de formulários | Out/2026 |
| lucide-react | 0.468.0 | dep | Ícones SVG | Out/2026 |
| @types/react | 18.3.12 | devDep | Tipos TypeScript para React | Out/2026 |
| @types/react-dom | 18.3.1 | devDep | Tipos TypeScript para React DOM | Out/2026 |
| @vitejs/plugin-react | 4.3.4 | devDep | Plugin Vite para React (Babel/SWC) | Out/2026 |
| typescript | 5.6.2 | devDep | Compilador TypeScript | Out/2026 |
| vite | 6.4.5 | devDep | Build tool e dev server | Out/2026 |

> **⚠️ Vulnerabilidade pendente — ação necessária:**
> `react-router-dom` 6.30.6 ainda possui 2 CVEs de severidade **moderate**
> (GHSA-wrjc-x8rr-h8h6 e GHSA-337j-9hxr-rhxg).
> O fix requer migração para `react-router-dom@7.x`, que é uma breaking change
> (mudança na API de routing). Avaliar e planejar a migração em sprint dedicada.
> Referência: https://github.com/advisories/GHSA-wrjc-x8rr-h8h6

### Como verificar vulnerabilidades (frontend)

```bash
# Audita as dependências e exibe vulnerabilidades conhecidas
cd Front
npm audit

# Para corrigir automaticamente as vulnerabilidades sem breaking changes
npm audit fix

# Para corrigir vulnerabilidades com breaking changes (avaliar impacto antes)
npm audit fix --force
```

---

## Política de Atualização

1. **A cada sprint**: executar `mvn dependency-check:check` e `npm audit` e registrar resultado aqui.
2. **Ao adicionar uma dependência nova**:
   - Verificar manutenção ativa (último commit < 6 meses)
   - Verificar histórico de CVEs no NVD e Snyk
   - Verificar licença
   - Usar versão **exata** (sem `^`, `~` ou ranges)
   - Adicionar à tabela acima com a data de verificação
3. **CVE identificado**: abrir issue de segurança com prioridade Alta e corrigir na sprint corrente.
4. **Dependência abandonada**: substituir por alternativa mantida ativamente.

---

## Referências

- [NVD — National Vulnerability Database](https://nvd.nist.gov/)
- [Snyk Advisor](https://snyk.io/advisor/)
- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [npm audit docs](https://docs.npmjs.com/cli/v10/commands/npm-audit)
