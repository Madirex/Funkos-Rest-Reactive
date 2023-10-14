package com.madirex;

import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.services.io.BackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase BackupServiceTest
 */
public class BackupServiceTest {
    private BackupService<List<Funko>> backupService;

    @BeforeEach
    public void setUp() {
        backupService = BackupService.getInstance();
    }

    /**
     * Test para comprobar que se puede exportar los datos
     */
    @Test
    public void testExportData() {
        File mockFile = mock(File.class);
        when(mockFile.toPath()).thenReturn(Paths.get("mock/path"));
        File mockDataDir = mock(File.class);
        when(mockDataDir.exists()).thenReturn(true);

        List<Funko> dataToExport = List.of(Funko.builder()
                .model(Model.ANIME)
                .name("Funko 1")
                .updateAt(LocalDateTime.now())
                .price(12.32)
                .releaseDate(LocalDate.now())
                .build());

        when(mockFile.exists()).thenReturn(true);
        doReturn(mockFile).when(mockFile).getParentFile();
        doReturn(mockDataDir).when(mockDataDir).getParentFile();

        CompletableFuture<Void> exportFuture = backupService.exportData(System.getProperty("user.dir") + File.separator + "data", "backup-test.json", dataToExport);

        exportFuture.join();
        assertNull(exportFuture.exceptionally(t -> null).join());
    }



    /**
     * Test para comprobar que se lanza una excepción cuando no se puede crear el directorio
     */
    @Test
    public void testExportDataDirectoryException() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);
        when(mockFile.toPath()).thenReturn(Paths.get("ruta/inexistente"));

        List<Funko> dataToExport = List.of(Funko.builder()
                .model(Model.ANIME)
                .name("Funko 1")
                .updateAt(LocalDateTime.now())
                .price(12.32)
                .releaseDate(LocalDate.now())
                .build());

        assertThrows(CompletionException.class, () -> {
            CompletableFuture<Void> exportFuture = backupService.exportData("ruta/inexistente",
                    "backup-test.json", dataToExport);
            exportFuture.join();
        });
    }

    /**
     * Test para comprobar que se puede importar los datos
     */
    @Test
    public void testImportData() {
        String testFilePath = System.getProperty("user.dir") + File.separator + "data" + File.separator;
        File testDataFile = new File(testFilePath);
        assertTrue(testDataFile.exists());
        CompletableFuture<List<Funko>> importFuture = backupService.importData(testFilePath, "backup-test.json");
        List<Funko> importedData = importFuture.join();
        assertNotNull(importedData);
    }

    /**
     * Test para comprobar que se lanza una excepción cuando no se puede leer el archivo
     */
    @Test
    public void testImportDataDirectoryException() {
        String nonExistentDirectory = "ruta/inexistente";
        String testFilePath = nonExistentDirectory + File.separator + "backup-test.json";

        File directory = new File(nonExistentDirectory);
        assertFalse(directory.exists());

        assertThrows(CompletionException.class, () -> {
            CompletableFuture<List<Funko>> importFuture = backupService.importData(testFilePath, "backup-test.json");
            importFuture.join();
        });
    }
}
