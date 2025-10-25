package com.jaestrada.multimedia.validators;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import org.primefaces.model.file.UploadedFile;

import java.util.Arrays;
import java.util.List;

@FacesValidator("fileTypeValidator")
public class FileTypeValidator implements Validator<UploadedFile> {
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png"
    );
    
    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList(
        "application/pdf"
    );
    
    private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final long MAX_PDF_SIZE = 5 * 1024 * 1024;   // 5 MB
    
    @Override
    public void validate(FacesContext context, UIComponent component, UploadedFile value) 
            throws ValidatorException {
        
        if (value != null && value.getSize() > 0) {
            String contentType = value.getContentType();
            long fileSize = value.getSize();
            
            // Obtener el tipo esperado del atributo del componente
            String expectedType = (String) component.getAttributes().get("expectedType");
            
            if ("poster".equals(expectedType)) {
                validatePosterFile(contentType, fileSize, value.getFileName());
            } else if ("technical".equals(expectedType)) {
                validateTechnicalFile(contentType, fileSize, value.getFileName());
            }
        }
    }
    
    private void validatePosterFile(String contentType, long fileSize, String fileName) 
            throws ValidatorException {
        
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Tipo de archivo inválido",
                "El poster debe ser una imagen JPG o PNG. Archivo: " + fileName
            );
            throw new ValidatorException(message);
        }
        
        if (fileSize > MAX_IMAGE_SIZE) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Archivo demasiado grande",
                "El poster no puede exceder 2 MB. Tamaño actual: " + formatFileSize(fileSize)
            );
            throw new ValidatorException(message);
        }
    }
    
    private void validateTechnicalFile(String contentType, long fileSize, String fileName) 
            throws ValidatorException {
        
        if (!ALLOWED_PDF_TYPES.contains(contentType)) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Tipo de archivo inválido",
                "La ficha técnica debe ser un archivo PDF. Archivo: " + fileName
            );
            throw new ValidatorException(message);
        }
        
        if (fileSize > MAX_PDF_SIZE) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Archivo demasiado grande",
                "La ficha técnica no puede exceder 5 MB. Tamaño actual: " + formatFileSize(fileSize)
            );
            throw new ValidatorException(message);
        }
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}