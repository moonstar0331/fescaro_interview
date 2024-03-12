package com.fescaro.interview.dto;

import com.fescaro.interview.entity.FileEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FileDto {
    private String originFileName;
    private String originFilePath;
    private String encryptedFileName;
    private String encryptedFilePath;
    private String iv;
    private LocalDateTime createdAt;

    public static FileDto from(FileEntity entity) {
        return FileDto.builder()
                .originFileName(entity.getOriginFileName())
                .originFilePath(entity.getOriginFilePath())
                .encryptedFileName(entity.getEncryptedFileName())
                .encryptedFilePath(entity.getEncryptedFilePath())
                .iv(entity.getIv())
//                .ivBytes(entity.getIvBytes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
