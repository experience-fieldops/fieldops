package com.experiencefieldops.api.upload;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Endpoint de upload seguro de imagens (issue #79).
 *
 * POST /api/v1/upload/image
 *   Parâmetro (multipart/form-data):
 *     file → arquivo de imagem
 *
 * Respostas:
 *   200 OK           → { "filename": "<uuid>.<ext>" }
 *   400 Bad Request  → { "error": "<motivo da rejeição>" }
 *   500 Internal     → { "error": "Erro interno ao salvar o arquivo." }
 *
 * Todas as validações de segurança são executadas pelo FileUploadService:
 *   ✓ Extensão permitida (jpg, jpeg, png, webp)
 *   ✓ MIME type real via magic bytes
 *   ✓ Tamanho máximo (10 MB)
 *   ✓ Renomeio com UUID
 *   ✓ Armazenamento fora do diretório público web
 */
@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    private final FileUploadService uploadService;

    public FileUploadController(FileUploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Recebe e armazena uma imagem de forma segura.
     *
     * @param file arquivo enviado via multipart/form-data com o campo "file"
     * @return nome gerado pelo servidor (nunca o nome original do cliente)
     */
    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {
            String savedFilename = uploadService.store(file);
            return ResponseEntity.ok(Map.of("filename", savedFilename));

        } catch (FileUploadException e) {
            // Erros de validação: extensão inválida, MIME suspeito, tamanho excedido, etc.
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IOException e) {
            // Erros de I/O ao salvar no disco
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erro interno ao salvar o arquivo."));
        }
    }
}
