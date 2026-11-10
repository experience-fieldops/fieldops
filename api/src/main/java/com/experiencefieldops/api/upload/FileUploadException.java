package com.experiencefieldops.api.upload;

/**
 * Exceção lançada quando o arquivo enviado falha em qualquer validação de segurança.
 *
 * Usada pelo FileUploadService e capturada pelo FileUploadController
 * para retornar 400 Bad Request com mensagem descritiva.
 */
public class FileUploadException extends RuntimeException {

    public FileUploadException(String message) {
        super(message);
    }
}
