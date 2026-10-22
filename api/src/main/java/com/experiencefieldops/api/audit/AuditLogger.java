package com.experiencefieldops.api.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Serviço central de auditoria de segurança (issue #78).
 *
 * Responsabilidades:
 *   - Logar tentativas de login (sucesso e falha)
 *   - Logar acessos negados (403)
 *   - Logar operações CRUD em entidades críticas
 *   - Logar sincronizações offline
 *
 * Regras de segurança nos logs (issue #78 — dados confidenciais):
 *   ✗ Nunca logar senha ou passwordHash
 *   ✗ Nunca logar token completo — apenas o prefixo quando necessário
 *   ✗ Nunca logar dados de cartão ou documentos sensíveis
 *   ✓ Logar apenas: userId, email, IP, endpoint, timestamp, resultado
 *
 * O logger usa o nome "AUDIT" para permitir roteamento independente
 * (ex: arquivo separado, alerta no ELK) sem afetar o logging geral.
 *
 * Formato de saída (perfil prod — JSON):
 *   { "@timestamp": "...", "level": "INFO", "logger_name": "AUDIT",
 *     "message": "[AUTH] LOGIN_SUCCESS email=user@x.com ip=1.2.3.4" }
 */
@Component
public class AuditLogger {

    /** Logger nomeado "AUDIT" — configurado separadamente no logback-spring.xml */
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    // ── Autenticação ────────────────────────────────────────────────────────

    /**
     * Login realizado com sucesso.
     *
     * Nota sobre privacidade: o e-mail é mascarado (ex: jo***@empresa.com)
     * para reduzir exposição em logs, atendendo o item #78 "dados confidenciais".
     * O domínio é preservado para facilitar rastreamento operacional.
     * Se o revisor precisar do e-mail completo para auditoria, remover o mask().
     *
     * @param email email do usuário autenticado
     * @param ip    IP de origem da requisição
     */
    public void loginSuccess(String email, String ip) {
        log.info("[AUTH] LOGIN_SUCCESS email={} ip={}", mask(email), ip);
    }

    /**
     * Tentativa de login com credenciais inválidas.
     *
     * ATENÇÃO: nunca passar a senha como parâmetro para este método — issue #78.
     * Apenas o motivo da falha (reason) deve ser registrado, nunca a senha tentada.
     *
     * @param email  email informado (pode não existir no sistema)
     * @param ip     IP de origem
     * @param reason motivo da falha (ex: "credenciais inválidas", "usuário inativo")
     */
    public void loginFailure(String email, String ip, String reason) {
        log.warn("[AUTH] LOGIN_FAILURE email={} ip={} reason={}", mask(email), ip, reason);
    }

    // ── Acessos negados ─────────────────────────────────────────────────────

    /**
     * Acesso negado (403) a um endpoint protegido.
     *
     * @param userId   ID do usuário que tentou o acesso (ou "anonymous")
     * @param endpoint endpoint acessado (ex: "GET /api/v1/users")
     * @param ip       IP de origem
     */
    public void accessDenied(String userId, String endpoint, String ip) {
        log.warn("[ACCESS] DENIED userId={} endpoint={} ip={}", userId, endpoint, ip);
    }

    // ── Operações em entidades críticas ────────────────────────────────────

    /**
     * Entidade criada.
     *
     * Entidades atualmente com auditoria: User, Client, Equipment, Location.
     *
     * TODO (issue #78): adicionar auditoria em InspectionOrder e Review
     * quando essas entidades forem implementadas no backend.
     * Exemplo de uso futuro:
     *   auditLogger.entityCreated("InspectionOrder", order.getId(), actorId);
     *
     * @param entity   nome da entidade (ex: "User", "Client", "Equipment")
     * @param entityId ID da entidade recém-criada
     * @param actorId  ID do usuário que executou a ação (ou "system" para seeds/batch)
     */
    public void entityCreated(String entity, Object entityId, String actorId) {
        log.info("[CRUD] CREATED entity={} id={} actorId={}", entity, entityId, actorId);
    }

    /**
     * Entidade atualizada.
     *
     * @param entity   nome da entidade
     * @param entityId ID da entidade
     * @param actorId  ID do usuário que executou a ação
     */
    public void entityUpdated(String entity, Object entityId, String actorId) {
        log.info("[CRUD] UPDATED entity={} id={} actorId={}", entity, entityId, actorId);
    }

    /**
     * Entidade excluída.
     *
     * @param entity   nome da entidade
     * @param entityId ID da entidade excluída
     * @param actorId  ID do usuário que executou a ação
     */
    public void entityDeleted(String entity, Object entityId, String actorId) {
        log.warn("[CRUD] DELETED entity={} id={} actorId={}", entity, entityId, actorId);
    }

    // ── Sincronização offline ───────────────────────────────────────────────

    /**
     * Sincronização offline registrada.
     *
     * TODO: chamar este método quando o endpoint de sync for implementado.
     *
     * @param userId    ID do usuário que iniciou a sync
     * @param result    resultado da operação (ex: "SUCCESS", "PARTIAL", "FAILURE")
     * @param itemCount número de itens sincronizados
     */
    public void syncOffline(String userId, String result, int itemCount) {
        log.info("[SYNC] OFFLINE userId={} result={} items={}", userId, result, itemCount);
    }

    // ── Utilitário interno ──────────────────────────────────────────────────

    /**
     * Mascara a parte local do e-mail para reduzir exposição em logs.
     * Exemplo: "joao.silva@empresa.com" → "jo***@empresa.com"
     *
     * Preserva o domínio para facilitar rastreamento sem expor o endereço completo.
     */
    private String mask(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) return local + "***@" + domain;
        return local.substring(0, 2) + "***@" + domain;
    }
}
