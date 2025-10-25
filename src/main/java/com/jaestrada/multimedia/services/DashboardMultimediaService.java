package com.jaestrada.multimedia.services;

import com.jaestrada.multimedia.enums.FileType;
import com.jaestrada.multimedia.enums.TitleType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DashboardMultimediaService {
    
    private static final Logger LOGGER = Logger.getLogger(DashboardMultimediaService.class.getName());
    
    @Inject
    private EntityManager em;
    
    /**
     * Obtiene el total de títulos registrados en el sistema
     */
    public Long getTotalTitles() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt", 
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener total de títulos", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de películas registradas
     */
    public Long getMovieCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = :type", 
                Long.class
            );
            query.setParameter("type", TitleType.MOVIE);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener conteo de películas", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de series registradas
     */
    public Long getSeriesCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = :type", 
                Long.class
            );
            query.setParameter("type", TitleType.SERIES);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener conteo de series", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el total de géneros disponibles
     */
    public Long getTotalGenres() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mg) FROM MovieGenre mg", 
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener total de géneros", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de títulos que tienen poster asignado
     */
    public Long getTitlesWithPoster() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(DISTINCT mf.mediaTitle) FROM MediaFile mf WHERE mf.fileType = :fileType", 
                Long.class
            );
            query.setParameter("fileType", FileType.POSTER);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener títulos con poster", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de títulos registrados en el último mes
     */
    public Long getTitlesLastMonth() {
        try {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt >= :date", 
                Long.class
            );
            query.setParameter("date", oneMonthAgo);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener títulos del último mes", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el total de archivos almacenados
     */
    public Long getTotalFiles() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf", 
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener total de archivos", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de fichas técnicas almacenadas
     */
    public Long getTechnicalSheetsCount() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf WHERE mf.fileType = :fileType", 
                Long.class
            );
            query.setParameter("fileType", FileType.TECHNICAL_SHEET);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener conteo de fichas técnicas", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el porcentaje de títulos que tienen poster
     */
    public String getPosterCoveragePercentage() {
        try {
            Long totalTitles = getTotalTitles();
            if (totalTitles == 0) {
                return "0%";
            }
            
            Long titlesWithPoster = getTitlesWithPoster();
            double percentage = (titlesWithPoster.doubleValue() / totalTitles.doubleValue()) * 100;
            return String.format("%.1f%%", percentage);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular porcentaje de cobertura de posters", e);
            return "0%";
        }
    }
    
    /**
     * Obtiene la relación películas vs series como string formateado
     */
    public String getMovieVsSeriesRatio() {
        try {
            Long movies = getMovieCount();
            Long series = getSeriesCount();
            return movies + " películas / " + series + " series";
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener relación películas vs series", e);
            return "0 películas / 0 series";
        }
    }
    
    /**
     * Obtiene el promedio de géneros por título
     */
    public String getAverageGenresPerTitle() {
        try {
            TypedQuery<Double> query = em.createQuery(
                "SELECT AVG(SIZE(mt.genres)) FROM MediaTitle mt", 
                Double.class
            );
            Double average = query.getSingleResult();
            if (average == null) {
                return "0.0";
            }
            return String.format("%.1f", average);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular promedio de géneros por título", e);
            return "0.0";
        }
    }
    
    /**
     * Obtiene el tamaño total de archivos almacenados (en MB)
     */
    public String getTotalStorageUsed() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(mf.sizeBytes), 0) FROM MediaFile mf", 
                Long.class
            );
            Long totalBytes = query.getSingleResult();
            if (totalBytes == null || totalBytes == 0) {
                return "0 MB";
            }
            
            double totalMB = totalBytes.doubleValue() / (1024 * 1024);
            if (totalMB < 1) {
                return String.format("%.2f MB", totalMB);
            } else if (totalMB < 1024) {
                return String.format("%.1f MB", totalMB);
            } else {
                double totalGB = totalMB / 1024;
                return String.format("%.2f GB", totalGB);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular almacenamiento total usado", e);
            return "0 MB";
        }
    }
    
    /**
     * Obtiene el título más reciente registrado
     */
    public String getMostRecentTitleName() {
        try {
            TypedQuery<String> query = em.createQuery(
                "SELECT mt.titleName FROM MediaTitle mt ORDER BY mt.createdAt DESC", 
                String.class
            );
            query.setMaxResults(1);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener título más reciente", e);
            return "Ninguno";
        }
    }
    
    // ==================== Azure Blob Storage Specific Queries ====================
    
    /**
     * Calcula la cantidad total de archivos almacenados en Azure Blob Storage
     * Requisito: "Calcular la cantidad total de archivos almacenados en Azure Blob"
     */
    public Long getTotalFilesInAzureBlob() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf WHERE mf.blobUrl IS NOT NULL", 
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener total de archivos en Azure Blob", e);
            return 0L;
        }
    }
    
    /**
     * Muestra el número de títulos que tienen póster asignado (almacenado en Azure Blob)
     * Requisito: "Mostrar el número de títulos que tienen póster asignado"
     */
    public Long getTitlesWithPosterInAzureBlob() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(DISTINCT mf.mediaTitle) FROM MediaFile mf " +
                "WHERE mf.fileType = :fileType AND mf.blobUrl IS NOT NULL", 
                Long.class
            );
            query.setParameter("fileType", FileType.POSTER);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener títulos con poster en Azure Blob", e);
            return 0L;
        }
    }
    
    /**
     * Muestra el número de títulos registrados durante el último mes
     * Requisito: "Mostrar el número de títulos registrados durante el último mes"
     */
    public Long getTitlesRegisteredLastMonth() {
        try {
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt >= :date", 
                Long.class
            );
            query.setParameter("date", oneMonthAgo);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener títulos registrados en el último mes", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el tamaño total de archivos almacenados en Azure Blob Storage (en bytes)
     * Requisito: Para calcular el almacenamiento usado en Azure Blob
     */
    public Long getTotalBytesInAzureBlob() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COALESCE(SUM(mf.sizeBytes), 0) FROM MediaFile mf WHERE mf.blobUrl IS NOT NULL", 
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener bytes totales en Azure Blob", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el tamaño total de archivos almacenados en Azure Blob Storage (formateado)
     */
    public String getTotalStorageInAzureBlob() {
        try {
            Long totalBytes = getTotalBytesInAzureBlob();
            if (totalBytes == null || totalBytes == 0) {
                return "0 MB";
            }
            
            double totalMB = totalBytes.doubleValue() / (1024 * 1024);
            if (totalMB < 1) {
                return String.format("%.2f MB", totalMB);
            } else if (totalMB < 1024) {
                return String.format("%.1f MB", totalMB);
            } else {
                double totalGB = totalMB / 1024;
                return String.format("%.2f GB", totalGB);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular almacenamiento total en Azure Blob", e);
            return "0 MB";
        }
    }
    
    /**
     * Obtiene el número de posters almacenados en Azure Blob Storage
     */
    public Long getPostersInAzureBlob() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf " +
                "WHERE mf.fileType = :fileType AND mf.blobUrl IS NOT NULL", 
                Long.class
            );
            query.setParameter("fileType", FileType.POSTER);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener posters en Azure Blob", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el número de fichas técnicas almacenadas en Azure Blob Storage
     */
    public Long getTechnicalSheetsInAzureBlob() {
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf " +
                "WHERE mf.fileType = :fileType AND mf.blobUrl IS NOT NULL", 
                Long.class
            );
            query.setParameter("fileType", FileType.TECHNICAL_SHEET);
            return query.getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener fichas técnicas en Azure Blob", e);
            return 0L;
        }
    }
    
    /**
     * Obtiene el porcentaje de títulos que tienen poster en Azure Blob Storage
     */
    public String getPosterCoverageInAzureBlobPercentage() {
        try {
            Long totalTitles = getTotalTitles();
            if (totalTitles == 0) {
                return "0%";
            }
            
            Long titlesWithPoster = getTitlesWithPosterInAzureBlob();
            double percentage = (titlesWithPoster.doubleValue() / totalTitles.doubleValue()) * 100;
            return String.format("%.1f%%", percentage);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al calcular porcentaje de cobertura de posters en Azure Blob", e);
            return "0%";
        }
    }
    
    /**
     * Obtiene estadísticas resumidas de Azure Blob Storage
     */
    public String getAzureBlobStorageStats() {
        try {
            Long totalFiles = getTotalFilesInAzureBlob();
            Long posters = getPostersInAzureBlob();
            Long technicalSheets = getTechnicalSheetsInAzureBlob();
            String totalStorage = getTotalStorageInAzureBlob();
            
            return String.format("Total: %d archivos (%d posters, %d fichas) - %s", 
                totalFiles, posters, technicalSheets, totalStorage);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error al obtener estadísticas de Azure Blob Storage", e);
            return "Error al obtener estadísticas";
        }
    }
}