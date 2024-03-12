package com.fescaro.interview.repository;

import com.fescaro.interview.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    Optional<FileEntity> findByOriginFileName(String fileName);
    Optional<FileEntity> findByEncryptedFileName(String fileName);

}
