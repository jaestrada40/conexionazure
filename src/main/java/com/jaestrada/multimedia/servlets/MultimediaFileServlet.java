package com.jaestrada.multimedia.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/multimedia/*")
public class MultimediaFileServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(MultimediaFileServlet.class.getName());
    private static final String UPLOAD_BASE_DIR = System.getProperty("jboss.server.data.dir", "/tmp") + "/multimedia/";
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Remover el "/" inicial
        String relativePath = pathInfo.substring(1);
        
        // Construir la ruta completa del archivo
        Path filePath = Paths.get(UPLOAD_BASE_DIR, relativePath);
        File file = filePath.toFile();
        
        // Verificar que el archivo existe y está dentro del directorio permitido
        if (!file.exists() || !file.isFile()) {
            LOGGER.warning("Archivo no encontrado: " + filePath);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Verificar que el archivo está dentro del directorio base (seguridad)
        try {
            Path basePath = Paths.get(UPLOAD_BASE_DIR).toRealPath();
            Path requestedPath = filePath.toRealPath();
            if (!requestedPath.startsWith(basePath)) {
                LOGGER.warning("Intento de acceso fuera del directorio permitido: " + requestedPath);
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error al verificar ruta del archivo", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        try {
            // Determinar el tipo de contenido
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = getServletContext().getMimeType(file.getName());
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // Configurar la respuesta
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            
            // Configurar headers para cache (opcional)
            response.setHeader("Cache-Control", "public, max-age=3600");
            response.setDateHeader("Last-Modified", file.lastModified());
            
            // Verificar If-Modified-Since para cache
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            if (ifModifiedSince != -1 && ifModifiedSince >= file.lastModified()) {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
            
            // Enviar el archivo
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }
            
            LOGGER.fine("Archivo servido exitosamente: " + relativePath);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error al servir archivo: " + relativePath, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}