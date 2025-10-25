package com.jaestrada.multimedia.services;

import com.jaestrada.multimedia.enums.FileType;
import com.jaestrada.multimedia.exceptions.MultimediaException;
import com.jaestrada.multimedia.models.MediaFile;
import com.jaestrada.multimedia.models.MediaTitle;
import com.jaestrada.multimedia.models.MovieGenre;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.primefaces.model.file.UploadedFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class MultimediaService {
    
    private static final Logger LOGGER = Logger.getLogger(MultimediaService.class.getName());
    
    @Inject
    private EntityManager em;
    
    @Inject
    private FileStorageService fileStorageService;
    
    // ==================== CRUD Operations for MediaTitle ====================
    
    @Transactional
    public void saveMediaTitle(MediaTitle title) throws MultimediaException {
        validateMediaTitle(title);
        
        try {
            if (title.getId() == null) {
                em.persist(title);
                LOGGER.info("Nuevo título multimedia creado: " + title.getTitleName());
            } else {
                em.merge(title);
                LOGGER.info("Título multimedia actualizado: " + title.getTitleName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar título multimedia: " + title.getTitleName(), e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al guardar el título multimedia: " + e.getMessage(),
                e
            );
        }
    }
    
    public List<MediaTitle> getAllTitles() {
        TypedQuery<MediaTitle> query = em.createQuery(
            "SELECT DISTINCT mt FROM MediaTitle mt LEFT JOIN FETCH mt.genres ORDER BY mt.createdAt DESC", 
            MediaTitle.class
        );
        return query.getResultList();
    }
    
    public MediaTitle findById(Long id) throws MultimediaException {
        try {
            MediaTitle title = em.find(MediaTitle.class, id);
            if (title == null) {
                throw new MultimediaException(
                    MultimediaException.Type.TITLE_NOT_FOUND,
                    "Título multimedia no encontrado con ID: " + id
                );
            }
            return title;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al buscar título multimedia con ID: " + id, e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al buscar el título multimedia: " + e.getMessage(),
                e
            );
        }
    }
    
    @Transactional
    public void deleteMediaTitle(Long id) throws MultimediaException {
        try {
            MediaTitle title = findById(id);
            
            // Eliminar archivos asociados de Azure Blob Storage
            for (MediaFile file : title.getMediaFiles()) {
                try {
                    fileStorageService.deleteFile(file.getLocalUrl()); // localUrl contiene el nombre del blob
                } catch (MultimediaException e) {
                    LOGGER.warning("No se pudo eliminar archivo de Azure Blob: " + file.getLocalUrl());
                }
            }
            
            em.remove(title);
            LOGGER.info("Título multimedia eliminado: " + title.getTitleName());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar título multimedia con ID: " + id, e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al eliminar el título multimedia: " + e.getMessage(),
                e
            );
        }
    }
    
    // ==================== Genre Management ====================
    
    public List<MovieGenre> getAllGenres() {
        TypedQuery<MovieGenre> query = em.createQuery(
            "SELECT mg FROM MovieGenre mg ORDER BY mg.genreName", 
            MovieGenre.class
        );
        return query.getResultList();
    }
    
    @Transactional
    public void saveGenre(MovieGenre genre) throws MultimediaException {
        validateGenre(genre);
        
        try {
            // Verificar si ya existe un género con el mismo nombre
            if (genreExistsByName(genre.getGenreName(), genre.getId())) {
                throw new MultimediaException(
                    MultimediaException.Type.DUPLICATE_GENRE,
                    "Ya existe un género con el nombre: " + genre.getGenreName()
                );
            }
            
            if (genre.getId() == null) {
                em.persist(genre);
                LOGGER.info("Nuevo género creado: " + genre.getGenreName());
            } else {
                em.merge(genre);
                LOGGER.info("Género actualizado: " + genre.getGenreName());
            }
        } catch (MultimediaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar género: " + genre.getGenreName(), e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al guardar el género: " + e.getMessage(),
                e
            );
        }
    }
    
    @Transactional
    public void deleteGenre(Long id) throws MultimediaException {
        try {
            MovieGenre genre = em.find(MovieGenre.class, id);
            if (genre == null) {
                throw new MultimediaException(
                    MultimediaException.Type.TITLE_NOT_FOUND,
                    "Género no encontrado con ID: " + id
                );
            }
            
            // Verificar si el género está siendo usado
            long titleCount = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt JOIN mt.genres g WHERE g.id = :genreId", 
                Long.class
            ).setParameter("genreId", id).getSingleResult();
            
            if (titleCount > 0) {
                throw new MultimediaException(
                    MultimediaException.Type.STORAGE_ERROR,
                    "No se puede eliminar el género porque está siendo usado por " + titleCount + " título(s)"
                );
            }
            
            em.remove(genre);
            LOGGER.info("Género eliminado: " + genre.getGenreName());
            
        } catch (MultimediaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar género con ID: " + id, e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al eliminar el género: " + e.getMessage(),
                e
            );
        }
    }
    
    public int getTitleCountForGenre(Long genreId) {
        try {
            Long count = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt JOIN mt.genres g WHERE g.id = :genreId", 
                Long.class
            ).setParameter("genreId", genreId).getSingleResult();
            return count.intValue();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener conteo de títulos para género: " + genreId, e);
            return 0;
        }
    }
    
    // ==================== File Operations ====================
    
    @Transactional
    public MediaFile uploadFile(MediaTitle title, UploadedFile file, FileType fileType, String uploadedBy) 
            throws MultimediaException {
        
        try {
            // Si es un poster, eliminar el poster anterior si existe
            if (fileType == FileType.POSTER) {
                removeExistingPoster(title);
            }
            
            // Subir archivo a Azure Blob Storage
            AzureBlobStorageService.BlobUploadResult uploadResult = 
                fileStorageService.saveFile(file, fileType, title.getTitleName());
            
            // Crear entidad MediaFile con información de Azure
            MediaFile mediaFile = new MediaFile();
            mediaFile.setMediaTitle(title);
            mediaFile.setFileType(fileType);
            mediaFile.setLocalUrl(uploadResult.getBlobName()); // Nombre del blob para referencia interna
            mediaFile.setBlobUrl(uploadResult.getBlobUrl()); // URL completa del blob
            mediaFile.setEtag(uploadResult.getEtag());
            mediaFile.setContentType(uploadResult.getContentType());
            mediaFile.setSizeBytes(uploadResult.getSizeBytes());
            mediaFile.setUploadedBy(uploadedBy);
            
            em.persist(mediaFile);
            
            LOGGER.info("Archivo subido exitosamente a Azure Blob: " + file.getFileName() + " para título: " + title.getTitleName());
            return mediaFile;
            
        } catch (MultimediaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al subir archivo: " + file.getFileName(), e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al subir el archivo: " + e.getMessage(),
                e
            );
        }
    }
    
    @Transactional
    public void deleteFile(Long fileId) throws MultimediaException {
        try {
            MediaFile mediaFile = em.find(MediaFile.class, fileId);
            if (mediaFile == null) {
                throw new MultimediaException(
                    MultimediaException.Type.TITLE_NOT_FOUND,
                    "Archivo no encontrado con ID: " + fileId
                );
            }
            
            // Eliminar archivo de Azure Blob Storage usando el nombre del blob
            fileStorageService.deleteFile(mediaFile.getLocalUrl());
            
            // Eliminar registro de la base de datos
            em.remove(mediaFile);
            
            LOGGER.info("Archivo eliminado de Azure Blob: " + mediaFile.getLocalUrl());
            
        } catch (MultimediaException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar archivo con ID: " + fileId, e);
            throw new MultimediaException(
                MultimediaException.Type.STORAGE_ERROR,
                "Error al eliminar el archivo: " + e.getMessage(),
                e
            );
        }
    }
    
    // ==================== Validation Methods ====================
    
    private void validateMediaTitle(MediaTitle title) throws MultimediaException {
        if (title.getTitleName() == null || title.getTitleName().trim().isEmpty()) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El nombre del título es obligatorio"
            );
        }
        
        if (title.getTitleType() == null) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El tipo de título es obligatorio"
            );
        }
        
        if (title.getReleaseYear() != null) {
            int currentYear = LocalDateTime.now().getYear();
            if (title.getReleaseYear() > currentYear) {
                throw new MultimediaException(
                    MultimediaException.Type.INVALID_FILE_TYPE,
                    "El año de lanzamiento no puede ser futuro"
                );
            }
        }
        
        // Validar que tenga al menos un género asignado
        if (title.getGenres() == null || title.getGenres().isEmpty()) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El título debe tener al menos un género asignado"
            );
        }
    }
    
    private void validateGenre(MovieGenre genre) throws MultimediaException {
        if (genre.getGenreName() == null || genre.getGenreName().trim().isEmpty()) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El nombre del género es obligatorio"
            );
        }
        
        if (genre.getGenreName().length() < 3 || genre.getGenreName().length() > 50) {
            throw new MultimediaException(
                MultimediaException.Type.INVALID_FILE_TYPE,
                "El nombre del género debe tener entre 3 y 50 caracteres"
            );
        }
    }
    
    private boolean genreExistsByName(String genreName, Long excludeId) {
        try {
            TypedQuery<Long> query;
            if (excludeId != null) {
                query = em.createQuery(
                    "SELECT COUNT(mg) FROM MovieGenre mg WHERE LOWER(mg.genreName) = LOWER(:name) AND mg.id != :id", 
                    Long.class
                );
                query.setParameter("id", excludeId);
            } else {
                query = em.createQuery(
                    "SELECT COUNT(mg) FROM MovieGenre mg WHERE LOWER(mg.genreName) = LOWER(:name)", 
                    Long.class
                );
            }
            query.setParameter("name", genreName);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al verificar existencia de género: " + genreName, e);
            return false;
        }
    }
    
    private void removeExistingPoster(MediaTitle title) {
        try {
            TypedQuery<MediaFile> query = em.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle = :title AND mf.fileType = :fileType", 
                MediaFile.class
            );
            query.setParameter("title", title);
            query.setParameter("fileType", FileType.POSTER);
            
            List<MediaFile> existingPosters = query.getResultList();
            for (MediaFile poster : existingPosters) {
                try {
                    fileStorageService.deleteFile(poster.getLocalUrl());
                } catch (MultimediaException e) {
                    LOGGER.warning("No se pudo eliminar poster anterior: " + poster.getLocalUrl());
                }
                em.remove(poster);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al eliminar poster anterior", e);
        }
    }
    
    // ==================== Utility Methods ====================
    
    public MediaFile getPosterForTitle(Long titleId) {
        try {
            TypedQuery<MediaFile> query = em.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle.id = :titleId AND mf.fileType = :fileType", 
                MediaFile.class
            );
            query.setParameter("titleId", titleId);
            query.setParameter("fileType", FileType.POSTER);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<MediaFile> getTechnicalSheetsForTitle(Long titleId) {
        TypedQuery<MediaFile> query = em.createQuery(
            "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle.id = :titleId AND mf.fileType = :fileType ORDER BY mf.uploadedAt DESC", 
            MediaFile.class
        );
        query.setParameter("titleId", titleId);
        query.setParameter("fileType", FileType.TECHNICAL_SHEET);
        return query.getResultList();
    }
}