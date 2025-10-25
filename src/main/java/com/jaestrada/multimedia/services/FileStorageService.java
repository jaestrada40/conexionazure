package com.jaestrada.multimedia.services;

import com.jaestrada.multimedia.enums.FileType;
import com.jaestrada.multimedia.exceptions.MultimediaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.primefaces.model.file.UploadedFile;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FileStorageService {
    
    private static final Logger LOGGER = Logger.getLogger(FileStorageService.class.getName());
    
    @Inject
    private AzureBlobStorageService azureBlobStorageService;
    
    // Límites de tamaño de archivo
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final long MAX_PDF_SIZE = 5 * 1024 * 1024;   // 5 MB
    
    // Tipos de archivo permitidos
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png"
    );
    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList(
        "application/pdf"
    );
    
    /**
     * Guarda un archivo en Azure Blob Storage
     */
    public AzureBlobStorageService.BlobUploadResult saveFile(UploadedFile file, FileType fileType, String titleName) throws MultimediaException {
        validateFile(file, fileType);
        
        try {
            // Convertir archivo a bytes
            byte[] fileContent = file.getContent();
            
            // Subir a Azure Blob Storage
            AzureBlobStorageService.BlobUploadResult result = azureBlobStorageService.uploadFile(
                titleName, 
                fileType, 
                fileContent, 
                file.getContentType(), 
                file.getFileName()
            );
            
            LOGGER.info("Archivo guardado exitosamente en Azure Blob: " + result.getBlobName());
            return result;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al procesar archivo: " + file.getFileName(), e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al procesar el archivo: " + e.getMessage(),
                e
            );
        }
    }
    
    /**
     * Elimina un archivo de Azure Blob Storage
     */
    public void deleteFile(String blobName) throws MultimediaException {
        if (blobName != null && !blobName.isEmpty()) {
            azureBlobStorageService.deleteFile(blobName);
            LOGGER.info("Archivo eliminado de Azure Blob: " + blobName);
        } else {
            LOGGER.warning("Nombre de blob vacío para eliminar");
        }
    }
    
    /**
     * Genera una URL SAS temporal para descargar el archivo desde Azure Blob Storage
     */
    public String generateDownloadUrl(String blobName) throws MultimediaException {
        if (blobName == null || blobName.isEmpty()) {
            throw new MultimediaException(
                MultimediaException.Type.FILE_NOT_FOUND,
                "Nombre de blob no válido"
            );
        }
        
        // Generar URL SAS válida por 1 hora
        return azureBlobStorageService.generateSasUrl(blobName, 1);
    }
    
    /**
     * Valida el archivo según el tipo y restricciones
     */
    private void validateFile(UploadedFile file, FileType fileType) throws MultimediaException {
        if (file == null || file.getSize() == 0) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El archivo está vacío o no es válido"
            );
        }
        
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        
        switch (fileType) {
            case POSTER:
                if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                    throw new MultimediaException(
                        MultimediaException.Type.INVALID_FILE_TYPE,
                        "Tipo de archivo no válido para poster. Se permiten: JPG, PNG"
                    );
                }
                if (fileSize > MAX_IMAGE_SIZE) {
                    throw new MultimediaException(
                        MultimediaException.Type.FILE_TOO_LARGE,
                        "El archivo de imagen es demasiado grande. Máximo permitido: 2 MB"
                    );
                }
                break;
                
            case TECHNICAL_SHEET:
                if (!ALLOWED_PDF_TYPES.contains(contentType)) {
                    throw new MultimediaException(
                        MultimediaException.Type.INVALID_FILE_TYPE,
                        "Tipo de archivo no válido para ficha técnica. Se permiten: PDF"
                    );
                }
                if (fileSize > MAX_PDF_SIZE) {
                    throw new MultimediaException(
                        MultimediaException.Type.FILE_TOO_LARGE,
                        "El archivo PDF es demasiado grande. Máximo permitido: 5 MB"
                    );
                }
                break;
        }
    }
    
    /**
     * Extrae el nombre del blob de una URL de Azure Blob Storage
     */
    public String extractBlobNameFromUrl(String blobUrl) {
        if (blobUrl == null || blobUrl.isEmpty()) {
            return null;
        }
        
        try {
            // Formato típico: https://storageaccount.blob.core.windows.net/container/blobname
            String[] parts = blobUrl.split("/");
            if (parts.length >= 2) {
                // Los últimos dos elementos son el contenedor y el blob name
                return parts[parts.length - 2] + "/" + parts[parts.length - 1];
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al extraer nombre de blob de URL: " + blobUrl, e);
        }
        
        return null;
    }
}