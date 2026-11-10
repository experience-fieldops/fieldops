# Segurança — operação e validação

## Limites de requisição

| Endpoint / grupo | Limite | Chave atual | Resposta ao exceder |
|---|---:|---|---|
| `POST /api/public/auth/login` | 5 tentativas por minuto | IP (prioriza `X-Forwarded-For`) | `429 Too Many Requests` e log de bloqueio |
| API geral, exceto `/api/public/**` e `/h2-console/**` | 100 requisições por minuto | IP (prioriza `X-Forwarded-For`) | `429 Too Many Requests` e log de bloqueio |

O backend ainda não valida JWT. Portanto, o limite geral por subject de token não
está implementado nem validado. Não usar o header `Authorization` como identidade;
quando JWT for implementado, a chave deve ser o subject de um token validado. Os
buckets são mantidos em memória e não compartilham estado entre instâncias.

## Upload

O upload aceita `jpg`, `jpeg`, `png` e `webp`, verifica os magic bytes,
renomeia o arquivo com UUID e limita arquivo e requisição a 10 MB. O diretório
atual é `${java.io.tmpdir}/fieldops-uploads`; é adequado para desenvolvimento e
testes, não é uma estratégia de armazenamento persistente.

## Configuração da API neste checkout

O backend usa apenas `api/src/main/resources/application.properties`. Não há
perfil de produção configurado. O H2 console está habilitado para desenvolvimento;
não exponha esta configuração como ambiente de produção. O diretório de upload
temporário e o banco H2 também não são configuração de produção.

## Validações pendentes ou limitadas

- **Headers (#76):** os headers são cobertos por teste HTTP. A nota do
  securityheaders.com permanece verificação manual após deploy em staging; não
  foi executada contra produção.
- **JWT e autorização (#77/#78):** JWT não existe e as rotas atuais usam
  `permitAll()`. O AccessDeniedAuditHandler pode ser testado isoladamente, mas
  não é exercitado por uma rota protegida; logs com actorId real e `userId` no
  MDC dependem da autenticação.
- **Domínio e sincronização (#78):** `InspectionOrder`, `Review` e o endpoint
  `syncOffline` não existem no backend deste checkout. A auditoria integrada
  dessas operações continua pendente.
- **Logs JSON (#78):** o perfil de produção e sua configuração Logback foram
  removidos junto com o ambiente de produção. Os eventos de auditoria continuam
  disponíveis no logging padrão; logging JSON deve ser reavaliado quando houver
  configuração de ambiente correspondente.
- **Admin/frontend (#76/#80):** não há aplicação Admin, diretório `Front` nem
  `package.json` neste checkout. CSP de frontend, `npm audit` e avaliação/migração
  de `react-router-dom` não podem ser validados aqui.
- **Dependências (#80):** execute `.\api\mvnw.cmd -f .\api\pom.xml dependency-check:check` na raiz e
  registre o resultado de cada sprint em `dependencies.md`. A auditoria npm só
  poderá ser feita no checkout que contenha os manifestos do frontend.
  Nesta revisão, o Dependency-Check iniciou a atualização do NVD, mas foi
  interrompido em 14% (sem `NVD_API_KEY`); nenhum resultado de vulnerabilidades
  foi produzido.

## Testes locais

Na raiz do repositório, execute:

```powershell
.\api\mvnw.cmd -f .\api\pom.xml test
```
