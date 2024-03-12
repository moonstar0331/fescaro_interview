package com.fescaro.interview.service;

import com.fescaro.interview.dto.EncryptionDto;
import com.fescaro.interview.entity.FileEntity;
import com.fescaro.interview.repository.FileRepository;
import com.fescaro.interview.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    private static final String UPLOADED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/uploaded_file";
    private static final String ENCRYPTED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/encrypted_file";
    private static final String CHECK_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/check_dir";

    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private FileRepository fileRepository;

    @BeforeEach
    void init() {
        Path originFilePath = Paths.get(UPLOADED_FILE_DIRECTORY_PATH + "/file.bin");
        Path encryptedFilePath = Paths.get(ENCRYPTED_FILE_DIRECTORY_PATH + "/file_enc.bin");
        Path checkFilePath = Paths.get(CHECK_FILE_DIRECTORY_PATH + "/file_check.bin");

        try {
            if (Files.exists(originFilePath)) {
                Files.delete(originFilePath);
            }
            if (Files.exists(encryptedFilePath)) {
                Files.delete(encryptedFilePath);
            }
            if (Files.exists(checkFilePath)) {
                Files.delete(checkFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("파일을 업로드하면 암호화 대상 파일과 암호화 된 파일이 저장된다.")
    @Test
    void 파일_업로드시_암호화_대상파일_저장_정상수행() throws Exception {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.bin",
                MediaType.MULTIPART_FORM_DATA.toString(),
                "Hello, World!".getBytes()
        );

        FileEntity fileEntity = createFileEntity();
        given(fileRepository.save(any(FileEntity.class))).willReturn(fileEntity);

        // when
        fileService.uploadProcess(multipartFile);

        String originFilePath = UPLOADED_FILE_DIRECTORY_PATH + "/file.bin";
        File originFile = new File(originFilePath);
        String encryptedFilePath = ENCRYPTED_FILE_DIRECTORY_PATH + "/file_enc.bin";
        File encryptedFile = new File(encryptedFilePath);

        // then
        assertThat(originFile).exists().isFile();
        assertThat(encryptedFile).exists().isFile();
        then(fileRepository).should().save(any(FileEntity.class));
    }

    @DisplayName("파일을 업로드하면 파일 엔티티를 저장한다.")
    @Test
    void 파일_업로드시_파일엔티티_저장_정상수행() throws Exception {
        // given
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.bin",
                MediaType.MULTIPART_FORM_DATA.toString(),
                "Hello, World!".getBytes()
        );

        FileEntity fileEntity = createFileEntity();
        given(fileRepository.save(any(FileEntity.class))).willReturn(fileEntity);

        // when
        fileService.uploadProcess(multipartFile);

        // then
        then(fileRepository).should().save(any(FileEntity.class));
    }

    @DisplayName("암호화 된 파일을 복호화를 하면 암호화 대상 파일과 같은 내용이다.")
    @Test
    void 파일_복호화시_원본과_같은_내용() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "file.bin",
                MediaType.MULTIPART_FORM_DATA.toString(),
                "Hello, World!".getBytes()
        );

        FileEntity fileEntity = createFileEntity();
        given(fileRepository.save(any(FileEntity.class))).willReturn(fileEntity);

        // when
        EncryptionDto encryptionDto = fileService.uploadProcess(multipartFile);

        File encryptedFile = new File(encryptionDto.getEncryptedFile().getAbsoluteFile().toString());

        String checkFilePath = CHECK_FILE_DIRECTORY_PATH + "/file_check.bin";
        File checkFile = new File(checkFilePath);

        InputStream input = new BufferedInputStream(new FileInputStream(encryptedFile));
        FileOutputStream fos = new FileOutputStream(checkFile);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKey secretKey = FileServiceImpl.generateKey(FileServiceImpl.KEY);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(encryptionDto.getIv()));

        byte[] buffer = new byte[64];
        int read;
        while ((read = input.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, read);
            if (output != null) {
                fos.write(output);
            }
        }
        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            fos.write(finalBytes);
        }

        // then
        assertThat(checkFile).content().isEqualTo("Hello, World!");
    }

    private FileEntity createFileEntity() {
        byte[] iv = new byte[] {
                (byte) 0x01, (byte) 0xAB, (byte) 0x3C, (byte) 0xFF,
                (byte) 0x45, (byte) 0x78, (byte) 0x9A, (byte) 0xCD,
                (byte) 0xEF, (byte) 0x20, (byte) 0x11, (byte) 0x22,
                (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66
        };

        return FileEntity.builder()
                .id(1L)
                .originFileName("file.bin")
                .originFilePath(UPLOADED_FILE_DIRECTORY_PATH + "/file.bin")
                .encryptedFileName("file_enc.bin")
                .encryptedFilePath(ENCRYPTED_FILE_DIRECTORY_PATH + "/file_enc.bin")
                .iv(FileServiceImpl.toHexaString(iv))
                .build();
    }
}