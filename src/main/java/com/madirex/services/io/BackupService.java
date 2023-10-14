package com.madirex.services.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.madirex.exceptions.DirectoryException;
import com.madirex.exceptions.ImportDataException;
import com.madirex.models.funko.Funko;
import com.madirex.utils.LocalDateAdapter;
import com.madirex.utils.LocalDateTimeAdapter;
import com.madirex.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    public Mono<Void> exportData(String path, String fileName, T data) {
        return Mono.defer(() -> {
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
                return Mono.empty();
            } else {
                return Mono.error(new DirectoryException("No se creará el backup."));
            }
        });
    }

    /**
     * Importa los datos de un archivo JSON
     *
     * @param path     Ruta del archivo JSON
     * @param fileName Nombre del archivo JSON
     * @return Datos importados
     */
    public Flux<Funko> importData(String path, String fileName) {
        return Mono.fromCallable(() -> {
                    File folder = new File(path + File.separator);
                    if (!folder.exists()) {
                        throw new DirectoryException("No se creará el backup.");
                    }
                    File dataFile = new File(path + File.separator + fileName);
                    String json = new String(Utils.getInstance().getFileBytes(dataFile));
                    Type listType = new TypeToken<List<Funko>>() {
                    }.getType();
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                            .create();
                    List<Funko> funkoList = gson.fromJson(json, listType);
                    return funkoList;
                })
                .flatMapMany(Flux::fromIterable)
                .onErrorMap(ex -> new ImportDataException(ex.getMessage()));
    }
}
