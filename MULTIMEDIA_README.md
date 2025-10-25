# Sistema de Catálogo Multimedia

Sistema de gestión de películas y series con subida de archivos y dashboard de métricas.

## Funcionalidades
- CRUD completo de títulos multimedia (películas/series)
- Gestión de géneros
- Subida de archivos (posters JPG/PNG, fichas técnicas PDF)
- Dashboard con métricas
- Validaciones cliente/servidor

## Configuración

### Base de Datos
Las tablas se crean automáticamente con Hibernate DDL:
- `media_titles` - Títulos multimedia
- `movie_genres` - Géneros
- `media_files` - Archivos asociados
- `media_title_genres` - Relación títulos-géneros

### Azure Blob Storage
**Opción 1: Archivo .env**
1. Copiar `src/main/resources/config/.env.example` a `.env`
2. Ver credenciales reales en `CREDENCIALES_AZURE.txt` (archivo local)

**Opción 2: Variables de entorno del sistema**
```bash
export AZURE_STORAGE_CONNECTION_STRING="tu_connection_string"
export AZURE_STORAGE_CONTAINER_NAME="catalogos"
```

### Datos Iniciales
Ejecutar: `src/main/resources/sql/clean_and_setup_multimedia.sql`

## Uso
1. **Dashboard**: `/home.xhtml` - Métricas del catálogo
2. **Gestión**: `/multimedia.xhtml` - CRUD de títulos y géneros

### Agregar Título
1. Ir a "Catálogo Multimedia"
2. Clic en "Nuevo Título"
3. Completar información y seleccionar géneros
4. Subir archivos (opcional)
5. Guardar

## Validaciones
- Títulos: 2-150 caracteres
- Años: 1900-2100, no futuros
- Géneros: obligatorio, únicos
- Archivos: Posters 2MB max, PDFs 5MB max

## Tecnologías
- Java 21 + Jakarta EE
- JSF + PrimeFaces
- JPA + Hibernate
- PostgreSQL
- WildFly