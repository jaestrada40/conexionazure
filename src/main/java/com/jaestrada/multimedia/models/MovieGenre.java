package com.jaestrada.multimedia.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie_genres")
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_genre_id")
    private Long id;

    @NotNull(message = "El nombre del género es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del género debe tener entre 3 y 50 caracteres")
    @Column(name = "genre_name", nullable = false, unique = true, length = 50)
    private String genreName;

    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private List<MediaTitle> mediaTitles = new ArrayList<>();

    // Constructors
    public MovieGenre() {}

    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public List<MediaTitle> getMediaTitles() {
        return mediaTitles;
    }

    public void setMediaTitles(List<MediaTitle> mediaTitles) {
        this.mediaTitles = mediaTitles;
    }

    @Override
    public String toString() {
        return "MovieGenre{" +
                "id=" + id +
                ", genreName='" + genreName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieGenre that = (MovieGenre) o;
        return genreName != null ? genreName.equals(that.genreName) : that.genreName == null;
    }

    @Override
    public int hashCode() {
        return genreName != null ? genreName.hashCode() : 0;
    }
}