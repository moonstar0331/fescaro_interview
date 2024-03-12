package com.fescaro.interview.service;

import com.fescaro.interview.dto.FileDownloadDto;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;

public interface FileService {

    void fileUpload(MultipartFile multipartFile) throws Exception;

    FileDownloadDto fileDownload(Long id, String type) throws MalformedURLException;
}
