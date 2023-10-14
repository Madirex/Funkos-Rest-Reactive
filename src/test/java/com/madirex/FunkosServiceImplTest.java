package com.madirex;

import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.cache.FunkoCacheImpl;
import com.madirex.services.crud.funko.FunkoServiceImpl;
import com.madirex.services.io.BackupService;
import com.madirex.services.notifications.FunkoNotificationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    @Mock
    FunkoNotificationImpl notification;
    @InjectMocks
    FunkoServiceImpl service;

    /**
     * Test para FindAll
     */
    @Test
    void testFindAll() {
        var funkos = List.of(
                Funko.builder().name("test1").price(42.0).build(),
                Funko.builder().name("test2").price(42.24).build()
        );
        when(repository.findAll()).thenReturn(Flux.fromIterable(funkos));
        List<Funko> result = service.findAll().collectList().block();
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
     */
    @Test
    void testFindByName() {
        var funkos = List.of(Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build());
        when(repository.findByName("cuack")).thenReturn(Flux.fromIterable(funkos));
        List<Funko> result = service.findByName("cuack").collectList().block();
        assertAll("findByName",
                () -> assertEquals(result.get(0).getName(), funkos.get(0).getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.get(0).getPrice(), funkos.get(0).getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.get(0).getReleaseDate(), funkos.get(0).getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.get(0).getModel(), funkos.get(0).getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).findByName("cuack");
    }

    /**
     * Test para importData
     */
    @Test
    void testImportData() {
        String path = "testPath";
        String fileName = "testFile";
        List<Funko> testData = List.of(Funko.builder().build());
        Mockito.when(service.importData(path, fileName)).thenReturn(Flux.fromIterable(testData));
        Flux<Funko> result = service.importData(path, fileName);
        assertEquals(testData.size(), Objects.requireNonNull(result.collectList().block()).size());
    }

    /**
     * Test para exportData
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testExportData() throws SQLException {
        String path = "testPath";
        String fileName = "testFile";
        List<Funko> testData = List.of(Funko.builder().build());
        Mockito.when(service.findAll()).thenReturn(Flux.fromIterable(testData));
        Mockito.when(backupService.exportData(path, fileName, testData)).thenReturn(Mono.empty());
        service.exportData(path, fileName, testData).block();
        Mockito.verify(backupService, Mockito.times(1)).exportData(path, fileName, testData);
    }

    /**
     * Test para FindById
     */
    @Test
    void testFindById() throws NullPointerException{
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        var id = funko.getCod();
        when(repository.findById(id)).thenReturn(Mono.just(funko));
        var result = service.findById(id).block();
        assertAll("findByName",
                () -> assertEquals(result.getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).findById(id);
    }

    /**
     * Test para FindById caché
     */
    @Test
    void testFindByIdInCache() {
        UUID id = UUID.randomUUID();
        Funko cachedFunko = Funko.builder().name("Cached Funko").build();

        when(cache.put(id.toString(), cachedFunko).thenReturn(Mono.just(cachedFunko)));
        when(cache.get(String.valueOf(id))).thenReturn(Mono.just(cachedFunko));

        var funko = service.findById(id).block();
        assertEquals("Cached Funko", funko.getName());

        verify(repository, never()).findById(id);
    }

    /**
     * Test para Save
     */
    @Test
    void testSave() {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        when(repository.save(funko)).thenReturn(Mono.just(funko));
        var result = service.save(funko).block();
        assertAll("save",
                () -> assertEquals(result.getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).save(funko);
    }

    /**
     * Test para Update
     */
    @Test
    void testUpdate() {
        LocalDate date = LocalDate.now();
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(date).model(Model.DISNEY).build();
        when(repository.update(funko.getCod(), funko)).thenReturn(Mono.just(funko));
        var result = service.update(funko.getCod(), funko).block();
        assertAll("update",
                () -> assertEquals(result.getName(), funko.getName(), "El Funko no tiene el nombre esperado"),
                () -> assertEquals(result.getPrice(), funko.getPrice(), "El precio del Funko no es el esperado"),
                () -> assertEquals(result.getReleaseDate(), funko.getReleaseDate(), "La fecha de lanzamiento del Funko no es la esperada"),
                () -> assertEquals(result.getModel(), funko.getModel(), "El modelo del Funko no es el esperado")
        );
        verify(repository, times(1)).update(funko.getCod(), funko);
    }

    /**
     * Test para Delete
     */
    @Test
    void testDelete() {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        UUID id = funko.getCod();
        when(repository.findById(id)).thenReturn(Mono.empty());
        when(repository.delete(id)).thenReturn(Mono.empty());
        when(cache.remove(any(String.class))).thenReturn(Mono.empty());
        var result = service.delete(id).block();
        assertNotNull(result);
        verify(repository, times(1)).delete(id);
    }

    /**
     * Test para FunkoNotRemovedException
     */
    @Test
    void testDeleteNotRemovedException() {
        var funko = Funko.builder().name("cuack").price(12.42).releaseDate(LocalDate.now()).model(Model.DISNEY).build();
        UUID id = funko.getCod();
        when(repository.findById(id)).thenReturn(Mono.empty());
        when(repository.delete(id)).thenReturn(Mono.empty());
        when(cache.remove(any(String.class))).thenReturn(Mono.empty());
        assertThrows(FunkoNotRemovedException.class, () -> {
            service.delete(id).block();
        });
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
        FunkoServiceImpl instance1 = FunkoServiceImpl.getInstance(repository, cache, backupService, FunkoNotificationImpl.getInstance());
        FunkoServiceImpl instance2 = FunkoServiceImpl.getInstance(repository, cache, backupService, FunkoNotificationImpl.getInstance());
        assertSame(instance1, instance2);
    }

}