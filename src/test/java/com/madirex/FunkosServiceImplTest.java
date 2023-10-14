package com.madirex;

import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.exceptions.FunkoNotValidException;
import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.cache.FunkoCacheImpl;
import com.madirex.services.crud.funko.FunkoServiceImpl;
import com.madirex.services.io.BackupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Clase de testeo para la clase FunkoService
 */
@ExtendWith(MockitoExtension.class)
class FunkosServiceImplTest {

    @Mock
    FunkoRepositoryImpl repository;
    @Mock
    FunkoCacheImpl cache;
    @Mock
    BackupService backupService;
    @InjectMocks
    FunkoServiceImpl service;

    /**
     * Test para FindAll
     *
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindAll() throws ExecutionException, InterruptedException {
        var funkos = List.of(
                Funko.builder().name("test1").price(42.0).build(),
                Funko.builder().name("test2").price(42.24).build()
        );
        when(repository.findAll()).thenReturn(CompletableFuture.completedFuture(funkos));
        List<Funko> result = service.findAll().get();
        assertAll("findAll",
                () -> assertEquals(2, result.size(), "No se han recuperado 2 Funkos"),
                () -> assertEquals("test1", result.get(0).getName(), "El primer Funko no es el esperado"),
                () -> assertEquals("test2", result.get(1).getName(), "El segundo Funko no es el esperado"),
                () -> assertEquals(42.0, result.get(0).getPrice(), "La calificación del primer Funko no es la esperada"),
                () -> assertEquals(42.24, result.get(1).getPrice(), "La calificación del segundo Funko no es la esperada")
        );
        verify(repository, times(1)).findAll();
    }

    /**
     * Test para FindByName
     *
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindByName() throws ExecutionException, InterruptedException {
        var funkos = List.of(Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build());
        when(repository.findByName("cuack")).thenReturn(CompletableFuture.completedFuture(funkos));
        List<Funko> result = service.findByName("cuack").get();
        assertAll("findByName",
                () -> assertEquals(result.get(0).getName(), funkos.get(0).getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.get(0).getPrice(), funkos.get(0).getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.get(0).getReleaseDate(), funkos.get(0).getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.get(0).getModel(), funkos.get(0).getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).findByName("cuack");
    }

    /**
     * Test FindByName cuando no se encuentra ningún Funko
     */
    @Test
    void testFindByNameEmptyList() {
        String name = "No existe el Funko";

        Mockito.when(repository.findByName(name)).thenReturn(CompletableFuture.completedFuture(List.of()));

        assertThrows(FunkoNotFoundException.class, () -> {
            CompletableFuture<List<Funko>> result = service.findByName(name);
            try {
                result.get();
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

    /**
     * Test para importData
     *
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testImportData() throws ExecutionException, InterruptedException {
        String path = "testPath";
        String fileName = "testFile";
        List<Funko> testData = List.of(Funko.builder().build());
        Mockito.when(service.importData(path, fileName)).thenReturn(CompletableFuture.completedFuture(testData));
        CompletableFuture<List<Funko>> result = service.importData(path, fileName);
        List<Funko> importedData = result.get();
        assertEquals(testData.size(), importedData.size());
    }

    /**
     * Test para exportData
     *
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     * @throws SQLException         Si hay un error en la base de datos
     */
    @Test
    void testExportData() throws ExecutionException, InterruptedException, SQLException {
        String path = "testPath";
        String fileName = "testFile";
        List<Funko> testData = List.of(Funko.builder().build());
        Mockito.when(service.findAll()).thenReturn(CompletableFuture.completedFuture(testData));
        Mockito.when(backupService.exportData(path, fileName, testData)).thenReturn(CompletableFuture.completedFuture(null));
        CompletableFuture<Void> result = service.exportData(path, fileName, testData);
        result.get();
        Mockito.verify(backupService, Mockito.times(1)).exportData(path, fileName, testData);
    }

    /**
     * Test para FindById
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindById() throws SQLException, ExecutionException, InterruptedException {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        String id = funko.getCod().toString();
        when(repository.findById(id)).thenReturn(CompletableFuture.completedFuture(Optional.of(funko)));
        var result = service.findById(id).get();
        assertTrue(result.isPresent());
        assertAll("findById",
                () -> assertEquals(result.get().getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.get().getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.get().getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).findById(id);
    }

    /**
     * Test para FindById caché
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindByIdInCache() throws SQLException, ExecutionException, InterruptedException {
        String id = "testId";
        Funko cachedFunko = Funko.builder().name("Cached Funko").build();

        Mockito.when(cache.get(id)).thenReturn(cachedFunko);

        CompletableFuture<Optional<Funko>> result = service.findById(id);

        Optional<Funko> foundFunko = result.get();
        assertTrue(foundFunko.isPresent());
        assertEquals("Cached Funko", foundFunko.get().getName());
        Mockito.verify(repository, Mockito.never()).findById(id);
    }

    /**
     * Test para Save
     *
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testSave() throws ExecutionException, InterruptedException {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        when(repository.save(funko)).thenReturn(CompletableFuture.completedFuture(Optional.of(funko)));
        var result = service.save(funko).get();
        assertTrue(result.isPresent());
        assertAll("save",
                () -> assertEquals(result.get().getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.get().getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.get().getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).save(funko);
    }

    /**
     * Test para Update
     *
     * @throws SQLException           Si hay un error en la base de datos
     * @throws ExecutionException     Si hay un error en la ejecución
     * @throws InterruptedException   Si hay un error en la ejecución
     * @throws FunkoNotValidException Si el Funko no es válido
     */
    @Test
    void testUpdate() throws SQLException, ExecutionException, InterruptedException, FunkoNotValidException {
        LocalDate date = LocalDate.now();
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(date).model(Model.DISNEY).build();
        String id = funko.getCod().toString();
        when(repository.update(id, funko)).thenReturn(CompletableFuture.completedFuture(Optional.of(funko)));
        var result = service.update(id, funko).get();
        assertTrue(result.isPresent());
        assertAll("update",
                () -> assertEquals(result.get().getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.get().getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.get().getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.get().getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).update(id, funko);
    }

    /**
     * Test para Delete
     *
     * @throws SQLException             Si hay un error en la base de datos
     * @throws ExecutionException       Si hay un error en la ejecución
     * @throws InterruptedException     Si hay un error en la ejecución
     * @throws FunkoNotRemovedException Si no se elimina el Funko
     */
    @Test
    void testDelete() throws SQLException, ExecutionException, InterruptedException, FunkoNotRemovedException {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        String id = funko.getCod().toString();
        when(repository.delete(id)).thenReturn(CompletableFuture.completedFuture(true));
        var result = service.delete(id).get();
        assertTrue(result);
        verify(repository, times(1)).delete(id);
    }

    /**
     * Test para Shutdown
     */
    @Test
    void testShutdown() {
        service.shutdown();
        verify(cache, times(1)).shutdown();
    }

    /**
     * Test para GetInstance
     */
    @Test
    void testGetInstance() {
        FunkoServiceImpl instance1 = FunkoServiceImpl.getInstance(repository, cache, backupService);
        FunkoServiceImpl instance2 = FunkoServiceImpl.getInstance(repository, cache, backupService);
        assertSame(instance1, instance2);
    }

}