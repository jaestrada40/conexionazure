package com.jaestrada.multimedia.controllers;

import com.jaestrada.multimedia.services.DashboardMultimediaService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class DashboardBean implements Serializable {
    
    @Inject
    private DashboardMultimediaService multimediaService;
    
    // ==================== Multimedia Metrics ====================
    
    public Long getTotalMultimediaTitles() {
        return multimediaService.getTotalTitles();
    }
    
    public String getMovieVsSeriesRatio() {
        return multimediaService.getMovieVsSeriesRatio();
    }
    
    public Long getAvailableGenres() {
        return multimediaService.getTotalGenres();
    }
    
    public Long getTitlesWithPosters() {
        return multimediaService.getTitlesWithPoster();
    }
    
    public Long getRecentTitles() {
        return multimediaService.getTitlesLastMonth();
    }
    
    public String getPosterCoveragePercentage() {
        return multimediaService.getPosterCoveragePercentage();
    }
    
    public String getTotalStorageUsed() {
        return multimediaService.getTotalStorageUsed();
    }
    
    public String getMostRecentTitleName() {
        return multimediaService.getMostRecentTitleName();
    }
    
    // ==================== Azure Blob Storage Metrics ====================
    
    /**
     * Cantidad total de archivos almacenados en Azure Blob
     */
    public Long getTotalFilesInAzureBlob() {
        return multimediaService.getTotalFilesInAzureBlob();
    }
    
    /**
     * Número de títulos que tienen póster asignado en Azure Blob
     */
    public Long getTitlesWithPosterInAzureBlob() {
        return multimediaService.getTitlesWithPosterInAzureBlob();
    }
    
    /**
     * Número de títulos registrados durante el último mes
     */
    public Long getTitlesRegisteredLastMonth() {
        return multimediaService.getTitlesRegisteredLastMonth();
    }
    
    /**
     * Tamaño total de archivos almacenados en Azure Blob Storage
     */
    public String getTotalStorageInAzureBlob() {
        return multimediaService.getTotalStorageInAzureBlob();
    }
    
    /**
     * Número de posters almacenados en Azure Blob Storage
     */
    public Long getPostersInAzureBlob() {
        return multimediaService.getPostersInAzureBlob();
    }
    
    /**
     * Número de fichas técnicas almacenadas en Azure Blob Storage
     */
    public Long getTechnicalSheetsInAzureBlob() {
        return multimediaService.getTechnicalSheetsInAzureBlob();
    }
    
    /**
     * Porcentaje de títulos que tienen poster en Azure Blob Storage
     */
    public String getPosterCoverageInAzureBlobPercentage() {
        return multimediaService.getPosterCoverageInAzureBlobPercentage();
    }
    
    /**
     * Estadísticas resumidas de Azure Blob Storage
     */
    public String getAzureBlobStorageStats() {
        return multimediaService.getAzureBlobStorageStats();
    }
}