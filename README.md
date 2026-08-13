FieldOps — Plataforma de Inspeção em Campo
> Projeto acadêmico integrado (Mobile, Backend e Web Administrativo) desenvolvido para a disciplina de Análise e Desenvolvimento de Sistemas.
![Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Mobile](https://img.shields.io/badge/mobile-Expo%20%2F%20React%20Native-blue)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot-brightgreen)
![Frontend](https://img.shields.io/badge/admin-Angular-red)
![Database](https://img.shields.io/badge/database-PostgreSQL-336791)
Índice
Sobre o projeto
Componentes do repositório
Fluxo principal do produto
Como executar o projeto
Equipe e responsabilidades
Documentação técnica
Dados de demonstração
Escopo do MVP
Limitações conhecidas
Como contribuir
Licença
Sobre o projeto
O FieldOps é uma plataforma digital para planejamento, execução, acompanhamento e revisão de inspeções técnicas realizadas em campo. Ele substitui processos baseados em formulários impressos, planilhas e mensagens informais por um fluxo digital integrado:
Técnicos usam um aplicativo mobile para consultar inspeções atribuídas, identificar equipamentos por QR Code, responder checklists, registrar evidências e concluir atividades — inclusive sem conexão com a internet.
Administradores e supervisores usam uma interface web para cadastrar dados, criar modelos de inspeção, agendar atividades, atribuir técnicos, acompanhar a execução e aprovar ou reprovar os resultados.
Uma API REST centraliza autenticação, regras de negócio, persistência, auditoria e a integração entre o app mobile e a interface administrativa.
Componentes do repositório
```
fieldops/
├── mobile/    # Aplicativo Expo + React Native + TypeScript
├── admin/     # Interface administrativa Angular + TypeScript
├── api/       # API REST Java + Spring Boot + PostgreSQL
└── docs/      # Documentação, diagramas e contrato OpenAPI
```
Componente	Tecnologia	Pasta	README específico
Aplicativo mobile	Expo + React Native + TypeScript	`/mobile`	`/mobile/README.md`
Interface administrativa web	Angular + TypeScript	`/admin`	`/admin/README.md`
API REST	Java + Spring Boot + PostgreSQL	`/api`	`/api/README.md`
Fluxo principal do produto
```
Configuração administrativa (usuários, clientes, locais, equipamentos)
        ↓
Criação e publicação do modelo de inspeção
        ↓
Agendamento e atribuição ao técnico
        ↓
Disponibilização ao técnico (sincronização)
        ↓
Execução em campo (checklist, fotos, QR Code, localização — inclusive offline)
        ↓
Sincronização das alterações
        ↓
Revisão do supervisor
        ↓
Aprovação ou reprovação (com solicitação de correção)
```
Como executar o projeto
Pré-requisitos
Node.js e Expo CLI (para o mobile)
Node.js e Angular CLI (para o admin)
Java e Maven/Gradle (para a API)
PostgreSQL
Passos gerais
```bash
# 1. Clonar o repositório
git clone [url-do-repositorio]
cd fieldops

# 2. Subir o banco PostgreSQL e executar as migrações da API
cd api && [comando de migração — ver api/README.md]

# 3. Executar a API
[comando de execução — ver api/README.md]

# 4. Executar a interface administrativa
cd ../admin && npm install && npm start

# 5. Executar o aplicativo mobile
cd ../mobile && npm install && npx expo start
```
Instruções detalhadas de cada componente estão nos respectivos READMEs.
Equipe e responsabilidades
Frente	Responsáveis
Mobile — Fundação e navegação	`[nome]`
Mobile — Checklist e formulários	`[nome]`
Mobile — Recursos nativos, offline e sincronização	`[nome]`
Backend — Identidade, acesso e cadastros	`[nome]`
Backend — Modelos, planejamento, sincronização e revisão	`[nome]`
Admin Web — Fundação, autenticação e cadastros	`[nome]`
Admin Web — Modelos, planejamento e revisão	`[nome]`
Product Owner / QA / Documentação	`[nome]`
Documentação técnica
Contrato da API (OpenAPI/Swagger): `[link ou caminho a definir]`
Modelo de dados / diagrama de classe: `[link ou caminho a definir]`
Backlog do produto (Notion): `[link a definir]`
Roadmap e sprints: `[link a definir]`
Dados de demonstração
Perfil	E-mail	Senha
Administrador	`[a definir]`	`[a definir]`
Supervisor	`[a definir]`	`[a definir]`
Técnico	`[a definir]`	`[a definir]`
Escopo do MVP
[x] Cadastro básico pelo administrador
[x] Criação de modelo de inspeção pelo supervisor
[x] Agendamento e atribuição da inspeção
[x] Recebimento da inspeção pelo técnico no aplicativo
[x] Execução do checklist com fotos, QR Code e localização
[x] Trabalho temporário sem internet
[x] Sincronização dos dados pendentes
[x] Revisão, aprovação ou reprovação pelo supervisor
Fora do escopo do MVP: app exclusivo para clientes, portal público, multiempresa, faturamento, roteirização automática, transmissão contínua de localização, chamadas de vídeo, assinatura eletrônica, integração com ERPs, IA para diagnóstico, integração com IoT.
Limitações conhecidas
Atualizar esta seção ao longo do desenvolvimento com funcionalidades incompletas ou limitações técnicas identificadas.
Como contribuir
Criar uma branch a partir de `main`/`develop` seguindo o padrão `feature/nome-da-funcionalidade`.
Abrir um Pull Request descrevendo a história de usuário e os critérios de aceitação atendidos.
Garantir que o código passa no lint e na verificação de tipos antes de solicitar revisão.
Aguardar revisão de ao menos um integrante antes do merge.
Licença
Projeto acadêmico — uso restrito à disciplina e à instituição de ensino.