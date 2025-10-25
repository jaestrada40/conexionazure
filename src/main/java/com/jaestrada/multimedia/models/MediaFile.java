package com.jaestrada.multimedia.models;

import com.jaestrada.multimedia.enums.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_files")
public class MediaFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_file_id")
    private Long id;

    @NotNull(message = "El título multimedia es obligatorio")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "media_title_id", nullable = false)
    private MediaTitle mediaTitle;

    @NotNull(message = "El tipo de archivo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @NotNull(message = "La URL local es obligatoria")
    @Size(max = 500, message = "La URL local no puede exceder 500 caracteres")
    @Column(name = "local_url", nullable = false, length = 500)
    private String localUrl;

    @Size(max = 500, message = "La URL del blob no puede exceder 500 caracteres")
    @Column(name = "blob_url", length = 500)
    private String blobUrl;

    @Size(max = 100, message = "El etag no puede exceder 100 caracteres")
    @Column(name = "etag", length = 100)
    private String etag;

    @Size(max = 50, message = "El tipo de contenido no puede exceder 50 caracteres")
    @Column(name = "content_type", length = 50)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Size(max = 50, message = "El usuario que subió el archivo no puede exceder 50 caracteres")
    @Column(name = "uploaded_by", length = 50)
    private String uploadedBy;

    @PrePersist
    public void prePersist() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Constructors
    public MediaFile() {}

    public MediaFile(MediaTitle mediaTitle, FileType fileType, String localUrl, String contentType, Long sizeBytes) {
        this.mediaTitle = mediaTitle;
        this.fileType = fileType;
        this.localUrl = localUrl;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MediaTitle getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(MediaTitle mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public String getBlobUrl() {
        return blobUrl;
    }

    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "id=" + id +
                ", fileType=" + fileType +
                ", localUrl='" + localUrl + '\'' +
                ", contentType='" + contentType + '\'' +
                ", sizeBytes=" + sizeBytes +
                ", uploadedAt=" + uploadedAt +
                ", uploadedBy='" + uploadedBy + '\'' +
                '}';
    }
}