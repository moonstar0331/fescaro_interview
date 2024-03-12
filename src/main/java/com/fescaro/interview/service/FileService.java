package com.fescaro.interview.service;

import com.fescaro.interview.dto.EncryptionDto;
import com.fescaro.interview.dto.FileDownloadDto;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

public interface FileService {

    EncryptionDto uploadProcess(MultipartFile multipartFile) throws Exception;

    FileDownloadDto fileDownload(String fileName, String type) throws MalformedURLException;
}
