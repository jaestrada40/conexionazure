package com.jaestrada.multimedia.models;

import com.jaestrada.multimedia.enums.TitleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media_titles")
public class MediaTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_title_id")
    private Long id;

    @NotNull(message = "El nombre del título es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(name = "title_name", nullable = false, length = 150)
    private String titleName;

    @NotNull(message = "El tipo de título es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "title_type", nullable = false)
    private TitleType titleType;

    @Min(value = 1900, message = "El año debe ser mayor o igual a 1900")
    @Max(value = 2100, message = "El año debe ser menor o igual a 2100")
    @Column(name = "release_year")
    private Integer releaseYear;

    @Size(max = 1000, message = "La sinopsis no puede exceder 1000 caracteres")
    @Column(name = "synopsis", length = 1000)
    private String synopsis;

    @DecimalMin(value = "0.0", message = "La calificación debe ser mayor o igual a 0.0")
    @DecimalMax(value = "10.0", message = "La calificación debe ser menor o igual a 10.0")
    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "mediaTitle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MediaFile> mediaFiles = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "media_title_genres",
        joinColumns = @JoinColumn(name = "media_title_id"),
        inverseJoinColumns = @JoinColumn(name = "movie_genre_id")
    )
    private List<MovieGenre> genres = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public TitleType getTitleType() {
        return titleType;
    }

    public void setTitleType(TitleType titleType) {
        this.titleType = titleType;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public List<MovieGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<MovieGenre> genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        return "MediaTitle{" +
                "id=" + id +
                ", titleName='" + titleName + '\'' +
                ", titleType=" + titleType +
                ", releaseYear=" + releaseYear +
                ", synopsis='" + synopsis + '\'' +
                ", averageRating=" + averageRating +
                ", createdAt=" + createdAt +
                '}';
    }
}