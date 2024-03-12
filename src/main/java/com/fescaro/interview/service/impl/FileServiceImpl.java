package com.fescaro.interview.service.impl;

import com.fescaro.interview.dto.FileDownloadDto;
import com.fescaro.interview.entity.FileEntity;
import com.fescaro.interview.repository.FileRepository;
import com.fescaro.interview.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final int IV_LENGTH = 16;
    private static final String UPLOADED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/uploaded_file";
    private static final String ENCRYPTED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/encrypted_file";

    private final FileRepository fileRepository;

    /**
     * 원본 파일 저장 + 암호화 + 암호화된 파일 저장을 수행한다.
     * @param multipartFile 원본 파일
     * @throws Exception
     * @Step
     * 1. 원본 파일 저장
     * 2. 파일 암호화
     * 3. 암호화된 파일 저장
     * 4. 파일 엔티티를 DB에 INSERT
     * @Reference
     * 1. CipherOutputStream: https://docs.oracle.com/javase/8/docs/api/javax/crypto/CipherOutputStream.html
     * 2. https://veneas.tistory.com/entry/JAVA-%EC%9E%90%EB%B0%94-AES-%EC%95%94%ED%98%B8%ED%99%94-%ED%95%98%EA%B8%B0-AES-128-AES-192-AES-256
     */
    @Override
    @Transactional
    public void fileUpload(MultipartFile multipartFile) throws Exception {

        // 원본 파일 저장 (MultipartFile -> File 변환)
        String originalFileName = multipartFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOADED_FILE_DIRECTORY_PATH + File.separator + originalFileName);
        Files.copy(multipartFile.getInputStream(), filePath);

        // Cipher 객체 인스턴스화
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // 비밀키는 임의의 16 바이트 값을 지정하여 사용
        String refKey = "aeskey1234567898";
        SecretKeySpec secretKey = new SecretKeySpec(refKey.getBytes("UTF-8"), "AES");

        // 임의의 16 바이트 IV 생성
        IvParameterSpec iv = generateIv();

        // Cipher 객체 초기화
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        try {
            InputStream input = multipartFile.getInputStream();

            String[] names = originalFileName.split("\\.");
            String encryptedFileSavePath = ENCRYPTED_FILE_DIRECTORY_PATH
                    + File.separator + names[0] + "_enc." + names[1];

            // 암호화된 파일 객체 및 OutputStream 생성
            File encryptedFile = new File(encryptedFileSavePath);
            FileOutputStream fos = new FileOutputStream(encryptedFile);

            // CipherOutputStream 은 OutputStream 과 Cipher 로 구성
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);

            /**
             * 파일 암호화 및 암호화된 파일 저장
             * CipherOutputStream 의 write()는 데이터를 쓰기 전에 먼저 데이터 암호화를 처리
             * GCM 에서 암호 해독과 함께 사용하는 것은 적합X
             */
            while (input.read() != -1) {
                cos.write(cipher.doFinal());
            }

            // 파일 엔티티 생성
            FileEntity fileEntity = FileEntity.builder()
                    .originFileName(originalFileName)
                    .originFilePath(filePath.toString())
                    .encryptedFileName(encryptedFile.getName())
                    .encryptedFilePath(encryptedFileSavePath)
                    .iv(toHexaString(iv.getIV()))
                    .build();

            // 파일 엔티티를 DB에 INSERT
            fileRepository.save(fileEntity);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 요청한 타입에 맞는 파일을 다운로드한다.
     * @param id   다운로드 하는 파일의 키 값
     * @param type 다운로드 하는 파일의 타입
     * @return 다운로드 하는 파일의 리소스 및 파일명을 가진 DTO 객체를 반환한다.
     * @throws MalformedURLException
     * @Step
     * 1. 파일 정보를 DB에서 가져옴
     * 2. 다운로드 하는 파일의 타입 비교
     * 3. 다운로드 하는 파일의 리소스 및 파일명 생성
     * @Reference
     * 1. https://yoons-development-space.tistory.com/87
     */
    @Override
    public FileDownloadDto fileDownload(Long id, String type) throws MalformedURLException {
        FileEntity fileEntity = fileRepository.findById(id).get();

        UrlResource resource = null;
        String fileName = null;

        // 파일 타입 비교
        if (type.equals("ORIGIN")) {
            resource = new UrlResource("file:" + fileEntity.getOriginFilePath());
            fileName = UriUtils.encode(fileEntity.getOriginFileName(), StandardCharsets.UTF_8);
        } else if (type.equals("ENC")) {
            resource = new UrlResource("file:" + fileEntity.getEncryptedFilePath());
            fileName = UriUtils.encode(fileEntity.getEncryptedFileName(), StandardCharsets.UTF_8);
        } else {
            throw new RuntimeException("파일의 타입을 지정 해주세요.");
        }

        return FileDownloadDto.builder()
                .urlResource(resource)
                .fileName(fileName)
                .build();
    }

    /**
     * 임의의 16 바이트 IV(Initialization Vector)를 생성한다.
     * @return 임의로 생성한 16 바이트 IV를 반환
     * @Reference
     * 1. http://www.java2s.com/example/java-utility-method/iv-generate/generateiv-77e61.html
     * 2. https://www.baeldung.com/java-encryption-iv
     */
    private static IvParameterSpec generateIv() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * 바이트 배열을 사람이 읽기 쉬운  16진수 문자열로 변환한다.
     * @param bytes 변환하고자 하는 바이트 배열
     * @return 변환된 16진수 문자열을 반환한다.
     */
    public static String toHexaString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
