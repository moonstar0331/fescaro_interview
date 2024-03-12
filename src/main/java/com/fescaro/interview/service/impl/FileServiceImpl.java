package com.fescaro.interview.service.impl;

import com.fescaro.interview.dto.EncryptionDto;
import com.fescaro.interview.dto.FileDownloadDto;
import com.fescaro.interview.entity.FileEntity;
import com.fescaro.interview.repository.FileRepository;
import com.fescaro.interview.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    public static final String KEY = "aeskey1234567898";
    private static final int IV_LENGTH = 16;
    private static final String UPLOADED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/uploaded_file";
    private static final String ENCRYPTED_FILE_DIRECTORY_PATH = "/Users/moonsung/coding/fescaro/interview/encrypted_file";

    private final FileRepository fileRepository;

    /**
     * 원본 파일 저장 + 암호화 + 암호화된 파일 저장을 수행한다.
     * @param multipartFile 업로드한 원본 파일
     * @return EncryptionDto AES 암호화/복호화에 필요한 내용들을 담은 DTO 객체를 반환한다.
     * @throws Exception
     * @Step
     * 1.
     * @Reference
     * 1. https://veneas.tistory.com/entry/JAVA-%EC%9E%90%EB%B0%94-AES-%EC%95%94%ED%98%B8%ED%99%94-%ED%95%98%EA%B8%B0-AES-128-AES-192-AES-256
     * 2. 1. https://www.baeldung.com/java-aes-encryption-decryption
     * 3. https://gist.github.com/psqq/091ae0a4fed8a561580469b958249b49
     */
    @Override
    public EncryptionDto uploadProcess(MultipartFile multipartFile) throws Exception {

        // 랜덤한 초기화 벡터(IV) 생성
        byte[] iv = generateIv();
        // 비밀키는 임의의 16 바이트 값을 지정하여 사용
        SecretKey secretKey = generateKey(KEY);

        // 원본 파일 저장 (MultipartFile -> File 변환)
        String originalFileName = multipartFile.getOriginalFilename();
        Path filePath = Paths.get(UPLOADED_FILE_DIRECTORY_PATH + File.separator + originalFileName);
        Files.copy(multipartFile.getInputStream(), filePath);
        File targetFile = new File(filePath.toString());

        // 암호화 결과 출력 파일 생성
        String[] names = originalFileName.split("\\.");
        String encryptedFilePath = ENCRYPTED_FILE_DIRECTORY_PATH + File.separator + names[0] + "_enc." + names[1];
        File encryptedFile = new File(encryptedFilePath);

        // 파일 암호화 수행
        encryptFile(targetFile, encryptedFile, secretKey, iv);

        // 파일 정보를 담은 엔티티 객체 생성 및 DB insert
        FileEntity fileEntity = FileEntity.builder()
                .originFileName(originalFileName)
                .originFilePath(targetFile.getAbsolutePath())
                .encryptedFileName(encryptedFile.getName())
                .encryptedFilePath(encryptedFile.getAbsolutePath())
                .build();
        fileRepository.save(fileEntity);

        return EncryptionDto.builder()
                .targetFile(targetFile)
                .encryptedFile(encryptedFile)
                .secretKey(secretKey)
                .iv(iv)
                .build();
    }

    /**
     * 요청한 타입에 맞는 파일을 다운로드한다.
     * @param fileName 다운로드 하는 파일명
     * @param type 다운로드 하는 파일의 타입
     * @return  다운로드 하는 파일의 리소스 및 파일명을 가진 DTO 객체를 반환한다.
     * @throws MalformedURLException
     * @Step
     * 1. 다운로드 하는 파일의 타입 비교
     * 2. 파일 타입에 맞는 파일 명을 기준으로 파일 정보를 DB에서 가져옴
     * 3. 다운로드 하는 파일의 리소스 및 파일명 정보를 담은 DTO 객체 생성
     * @Reference
     * 1. https://yoons-development-space.tistory.com/87
     */
    @Override
    public FileDownloadDto fileDownload(String fileName, String type) throws MalformedURLException {
        
        FileEntity fileEntity = null;
        UrlResource resource = null;
        String downloadFileName = null;

        // 파일 타입 비교
        if (type.equals("ORIGIN")) {
            fileEntity = fileRepository.findByOriginFileName(fileName).get();
            System.out.println("origin: " + fileEntity.getOriginFileName());
            resource = new UrlResource("file:" + fileEntity.getOriginFilePath());
            downloadFileName = UriUtils.encode(fileEntity.getOriginFileName(), StandardCharsets.UTF_8);
        } else if (type.equals("ENC")) {
            fileEntity = fileRepository.findByEncryptedFileName(fileName).get();
            System.out.println("enc: " + fileEntity.getEncryptedFileName());
            resource = new UrlResource("file:" + fileEntity.getEncryptedFilePath());
            downloadFileName = UriUtils.encode(fileEntity.getEncryptedFileName(), StandardCharsets.UTF_8);
        } else {
            throw new RuntimeException("올바른 파일의 타입을 지정 해주세요.");
        }

        return FileDownloadDto.builder()
                .urlResource(resource)
                .fileName(downloadFileName)
                .build();
    }

    /**
     * 암호화 대상을 암호화하고 이를 암호화된 파일에 작성한다.
     * @param targetFile 암호화 대상 파일
     * @param encryptedFile 암호화 된 파일
     * @param secretKey AES128 키
     * @param iv AES IV(Initialization Vector)
     * @throws Exception
     * @Step
     * 1. Cipher 객체 생성 및 초기화
     * 2. 대상 파일 입력 스트림 및 암호화 된 파일 출력 스트림 생성
     * 3. 마지막으로 Padding 을 추가
     * @Reference
     * 1. https://huisam.tistory.com/entry/aes
     * 2. https://ohdaldal.tistory.com/24
     * 3. https://gist.github.com/psqq/091ae0a4fed8a561580469b958249b49
     */
    private static void encryptFile(File targetFile, File encryptedFile, SecretKey secretKey, byte[] iv) throws Exception {

        // Cipher 객체 인스턴스화 및 초기화
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        // 대상 파일 및 암호화 된 파일 입출력 스트림 생성
        try (FileInputStream input = new FileInputStream(targetFile);
             FileOutputStream fos = new FileOutputStream(encryptedFile)) {

            byte[] buffer = new byte[64];
            int read;
            while ((read = input.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, read);
                if (output != null) {
                    fos.write(output);
                }
            }

            // 마지막 패딩을 추가하여 암호화 작업 마무리
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) {
                fos.write(finalBytes);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 임의의 16 바이트 IV(Initialization Vector)를 생성한다.
     * @return 임의로 생성한 16 바이트 IV를 반환
     * @Reference
     * 1. http://www.java2s.com/example/java-utility-method/iv-generate/generateiv-77e61.html
     * 2. https://www.baeldung.com/java-encryption-iv
     */
    public static byte[] generateIv() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);
        return iv;
    }

    /**
     * 임의로 지정된 16bytes 값으로 암호화/복호화에 필요한 비밀키를 만든다.
     * @param key 임의로 지정된 16bytes 값
     * @return 암호화/복호화에 필요한 비밀키
     * @Reference
     * 1. https://www.baeldung.com/java-aes-encryption-decryption
     * 2. https://docs.oracle.com/javase/8/docs/api/javax/crypto/spec/SecretKeySpec.html
     */
    public static SecretKey generateKey(String key) {
        return new SecretKeySpec(key.getBytes(), "AES");
    }

    /**
     * 바이트 배열을 데이터베이스에 저장하기 간편한 문자열 형식으로 변경한다.
     * 바이트 배열을 사람이 읽기 쉬운  16진수 문자열로 변환한다.
     * @param bytes 변환하고자 하는 바이트 배열
     * @return 변환된 16진수 문자열을 반환한다.
     * @Reference
     * 1. https://stackoverflow.com/questions/9655181/java-convert-a-byte-array-to-a-hex-string
     * 2. https://www.geeksforgeeks.org/java-program-to-convert-byte-array-to-hex-string/
     */
    public static String toHexaString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

}
