package com.jaestrada.multimedia.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.jaestrada.multimedia.enums.FileType;
import com.jaestrada.multimedia.exceptions.MultimediaException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class AzureBlobStorageService {
    
    private static final Logger LOGGER = Logger.getLogger(AzureBlobStorageService.class.getName());
    
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;
    
    @PostConstruct
    public void init() {
        try {
            String connectionString = System.getProperty("AZURE_STORAGE_CONNECTION_STRING");
            String containerName = System.getProperty("AZURE_STORAGE_CONTAINER_NAME");
            
            if (connectionString == null || connectionString.isEmpty()) {
                throw new IllegalStateException("AZURE_STORAGE_CONNECTION_STRING no está configurado");
            }
            
            if (containerName == null || containerName.isEmpty()) {
                throw new IllegalStateException("AZURE_STORAGE_CONTAINER_NAME no está configurado");
            }
            
            this.blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
            
            this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
            
            // Crear el contenedor si no existe
            if (!containerClient.exists()) {
                containerClient.create();
                LOGGER.info("Contenedor creado: " + containerName);
            }
            
            LOGGER.info("Azure Blob Storage inicializado correctamente");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar Azure Blob Storage", e);
            throw new RuntimeException("No se pudo inicializar Azure Blob Storage", e);
        }
    }
    
    /**
     * Sube un archivo a Azure Blob Storage
     */
    public BlobUploadResult uploadFile(String titleName, FileType fileType, byte[] fileContent, 
                                     String contentType, String originalFileName) throws MultimediaException {
        try {
            String blobName = generateBlobName(titleName, fileType, originalFileName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            // Subir el archivo
            blobClient.upload(new ByteArrayInputStream(fileContent), fileContent.length, true);
            
            // Establecer el content type
            blobClient.setHttpHeaders(new com.azure.storage.blob.models.BlobHttpHeaders()
                    .setContentType(contentType));
            
            // Obtener propiedades del blob
            BlobProperties properties = blobClient.getProperties();
            
            BlobUploadResult result = new BlobUploadResult();
            result.setBlobUrl(blobClient.getBlobUrl());
            result.setEtag(properties.getETag());
            result.setContentType(contentType);
            result.setSizeBytes((long) fileContent.length);
            result.setBlobName(blobName);
            
            LOGGER.info("Archivo subido exitosamente a Azure Blob: " + blobName);
            return result;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al subir archivo a Azure Blob Storage", e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al subir archivo a Azure Blob Storage: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Genera una URL SAS temporal para descargar un archivo
     */
    public String generateSasUrl(String blobName, int hoursValid) throws MultimediaException {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (!blobClient.exists()) {
                throw new MultimediaException(
                    MultimediaException.Type.FILE_NOT_FOUND,
                    "El archivo no existe en Azure Blob Storage: " + blobName
                );
            }
            
            // Configurar permisos SAS
            BlobSasPermission sasPermission = new BlobSasPermission().setReadPermission(true);
            
            // Configurar tiempo de expiración
            OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(hoursValid);
            
            // Generar SAS
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission);
            
            String sasUrl = blobClient.generateSas(sasValues);
            return blobClient.getBlobUrl() + "?" + sasUrl;
            
        } catch (MultimediaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al generar SAS URL", e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al generar URL de descarga: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Elimina un archivo de Azure Blob Storage (eliminación lógica)
     */
    public void deleteFile(String blobName) throws MultimediaException {
        try {
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            
            if (blobClient.exists()) {
                // En lugar de eliminar físicamente, podríamos marcar como eliminado
                // Por ahora, eliminación física para simplificar
                blobClient.delete();
                LOGGER.info("Archivo eliminado de Azure Blob: " + blobName);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al eliminar archivo de Azure Blob Storage", e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al eliminar archivo: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Genera el nombre del blob basado en la estructura requerida
     */
    private String generateBlobName(String titleName, FileType fileType, String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String sanitizedTitleName = titleName.replaceAll("[^a-zA-Z0-9]", "_");
        
        String folder = (fileType == FileType.POSTER) ? "posters" : "fichas";
        String extension = getFileExtension(originalFileName);
        
        return String.format("%s/%s/%s%s", folder, sanitizedTitleName, timestamp, extension);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * Clase para encapsular el resultado de la subida
     */
    public static class BlobUploadResult {
        private String blobUrl;
        private String etag;
        private String contentType;
        private Long sizeBytes;
        private String blobName;
        
        // Getters y setters
        public String getBlobUrl() { return blobUrl; }
        public void setBlobUrl(String blobUrl) { this.blobUrl = blobUrl; }
        
        public String getEtag() { return etag; }
        public void setEtag(String etag) { this.etag = etag; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public Long getSizeBytes() { return sizeBytes; }
        public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
        
        public String getBlobName() { return blobName; }
        public void setBlobName(String blobName) { this.blobName = blobName; }
    }
}