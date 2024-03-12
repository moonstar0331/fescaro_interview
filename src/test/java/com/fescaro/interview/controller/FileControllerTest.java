package com.fescaro.interview.controller;

import com.fescaro.interview.dto.FileDownloadDto;
import com.fescaro.interview.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@MockBean(JpaMetamodelMappingContext.class)
class FileControllerTest {

    private final MockMvc mvc;

    @MockBean
    private FileService fileService;

    public FileControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    /**
     * @Reference
     * 1. https://www.baeldung.com/spring-multipart-post-request-test
     * @throws Exception
     */
    @DisplayName("[POST] 파일 업로드 호출 - 정상 호출")
    @Test
    void 파일_업로드_정상호출() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.bin",
                MediaType.MULTIPART_FORM_DATA.toString(),
                "Hello, World!".getBytes()
        );

        // when & then
        mvc.perform(multipart("/api/upload")
                        .file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @DisplayName("[GET] 파일 다운로드 호출 (ORIGIN TYPE) - 정상 호출")
    @Test
    void 파일_다운로드_원본파일() throws Exception {
        // given
        String fileName = "test.bin";
        String testFilePath = "src/test/test_file_dir";

        FileDownloadDto fileDownloadDto = FileDownloadDto.builder()
                .fileName(UriUtils.encode("test.bin", StandardCharsets.UTF_8))
                .urlResource(new UrlResource("file:" + testFilePath))
                .build();

        // when
        when(fileService.fileDownload(fileName, "ORIGIN")).thenReturn(fileDownloadDto);

        // then
        mvc.perform(get("/api/download/" + fileName).param("type", "ORIGIN"))
                .andExpect(status().isOk());
    }

    @DisplayName("[GET] 파일 다운로드 호출 (ENC TYPE) - 정상 호출")
    @Test
    void 파일_다운로드_암호화된파일() throws Exception {
        // given
        String fileName = "test_enc.bin";
        String testFilePath = "src/test/test_file_dir";

        FileDownloadDto fileDownloadDto = FileDownloadDto.builder()
                .fileName(UriUtils.encode("test_enc.bin", StandardCharsets.UTF_8))
                .urlResource(new UrlResource("file:" + testFilePath))
                .build();

        // when
        when(fileService.fileDownload(fileName, "ENC")).thenReturn(fileDownloadDto);

        // then
        mvc.perform(get("/api/download/" + fileName).param("type", "ENC"))
                .andExpect(status().isOk());
    }
}