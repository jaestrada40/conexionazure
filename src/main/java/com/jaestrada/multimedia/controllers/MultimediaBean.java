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
    
    // File upload
    private UploadedFile posterFile;
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
        this.posterFile = null;
        this.technicalFile = null;
        this.dialogVisible = true;
    }
    
    public void save() {
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
                return;
            }
            
            // Asignar géneros seleccionados
            selectedTitle.setGenres(new ArrayList<>(selectedGenres));
            
            // Guardar título
            multimediaService.saveMediaTitle(selectedTitle);
            
            // Subir archivos si están presentes
            if (posterFile != null && posterFile.getSize() > 0) {
                handlePosterUpload();
            }
            
            if (technicalFile != null && technicalFile.getSize() > 0) {
                handleTechnicalUpload();
            }
            
            this.dialogVisible = false;
            addInfoMessage("Título multimedia guardado exitosamente");
            
            // Reset form
            this.selectedTitle = new MediaTitle();
            this.selectedGenres = new ArrayList<>();
            this.posterFile = null;
            this.technicalFile = null;
            
        } catch (MultimediaException e) {
            addErrorMessage("Error al guardar: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al guardar título multimedia", e);
        } catch (Exception e) {
            addErrorMessage("Error inesperado al guardar el título");
            LOGGER.log(Level.SEVERE, "Error inesperado al guardar título multimedia", e);
        }
    }
    
    public void edit(MediaTitle title) {
        clearFacesMessages();
        this.selectedTitle = title;
        this.selectedGenres = new ArrayList<>(title.getGenres());
        this.posterFile = null;
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
    
    public void handlePosterUpload() {
        if (posterFile != null && posterFile.getSize() > 0 && selectedTitle.getId() != null) {
            try {
                multimediaService.uploadFile(selectedTitle, posterFile, FileType.POSTER, getCurrentUser());
                addInfoMessage("Poster subido exitosamente");
                posterFile = null;
            } catch (MultimediaException e) {
                addErrorMessage("Error al subir poster: " + e.getMessage());
                LOGGER.log(Level.WARNING, "Error al subir poster", e);
            }
        }
    }
    
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
    
    public void onPosterUpload(FileUploadEvent event) {
        try {
            if (selectedTitle.getId() != null) {
                multimediaService.uploadFile(selectedTitle, event.getFile(), FileType.POSTER, getCurrentUser());
                addInfoMessage("Poster subido exitosamente: " + event.getFile().getFileName());
            } else {
                this.posterFile = event.getFile();
                addInfoMessage("Poster seleccionado: " + event.getFile().getFileName() + ". Se subirá al guardar el título.");
            }
        } catch (MultimediaException e) {
            addErrorMessage("Error al subir poster: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al subir poster", e);
        }
    }
    
    public void onTechnicalUpload(FileUploadEvent event) {
        try {
            if (selectedTitle.getId() != null) {
                multimediaService.uploadFile(selectedTitle, event.getFile(), FileType.TECHNICAL_SHEET, getCurrentUser());
                addInfoMessage("Ficha técnica subida exitosamente: " + event.getFile().getFileName());
            } else {
                this.technicalFile = event.getFile();
                addInfoMessage("Ficha técnica seleccionada: " + event.getFile().getFileName() + ". Se subirá al guardar el título.");
            }
        } catch (MultimediaException e) {
            addErrorMessage("Error al subir ficha técnica: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Error al subir ficha técnica", e);
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
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
    
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
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
    
    public UploadedFile getPosterFile() {
        return posterFile;
    }
    
    public void setPosterFile(UploadedFile posterFile) {
        this.posterFile = posterFile;
    }
    
    public UploadedFile getTechnicalFile() {
        return technicalFile;
    }
    
    public void setTechnicalFile(UploadedFile technicalFile) {
        this.technicalFile = technicalFile;
    }
}