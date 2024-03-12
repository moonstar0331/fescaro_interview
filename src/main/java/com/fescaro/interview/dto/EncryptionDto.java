package com.fescaro.interview.dto;

import lombok.Builder;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.io.File;

@Getter
@Builder
public class EncryptionDto {

    private File targetFile;
    private File encryptedFile;
    private SecretKey secretKey;
    private byte[] iv;
}
