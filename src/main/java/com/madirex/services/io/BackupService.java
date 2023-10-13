package com.madirex.services.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.madirex.exceptions.DirectoryException;
import com.madirex.exceptions.ImportDataException;
import com.madirex.models.Funko;
import com.madirex.utils.LocalDateAdapter;
import com.madirex.utils.LocalDateTimeAdapter;
import com.madirex.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Clase BackupService
 */
public class BackupService<T> {

    private static BackupService backupServiceInstance;
    private final Logger logger = LoggerFactory.getLogger(BackupService.class);

    /**
     * Constructor de la clase
     */
    private BackupService() {
    }

    /**
     * Devuelve la instancia de la clase
     *
     * @return Instancia de la clase
     */
    public static synchronized BackupService getInstance() {
        if (backupServiceInstance == null) {
            backupServiceInstance = new BackupService();
        }
        return backupServiceInstance;
    }

    /**
     * Exportar los datos pasados por parámetro a un archivo JSON
     *
     * @param path     Ruta del directorio donde se guardará el backup
     * @param fileName Nombre del archivo del backup
     * @param data     Datos a guardar
     */
    public CompletableFuture<Void> exportData(String path, String fileName, T data) {
        CompletableFuture.runAsync(() -> {
            File dataDir = new File(path);
            if (dataDir.exists()) {
                String dest = path + File.separator + fileName;
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .setPrettyPrinting()
                        .create();
                String json = gson.toJson(data);
                Utils.getInstance().writeString(dest, json);
                logger.debug("Backup realizado con éxito");
            } else {
                throw new CompletionException(new DirectoryException("No se creará el backup."));
            }
        }).join();
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Importa los datos de un archivo JSON
     *
     * @param path     Ruta del archivo JSON
     * @param fileName Nombre del archivo JSON
     * @return CompletableFuture de los datos importados
     */
    public CompletableFuture<List<Funko>> importData(String path, String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            File folder = new File(path + File.separator);
            if (!folder.exists()) {
                throw new CompletionException(new DirectoryException("No se creará el backup."));
            }
            File dataFile = new File(path + File.separator + fileName);
            String json = new String(Utils.getInstance().getFileBytes(dataFile));
            Type listType = new TypeToken<List<Funko>>() {
            }.getType();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
            return (List<Funko>) gson.fromJson(json, listType);
        }).exceptionally(ex -> {
            throw new CompletionException(new ImportDataException(ex.getMessage()));
        });
    }
}
