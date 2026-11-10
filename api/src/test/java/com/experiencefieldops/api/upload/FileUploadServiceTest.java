package com.experiencefieldops.api.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileUploadServiceTest {

    @TempDir
    Path uploadDirectory;

    private FileUploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new FileUploadService();
        ReflectionTestUtils.setField(uploadService, "uploadDirectory", uploadDirectory.toString());
    }

    @Test
    void acceptsSupportedImageExtensionsAndStoresUsingGeneratedNames() throws IOException {
        Map<String, byte[]> imageHeaders = Map.of(
                "jpg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                "jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                "png", new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0},
                "webp", new byte[] {0x52, 0x49, 0x46, 0x46, 0, 0, 0, 0, 0x57, 0x45, 0x42, 0x50});

        for (Map.Entry<String, byte[]> image : imageHeaders.entrySet()) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "client-name." + image.getKey(), "application/octet-stream", image.getValue());

            String savedFilename = uploadService.store(file);

            assertThat(savedFilename).matches(
                    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\."
                            + image.getKey());
            assertThat(savedFilename).doesNotContain("client-name");
            assertThat(Files.exists(uploadDirectory.resolve(savedFilename))).isTrue();
        }
    }

    @Test
    void rejectsUnsupportedExtensionsAndContentThatDoesNotMatchTheExtension() {
        MockMultipartFile unsupported = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[] {1, 2, 3, 4});
        assertThatThrownBy(() -> uploadService.store(unsupported))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Extensão não permitida");

        MockMultipartFile mismatched = new MockMultipartFile(
                "file", "image.png", "image/png", "<html>not an image</html>".getBytes());
        assertThatThrownBy(() -> uploadService.store(mismatched))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("não corresponde à extensão");
    }

    @Test
    void rejectsFilesLargerThanTenMegabytes() {
        byte[] content = new byte[10 * 1024 * 1024 + 1];
        content[0] = (byte) 0xFF;
        content[1] = (byte) 0xD8;
        content[2] = (byte) 0xFF;
        MockMultipartFile oversized = new MockMultipartFile(
                "file", "large.jpg", "image/jpeg", content);

        assertThatThrownBy(() -> uploadService.store(oversized))
                .isInstanceOf(FileUploadException.class)
                .hasMessageContaining("Tamanho máximo: 10 MB");
    }
}
