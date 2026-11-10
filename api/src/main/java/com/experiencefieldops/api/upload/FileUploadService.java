package com.experiencefieldops.api.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço de upload seguro de arquivos (issue #79).
 *
 * Camadas de validação aplicadas em sequência:
 *
 *   1. Extensão do arquivo: aceita apenas jpg, jpeg, png, webp
 *   2. MIME type real via magic bytes: não confia apenas na extensão
 *      informada pelo cliente
 *   3. Tamanho máximo: 10 MB por arquivo
 *      (limite complementar ao configurado em application.properties)
 *   4. Renomeio com UUID: o nome original do cliente nunca é usado no servidor
 *   5. Armazenamento fora do diretório público web: configurável via
 *      application.properties (upload.directory)
 *
 * Magic bytes verificados:
 *   JPEG  → FF D8 FF
 *   PNG   → 89 50 4E 47 0D 0A 1A 0A
 *   WEBP  → 52 49 46 46 ... 57 45 42 50  (RIFF....WEBP)
 *
 * Configuração necessária em application.properties:
 *   upload.directory=/var/fieldops/uploads   ← fora do diretório público web
 */
@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    /** Extensões permitidas (issue #79 — validar extensão) */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /** Tamanho máximo: 10 MB (issue #79) */
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    /**
     * Mapeamento extensão → MIME type esperado após leitura dos magic bytes.
     * Usamos apenas os valores que o próprio detector interno retorna.
     */
    private static final Map<String, String> EXTENSION_TO_MIME = Map.of(
            "jpg",  "image/jpeg",
            "jpeg", "image/jpeg",
            "png",  "image/png",
            "webp", "image/webp"
    );

    /**
     * Diretório de armazenamento dos arquivos enviados.
     * Deve apontar para um caminho FORA do diretório público web.
     * Valor padrão adequado para desenvolvimento local.
     */
    @Value("${upload.directory:${java.io.tmpdir}/fieldops-uploads}")
    private String uploadDirectory;

    /**
     * Valida e armazena o arquivo enviado.
     *
     * @param file arquivo recebido via multipart/form-data
     * @return nome do arquivo salvo no servidor (UUID + extensão)
     * @throws FileUploadException se qualquer validação falhar
     * @throws IOException         se ocorrer erro de I/O ao salvar
     */
    public String store(MultipartFile file) throws IOException {
        validateNotEmpty(file);

        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "";

        String extension = extractExtension(originalFilename);
        validateExtension(extension);
        validateFileSize(file);
        validateMimeType(file, extension);

        // Renomeia com UUID — nunca usa o nome original do cliente (issue #79)
        String safeFilename = UUID.randomUUID() + "." + extension;

        Path uploadPath = Paths.get(uploadDirectory);
        Files.createDirectories(uploadPath); // cria o diretório se não existir

        Path destination = uploadPath.resolve(safeFilename);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("[UPLOAD] Arquivo salvo — nome original: '{}' → nome no servidor: '{}' | tamanho: {} bytes",
                originalFilename, safeFilename, file.getSize());

        return safeFilename;
    }

    // ── Validações privadas ───────────────────────────────────────────────────

    private void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Nenhum arquivo enviado ou arquivo vazio.");
        }
    }

    /**
     * Extrai e normaliza a extensão do nome do arquivo.
     * Rejeita nomes sem extensão ou com extensão dupla (ex: "virus.exe.jpg").
     */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileUploadException("O arquivo não possui extensão.");
        }

        // Rejeita nomes com múltiplas extensões para prevenir bypass
        String[] parts = filename.split("\\.");
        if (parts.length > 2) {
            throw new FileUploadException(
                    "Nome de arquivo inválido. Extensões duplas não são permitidas.");
        }

        return parts[parts.length - 1].toLowerCase();
    }

    /** Verifica se a extensão está na lista de permitidas (issue #79) */
    private void validateExtension(String extension) {
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileUploadException(
                    "Extensão não permitida: ." + extension +
                    ". Extensões aceitas: jpg, jpeg, png, webp.");
        }
    }

    /** Verifica tamanho máximo de 10 MB (issue #79) */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileUploadException(
                    String.format("Arquivo muito grande: %.2f MB. Tamanho máximo: 10 MB.",
                            file.getSize() / (1024.0 * 1024.0)));
        }
    }

    /**
     * Valida o MIME type real do arquivo via magic bytes (issue #79).
     *
     * Não confia no Content-Type informado pelo cliente nem na extensão do arquivo.
     * Lê os primeiros bytes do arquivo para identificar o formato real.
     *
     * Magic bytes verificados:
     *   JPEG  → primeiros 3 bytes: FF D8 FF
     *   PNG   → primeiros 8 bytes: 89 50 4E 47 0D 0A 1A 0A
     *   WEBP  → bytes 0-3: RIFF (52 49 46 46) + bytes 8-11: WEBP (57 45 42 50)
     */
    private void validateMimeType(MultipartFile file, String extension) throws IOException {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new FileUploadException("Arquivo muito pequeno ou corrompido.");
            }
        }

        String detectedMime = detectMimeFromMagicBytes(header);
        String expectedMime = EXTENSION_TO_MIME.get(extension);

        if (!expectedMime.equals(detectedMime)) {
            log.warn("[UPLOAD] MIME type suspeito — extensão: .{} | esperado: {} | detectado: {}",
                    extension, expectedMime, detectedMime);
            throw new FileUploadException(
                    "O conteúdo do arquivo não corresponde à extensão informada. " +
                    "Tipo detectado: " + (detectedMime != null ? detectedMime : "desconhecido") + ".");
        }
    }

    /**
     * Detecta o MIME type real através dos magic bytes do arquivo.
     *
     * @param header primeiros 12 bytes do arquivo
     * @return MIME type detectado ou null se não reconhecido
     */
    private String detectMimeFromMagicBytes(byte[] header) {
        // JPEG: FF D8 FF
        if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
            return "image/jpeg";
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header.length >= 8
                && header[0] == (byte) 0x89 && header[1] == 0x50
                && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A
                && header[6] == 0x1A && header[7] == 0x0A) {
            return "image/png";
        }

        // WEBP: RIFF (bytes 0-3) + WEBP (bytes 8-11)
        if (header.length >= 12
                && header[0] == 0x52 && header[1] == 0x49  // RI
                && header[2] == 0x46 && header[3] == 0x46  // FF
                && header[8] == 0x57 && header[9] == 0x45  // WE
                && header[10] == 0x42 && header[11] == 0x50) { // BP
            return "image/webp";
        }

        return null; // não reconhecido
    }
}
