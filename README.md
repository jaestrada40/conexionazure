# Catálogo Multimedia -
- **Jonathan Javier Soberanis Castillo**
- **Javier Augusto Estrada Gordillo**

### 1. Clonar el Repositorio
```bash
git clone <url-del-repositorio>
cd tareadehoy
```

### 2. Configurar Base de Datos PostgreSQL

#### Crear la base de datos:
```bash
psql -U postgres
CREATE DATABASE tareaclase;
\q
```

#### Ejecutar el script SQL:
```bash
psql -U postgres -d tareaclase -f basededatos.sql
```

O desde pgAdmin/DBeaver, ejecutar el contenido del archivo `basededatos.sql`

### 3. Configurar Credenciales de Azure

#### Crear archivo de configuración:
1. Copia el archivo de ejemplo:
```bash
cp src/main/resources/config/.env.example src/main/resources/config/.env
```
