-- Script completo: Limpiar base de datos y configurar solo sistema multimedia
-- Este script elimina TODO y crea solo las tablas multimedia

-- PASO 1: Eliminar todas las tablas existentes
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;

-- PASO 2: Crear las tablas del sistema multimedia
-- Tabla de géneros de películas
CREATE TABLE movie_genres (
    movie_genre_id BIGSERIAL PRIMARY KEY,
    genre_name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabla de títulos multimedia (películas y series)
CREATE TABLE media_titles (
    media_title_id BIGSERIAL PRIMARY KEY,
    title_name VARCHAR(150) NOT NULL,
    title_type VARCHAR(20) NOT NULL CHECK (title_type IN ('MOVIE', 'SERIES')),
    release_year INTEGER CHECK (release_year >= 1900 AND release_year <= 2100),
    synopsis VARCHAR(1000),
    average_rating DECIMAL(3,1) CHECK (average_rating >= 0.0 AND average_rating <= 10.0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de archivos multimedia
CREATE TABLE media_files (
    media_file_id BIGSERIAL PRIMARY KEY,
    media_title_id BIGINT NOT NULL REFERENCES media_titles(media_title_id) ON DELETE CASCADE,
    file_type VARCHAR(20) NOT NULL CHECK (file_type IN ('POSTER', 'TECHNICAL_SHEET')),
    local_url VARCHAR(500) NOT NULL,
    etag VARCHAR(100),
    content_type VARCHAR(50),
    size_bytes BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(50)
);

-- Tabla de relación muchos a muchos entre títulos y géneros
CREATE TABLE media_title_genres (
    media_title_id BIGINT NOT NULL REFERENCES media_titles(media_title_id) ON DELETE CASCADE,
    movie_genre_id BIGINT NOT NULL REFERENCES movie_genres(movie_genre_id) ON DELETE CASCADE,
    PRIMARY KEY (media_title_id, movie_genre_id)
);

-- PASO 3: Crear índices para optimizar rendimiento
CREATE INDEX idx_media_titles_type ON media_titles(title_type);
CREATE INDEX idx_media_titles_year ON media_titles(release_year);
CREATE INDEX idx_media_titles_created ON media_titles(created_at);
CREATE INDEX idx_media_files_title ON media_files(media_title_id);
CREATE INDEX idx_media_files_type ON media_files(file_type);

-- PASO 4: Insertar datos iniciales (géneros)
INSERT INTO movie_genres (genre_name) VALUES 
    ('Acción'),
    ('Drama'),
    ('Comedia'),
    ('Terror'),
    ('Ciencia Ficción'),
    ('Romance'),
    ('Thriller'),
    ('Aventura'),
    ('Animación'),
    ('Documental'),
    ('Fantasía'),
    ('Misterio'),
    ('Crimen'),
    ('Guerra'),
    ('Western'),
    ('Musical'),
    ('Biografía'),
    ('Historia'),
    ('Deportes'),
    ('Familia');

-- PASO 5: Insertar algunos títulos de ejemplo (opcional)
INSERT INTO media_titles (title_name, title_type, release_year, synopsis, average_rating) VALUES 
    ('El Padrino', 'MOVIE', 1972, 'La historia de una familia de la mafia italiana en Nueva York.', 9.2),
    ('Breaking Bad', 'SERIES', 2008, 'Un profesor de química se convierte en fabricante de metanfetaminas.', 9.5),
    ('Pulp Fiction', 'MOVIE', 1994, 'Varias historias criminales entrelazadas en Los Ángeles.', 8.9);

-- Asignar géneros a los títulos de ejemplo
INSERT INTO media_title_genres (media_title_id, movie_genre_id) VALUES 
    (1, 2), (1, 7),  -- El Padrino: Drama, Thriller
    (2, 2), (2, 7),  -- Breaking Bad: Drama, Thriller  
    (3, 2), (3, 13); -- Pulp Fiction: Drama, Crimen

-- PASO 6: Verificar que todo se creó correctamente
SELECT 'Tablas creadas:' as info;
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;

SELECT 'Géneros insertados:' as info;
SELECT count(*) as total_generos FROM movie_genres;

SELECT 'Títulos de ejemplo:' as info;
SELECT count(*) as total_titulos FROM media_titles;

SELECT 'Base de datos lista para usar el sistema multimedia' as mensaje;