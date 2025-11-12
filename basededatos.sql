-- -------------------------------------------------------------
-- TablePlus 6.7.3(640)
--
-- https://tableplus.com/
--
-- Database: tareaclase
-- Generation Time: 2025-11-11 22:39:40.0170
-- -------------------------------------------------------------


DROP TABLE IF EXISTS "public"."media_titles";
-- Sequence and defined type
CREATE SEQUENCE IF NOT EXISTS media_titles_media_title_id_seq;

-- Table Definition
CREATE TABLE "public"."media_titles" (
    "media_title_id" int8 NOT NULL DEFAULT nextval('media_titles_media_title_id_seq'::regclass),
    "title_name" varchar(150) NOT NULL,
    "title_type" varchar(255) NOT NULL CHECK ((title_type)::text = ANY (ARRAY[('MOVIE'::character varying)::text, ('SERIES'::character varying)::text])),
    "release_year" int4 CHECK ((release_year >= 1900) AND (release_year <= 2100)),
    "synopsis" varchar(1000),
    "average_rating" float8 CHECK ((average_rating >= (0.0)::double precision) AND (average_rating <= (10.0)::double precision)),
    "created_at" timestamp DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY ("media_title_id")
);

DROP TABLE IF EXISTS "public"."media_title_genres";
-- Table Definition
CREATE TABLE "public"."media_title_genres" (
    "media_title_id" int8 NOT NULL,
    "movie_genre_id" int8 NOT NULL,
    PRIMARY KEY ("media_title_id","movie_genre_id")
);

DROP TABLE IF EXISTS "public"."movie_genres";
-- Sequence and defined type
CREATE SEQUENCE IF NOT EXISTS movie_genres_movie_genre_id_seq;

-- Table Definition
CREATE TABLE "public"."movie_genres" (
    "movie_genre_id" int8 NOT NULL DEFAULT nextval('movie_genres_movie_genre_id_seq'::regclass),
    "genre_name" varchar(50) NOT NULL,
    PRIMARY KEY ("movie_genre_id")
);

DROP TABLE IF EXISTS "public"."media_files";
-- Sequence and defined type
CREATE SEQUENCE IF NOT EXISTS media_files_media_file_id_seq;

-- Table Definition
CREATE TABLE "public"."media_files" (
    "media_file_id" int8 NOT NULL DEFAULT nextval('media_files_media_file_id_seq'::regclass),
    "media_title_id" int8 NOT NULL,
    "file_type" varchar(255) NOT NULL CHECK ((file_type)::text = ANY (ARRAY[('POSTER'::character varying)::text, ('TECHNICAL_SHEET'::character varying)::text])),
    "local_url" varchar(500) NOT NULL,
    "etag" varchar(100),
    "content_type" varchar(50),
    "size_bytes" int8,
    "uploaded_at" timestamp DEFAULT CURRENT_TIMESTAMP,
    "uploaded_by" varchar(50),
    "blob_url" varchar(500),
    PRIMARY KEY ("media_file_id")
);

INSERT INTO "public"."media_titles" ("media_title_id", "title_name", "title_type", "release_year", "synopsis", "average_rating", "created_at") VALUES
(1, 'El Padrino', 'MOVIE', 1972, 'La historia de una familia de la mafia italiana en Nueva York.', 9.2, '2025-11-12 01:34:57.49503'),
(2, 'Breaking Bad', 'SERIES', 2008, 'Un profesor de química se convierte en fabricante de metanfetaminas.', 9.5, '2025-11-12 01:34:57.49503');

INSERT INTO "public"."media_title_genres" ("media_title_id", "movie_genre_id") VALUES
(1, 2),
(1, 7),
(2, 2),
(2, 7);

INSERT INTO "public"."movie_genres" ("movie_genre_id", "genre_name") VALUES
(1, 'Acción'),
(2, 'Drama'),
(3, 'Comedia'),
(4, 'Terror'),
(5, 'Ciencia Ficción'),
(6, 'Romance'),
(7, 'Thriller'),
(8, 'Aventura'),
(9, 'Animación'),
(10, 'Documental'),
(11, 'Fantasía'),
(12, 'Misterio'),
(13, 'Crimen'),
(14, 'Guerra'),
(15, 'Western'),
(16, 'Musical'),
(17, 'Biografía'),
(18, 'Historia'),
(19, 'Deportes'),
(20, 'Familia'),
(21, 'Prueba');

INSERT INTO "public"."media_files" ("media_file_id", "media_title_id", "file_type", "local_url", "etag", "content_type", "size_bytes", "uploaded_at", "uploaded_by", "blob_url") VALUES
(8, 1, 'POSTER', 'posters/El_Padrino/20251111_223231.jpg', '0x8DE21A4BB7BC9F2', 'image/jpeg', 1538421, '2025-11-11 22:34:13.809016', 'admin', 'https://storagejaestradag.blob.core.windows.net/catalogos/posters%2FEl_Padrino%2F20251111_223231.jpg'),
(10, 1, 'POSTER', 'posters/El_Padrino/20251111_223518.jpg', '0x8DE21A4E262EC99', 'image/jpeg', 219408, '2025-11-11 22:35:19.049751', 'admin', 'https://storagejaestradag.blob.core.windows.net/catalogos/posters%2FEl_Padrino%2F20251111_223518.jpg'),
(11, 2, 'POSTER', 'posters/Breaking_Bad/20251111_223610.jpg', '0x8DE21A5014D9CB6', 'image/jpeg', 10247, '2025-11-11 22:36:10.931525', 'admin', 'https://storagejaestradag.blob.core.windows.net/catalogos/posters%2FBreaking_Bad%2F20251111_223610.jpg');



-- Indices
CREATE INDEX idx_media_titles_year ON public.media_titles USING btree (release_year);
CREATE INDEX idx_media_titles_created ON public.media_titles USING btree (created_at);
CREATE INDEX idx_media_titles_type ON public.media_titles USING btree (title_type);
ALTER TABLE "public"."media_title_genres" ADD FOREIGN KEY ("media_title_id") REFERENCES "public"."media_titles"("media_title_id") ON DELETE CASCADE;
ALTER TABLE "public"."media_title_genres" ADD FOREIGN KEY ("movie_genre_id") REFERENCES "public"."movie_genres"("movie_genre_id") ON DELETE CASCADE;


-- Indices
CREATE UNIQUE INDEX movie_genres_genre_name_key ON public.movie_genres USING btree (genre_name);
ALTER TABLE "public"."media_files" ADD FOREIGN KEY ("media_title_id") REFERENCES "public"."media_titles"("media_title_id") ON DELETE CASCADE;


-- Indices
CREATE INDEX idx_media_files_title ON public.media_files USING btree (media_title_id);
CREATE INDEX idx_media_files_type ON public.media_files USING btree (file_type);
