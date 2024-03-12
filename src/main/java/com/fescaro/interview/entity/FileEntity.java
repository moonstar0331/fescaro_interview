package com.fescaro.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "file_entity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity extends AuditingFields {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "origin_file_name", unique = true)
    private String originFileName;

    @Column(name = "origin_file_path", unique = true)
    private String originFilePath;

    @Column(name = "encrypted_file_name", unique = true)
    private String encryptedFileName;

    @Column(name = "encrypted_file_path", unique = true)
    private String encryptedFilePath;

    @Column(name = "iv")
    private String iv;
}
