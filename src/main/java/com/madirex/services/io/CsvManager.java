package com.madirex.services.io;

import com.madirex.exceptions.CreateFolderException;
import com.madirex.exceptions.ReadCSVFailException;
import com.madirex.models.Funko;
import com.madirex.models.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Clase CsvManager que administra la exportación e importación de datos CSV
 */
public class CsvManager {

    private static CsvManager csvManagerInstance;

    /**
     * Constructor privado para evitar la creación de instancia
     * SINGLETON
     */
    private CsvManager() {
    }

    /**
     * Obtiene la instancia de CsvManager
     * SINGLETON
     *
     * @return Instancia de CsvManager
     */
    public static synchronized CsvManager getInstance() {
        if (csvManagerInstance == null) {
            csvManagerInstance = new CsvManager();
        }
        return csvManagerInstance;
    }

    /**
     * Lee un archivo CSV y lo convierte en un Optional de la lista de Funko
     *
     * @param path Ruta del archivo CSV
     * @return CompletableFuture de Optional de la lista de Funko
     * @throws ReadCSVFailException Excepción al leer el archivo CSV
     */
    public CompletableFuture<Optional<List<Funko>>> fileToFunkoList(String path) throws ReadCSVFailException {
        CompletableFuture<Optional<List<Funko>>> future = new CompletableFuture<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        CompletableFuture.runAsync(() -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
                        future.complete(Optional.of(reader.lines()
                                .map(line -> line.split(","))
                                .skip(1)
                                .map(values -> Funko.builder()
                                        .cod(UUID.fromString(values[0].chars().limit(36).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                                .toString()))
                                        .name(values[1])
                                        .model(Model.valueOf(values[2]))
                                        .price(Double.parseDouble(values[3]))
                                        .releaseDate(LocalDate.parse(values[4], formatter))
                                        .build()
                                )
                                .toList()));
                    } catch (IOException e) {
                        future.completeExceptionally(new ReadCSVFailException(e.getMessage()));
                    }
                }
        );
        return future;
    }

    /**
     * Crea la carpeta out si no existe
     *
     * @throws CreateFolderException Excepción al crear la carpeta
     */
    private void createOutFolderIfNotExists() throws CreateFolderException {
        try {
            Path folderPath = Paths.get("out");
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
        } catch (IOException e) {
            throw new CreateFolderException(e.getMessage());
        }
    }
}
