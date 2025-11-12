package com.jaestrada.multimedia.controllers;

import com.jaestrada.multimedia.enums.FileType;
import com.jaestrada.multimedia.enums.TitleType;
import com.jaestrada.multimedia.exceptions.MultimediaException;
import com.jaestrada.multimedia.models.MediaFile;
import com.jaestrada.multimedia.models.MediaTitle;
import com.jaestrada.multimedia.models.MovieGenre;
import com.jaestrada.multimedia.services.MultimediaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.servlet.http.Part;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class MultimediaBean implements Serializable {
    
    private static final Logger LOGGER = Logger.getLogger(MultimediaBean.class.getName());
    
    @Inject
    private MultimediaService multimediaService;
    
    @Inject
    private Validator validator;
    
    // UI State
    private boolean dialogVisible;
    private boolean genreDialogVisible;
    private MediaTitle selectedTitle;
    private MovieGenre selectedGenre;
    private List<MovieGenre> selectedGenres;
    
    // File upload usando Jakarta Servlet Part
    private Part uploadedPosterFile;
    private UploadedFile technicalFile;
    
    @PostConstruct
    public void init() {
        selectedTitle = new MediaTitle();
        selectedGenre = new MovieGenre();
        selectedGenres = new ArrayList<>();
        dialogVisible = false;
        genreDialogVisible = false;
    }
    
    // ==================== Title Management ====================
    
    public void newTitle() {
        clearFacesMessages();
        this.selectedTitle = new MediaTitle();
        this.selectedGenres = new ArrayList<>();
        this.uploadedPosterFile = null;
        this.technicalFile = null;
        this.dialogVisible = true;
    }
    
    public String save() {
        clearFacesMessages();
        
        try {
            // Validar entidad
            Set<ConstraintViolation<MediaTitle>> violations = validator.validate(selectedTitle);
            if (!violations.isEmpty()) {
                for (ConstraintViolation<MediaTitle> violation : violations) {
                    String field = violation.getPropertyPath().toString();
                    String message = violation.getMessage();
                    String label = getFieldLabel(field);
                    
                    FacesContext.getCurrentInstance().addMessage("frmMultimedia:msgMultimedia",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    label + ": " + message, null));
                }
                FacesContext.getCurrentInstance().validationFailed();
                return null;
            }
            
            // Asignar géneros seleccionados
            selectedTitle.setGenres(new ArrayList<>(selectedGenres));
            
            // Guardar título primero
            multimediaService.saveMediaTitle(selectedTitle);
            LOGGER.info("Título guardado con ID: " + selectedTitle.getId());
            
            // Debug: verificar estado del archivo
            LOGGER.info("🔍 uploadedPosterFile es null? " + (uploadedPosterFile == null));
            if (uploadedPosterFile != null) {
                LOGGER.info("🔍 uploadedPosterFile.getSize(): " + uploadedPosterFile.getSize());
                LOGGER.info("🔍 uploadedPosterFile.getSubmittedFileName(): " + uploadedPosterFile.getSubmittedFileName());
            }
            
            // Subir archivos si están presentes
            boolean fileUploaded = false;
            try {
                if (uploadedPosterFile != null && uploadedPosterFile.getSize() > 0) {
                    LOGGER.info("Subiendo poster de tamaño: " + uploadedPosterFile.getSize() + " bytes");
                    
                    // Convertir Part a UploadedFile
                    UploadedFile posterFile = new PartUploadedFile(uploadedPosterFile);
                    
                    multimediaService.uploadFile(selectedTitle, posterFile, FileType.POSTER, getCurrentUser());
                    fileUploaded = true;
                    LOGGER.info("Poster subido exitosamente");
                }
                
                if (technicalFile != null && technicalFile.getSize() > 0) {
                    LOGGER.info("Subiendo ficha técnica de tamaño: " + technicalFile.getSize() + " bytes");
                    multimediaService.uploadFile(selectedTitle, technicalFile, FileType.TECHNICAL_SHEET, getCurrentUser());
                    fileUploaded = true;
                    LOGGER.info("Ficha técnica subida exitosamente");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error al subir archivos", e);
                addErrorMessage("Título guardado pero error al subir imagen: " + e.getMessage());
                this.dialogVisible = false;
                return null;
            }
            
            // Cerrar diálogo y limpiar
            this.dialogVisible = false;
            this.selectedTitle = new MediaTitle();
            this.selectedGenres = new ArrayList<>();
            this.uploadedPosterFile = null;
            this.technicalFile = null;
            
            // Mensaje único
            if (fileUploaded) {
                addInfoMessage("Título guardado con imagen");
            } else {
                addInfoMessage("Título guardado");
            }
            
            return null;
            
        } catch (MultimediaException e) {
            addErrorMessage("Error al guardar: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al guardar título multimedia", e);
            return null;
        } catch (Exception e) {
            addErrorMessage("Error inesperado al guardar el título");
            LOGGER.log(Level.SEVERE, "Error inesperado al guardar título multimedia", e);
            return null;
        }
    }
    
    public void edit(MediaTitle title) {
        clearFacesMessages();
        this.selectedTitle = title;
        this.selectedGenres = new ArrayList<>(title.getGenres());
        this.uploadedPosterFile = null;
        this.technicalFile = null;
        this.dialogVisible = true;
    }
    
    public void delete(MediaTitle title) {
        try {
            multimediaService.deleteMediaTitle(title.getId());
            addInfoMessage("Título multimedia eliminado exitosamente");
        } catch (MultimediaException e) {
            addErrorMessage("Error al eliminar: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al eliminar título multimedia", e);
        } catch (Exception e) {
            addErrorMessage("Error inesperado al eliminar el título");
            LOGGER.log(Level.SEVERE, "Error inesperado al eliminar título multimedia", e);
        }
    }
    
    // ==================== Genre Management ====================
    
    public void newGenre() {
        clearFacesMessages();
        this.selectedGenre = new MovieGenre();
        this.genreDialogVisible = true;
    }
    
    public void saveGenre() {
        clearFacesMessages();
        
        try {
            // Validar entidad
            Set<ConstraintViolation<MovieGenre>> violations = validator.validate(selectedGenre);
            if (!violations.isEmpty()) {
                for (ConstraintViolation<MovieGenre> violation : violations) {
                    String field = violation.getPropertyPath().toString();
                    String message = violation.getMessage();
                    String label = getGenreFieldLabel(field);
                    
                    FacesContext.getCurrentInstance().addMessage("frmGenre:msgGenre",
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    label + ": " + message, null));
                }
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }
            
            multimediaService.saveGenre(selectedGenre);
            this.genreDialogVisible = false;
            addInfoMessage("Género guardado exitosamente");
            this.selectedGenre = new MovieGenre();
            
        } catch (MultimediaException e) {
            addErrorMessage("Error al guardar género: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al guardar género", e);
        } catch (Exception e) {
            addErrorMessage("Error inesperado al guardar el género");
            LOGGER.log(Level.SEVERE, "Error inesperado al guardar género", e);
        }
    }
    
    public void editGenre(MovieGenre genre) {
        clearFacesMessages();
        this.selectedGenre = genre;
        this.genreDialogVisible = true;
    }
    
    public void deleteGenre(MovieGenre genre) {
        try {
            multimediaService.deleteGenre(genre.getId());
            addInfoMessage("Género eliminado exitosamente");
        } catch (MultimediaException e) {
            addErrorMessage("Error al eliminar género: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al eliminar género", e);
        } catch (Exception e) {
            addErrorMessage("Error inesperado al eliminar el género");
            LOGGER.log(Level.SEVERE, "Error inesperado al eliminar género", e);
        }
    }
    
    // ==================== File Upload Handling ====================
    
    public void handleTechnicalUpload() {
        if (technicalFile != null && technicalFile.getSize() > 0 && selectedTitle.getId() != null) {
            try {
                multimediaService.uploadFile(selectedTitle, technicalFile, FileType.TECHNICAL_SHEET, getCurrentUser());
                addInfoMessage("Ficha técnica subida exitosamente");
                technicalFile = null;
            } catch (MultimediaException e) {
                addErrorMessage("Error al subir ficha técnica: " + e.getMessage());
                LOGGER.log(Level.WARNING, "Error al subir ficha técnica", e);
            }
        }
    }
    

    
    public void deleteFile(MediaFile file) {
        try {
            multimediaService.deleteFile(file.getId());
            addInfoMessage("Archivo eliminado exitosamente");
        } catch (MultimediaException e) {
            addErrorMessage("Error al eliminar archivo: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al eliminar archivo", e);
        }
    }
    
    // ==================== Getters for UI ====================
    
    public List<MediaTitle> getTitles() {
        try {
            return multimediaService.getAllTitles();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener títulos", e);
            return new ArrayList<>();
        }
    }
    
    public List<MovieGenre> getGenres() {
        try {
            return multimediaService.getAllGenres();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener géneros", e);
            return new ArrayList<>();
        }
    }
    
    public TitleType[] getTitleTypes() {
        return TitleType.values();
    }
    
    public MediaFile getPosterForTitle(MediaTitle title) {
        try {
            return multimediaService.getPosterForTitle(title.getId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener poster para título: " + title.getId(), e);
            return null;
        }
    }
    
    public List<MediaFile> getTechnicalSheetsForTitle(MediaTitle title) {
        try {
            return multimediaService.getTechnicalSheetsForTitle(title.getId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener fichas técnicas para título: " + title.getId(), e);
            return new ArrayList<>();
        }
    }
    
    public int getTitleCountForGenre(MovieGenre genre) {
        try {
            return multimediaService.getTitleCountForGenre(genre.getId());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener conteo de títulos para género: " + genre.getId(), e);
            return 0;
        }
    }
    
    // ==================== Test Methods ====================
    
    public String testAzure() {
        try {
            LOGGER.info("=== INICIANDO PRUEBA DE AZURE DESDE BEAN ===");
            
            // Crear un título de prueba
            MediaTitle testTitle = new MediaTitle();
            testTitle.setTitleName("PRUEBA AZURE " + System.currentTimeMillis());
            testTitle.setTitleType(TitleType.MOVIE);
            testTitle.setReleaseYear(2024);
            
            // Asignar un género existente
            List<MovieGenre> genres = multimediaService.getAllGenres();
            if (!genres.isEmpty()) {
                testTitle.setGenres(List.of(genres.get(0)));
            } else {
                addErrorMessage("No hay géneros disponibles. Crea uno primero.");
                return null;
            }
            
            // Guardar el título
            multimediaService.saveMediaTitle(testTitle);
            LOGGER.info("Título de prueba creado con ID: " + testTitle.getId());
            
            // Crear una imagen PNG ficticia (1x1 pixel transparente)
            byte[] testData = new byte[] {
                (byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
                0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte)0xC4,
                (byte)0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41, 0x54,
                0x78, (byte)0x9C, 0x63, 0x00, 0x01, 0x00, 0x00, 0x05,
                0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte)0xB4, 0x00, 0x00,
                0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte)0xAE, 0x42,
                0x60, (byte)0x82
            };
            
            TestUploadedFile testFile = new TestUploadedFile(
                "test-azure-" + System.currentTimeMillis() + ".png",
                "image/png",
                testData
            );
            
            // Intentar subir el archivo a Azure
            LOGGER.info("Intentando subir archivo a Azure...");
            MediaFile result = multimediaService.uploadFile(testTitle, testFile, FileType.POSTER, "sistema-test");
            
            if (result != null && result.getBlobUrl() != null) {
                LOGGER.info("✅ AZURE BLOB STORAGE FUNCIONANDO CORRECTAMENTE");
                LOGGER.info("✅ Archivo subido a: " + result.getBlobUrl());
                addInfoMessage("✅ Azure funciona! Archivo subido a: " + result.getBlobUrl());
            } else {
                LOGGER.severe("❌ ERROR: No se pudo subir el archivo a Azure");
                addErrorMessage("❌ Error: No se pudo subir el archivo a Azure");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ ERROR EN PRUEBA DE AZURE", e);
            addErrorMessage("❌ Error en prueba de Azure: " + e.getMessage());
        }
        return null;
    }
    
    // Clase interna para simular un archivo subido
    private static class TestUploadedFile implements UploadedFile {
        private final String fileName;
        private final String contentType;
        private final byte[] content;
        
        public TestUploadedFile(String fileName, String contentType, byte[] content) {
            this.fileName = fileName;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getFileName() { return fileName; }
        
        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return new java.io.ByteArrayInputStream(content);
        }
        
        @Override
        public long getSize() { return content.length; }
        
        @Override
        public byte[] getContent() { return content; }
        
        @Override
        public String getContentType() { return contentType; }
        
        @Override
        public void write(String filePath) throws Exception {}
        
        @Override
        public void delete() {}
    }
    
    // ==================== Utility Methods ====================
    
    private void clearFacesMessages() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return;
        for (Iterator<FacesMessage> it = ctx.getMessages(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
    }
    
    private void addInfoMessage(String message) {
        // Agregar mensaje solo al componente de mensajes principal
        FacesContext.getCurrentInstance().addMessage("frmMain:messages",
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
    
    private void addErrorMessage(String message) {
        // Agregar mensaje solo al componente de mensajes principal
        FacesContext.getCurrentInstance().addMessage("frmMain:messages",
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
    
    private String getFieldLabel(String fieldName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("titleName", "Nombre del Título");
        labels.put("titleType", "Tipo de Título");
        labels.put("releaseYear", "Año de Lanzamiento");
        labels.put("synopsis", "Sinopsis");
        labels.put("averageRating", "Calificación Promedio");
        labels.put("genres", "Géneros");
        return labels.getOrDefault(fieldName, fieldName);
    }
    
    private String getGenreFieldLabel(String fieldName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("genreName", "Nombre del Género");
        return labels.getOrDefault(fieldName, fieldName);
    }
    
    private String getCurrentUser() {
        // En un sistema real, esto vendría del contexto de seguridad
        return "admin";
    }
    
    // ==================== Getters and Setters ====================
    
    public boolean isDialogVisible() {
        return dialogVisible;
    }
    
    public void setDialogVisible(boolean dialogVisible) {
        this.dialogVisible = dialogVisible;
    }
    
    public boolean isGenreDialogVisible() {
        return genreDialogVisible;
    }
    
    public void setGenreDialogVisible(boolean genreDialogVisible) {
        this.genreDialogVisible = genreDialogVisible;
    }
    
    public MediaTitle getSelectedTitle() {
        return selectedTitle;
    }
    
    public void setSelectedTitle(MediaTitle selectedTitle) {
        this.selectedTitle = selectedTitle;
    }
    
    public MovieGenre getSelectedGenre() {
        return selectedGenre;
    }
    
    public void setSelectedGenre(MovieGenre selectedGenre) {
        this.selectedGenre = selectedGenre;
    }
    
    public List<MovieGenre> getSelectedGenres() {
        return selectedGenres;
    }
    
    public void setSelectedGenres(List<MovieGenre> selectedGenres) {
        this.selectedGenres = selectedGenres;
    }
    
    public Part getUploadedPosterFile() {
        return uploadedPosterFile;
    }
    
    public void setUploadedPosterFile(Part uploadedPosterFile) {
        this.uploadedPosterFile = uploadedPosterFile;
    }
    
    public UploadedFile getTechnicalFile() {
        return technicalFile;
    }
    
    public void setTechnicalFile(UploadedFile technicalFile) {
        this.technicalFile = technicalFile;
    }
    
    // ==================== Inner Classes ====================
    
    /**
     * Clase para convertir Jakarta Servlet Part a PrimeFaces UploadedFile
     */
    private static class PartUploadedFile implements UploadedFile {
        private final Part part;
        
        public PartUploadedFile(Part part) {
            this.part = part;
        }
        
        @Override
        public String getFileName() {
            return part.getSubmittedFileName();
        }
        
        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return part.getInputStream();
        }
        
        @Override
        public long getSize() {
            return part.getSize();
        }
        
        @Override
        public byte[] getContent() {
            try {
                return part.getInputStream().readAllBytes();
            } catch (java.io.IOException e) {
                throw new RuntimeException("Error al leer contenido del archivo", e);
            }
        }
        
        @Override
        public String getContentType() {
            return part.getContentType();
        }
        
        @Override
        public void write(String filePath) throws Exception {
            part.write(filePath);
        }
        
        @Override
        public void delete() {
            try {
                part.delete();
            } catch (java.io.IOException e) {
                // Ignorar
            }
        }
    }
}