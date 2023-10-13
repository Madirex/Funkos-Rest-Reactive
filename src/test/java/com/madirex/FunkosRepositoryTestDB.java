package com.madirex;

import com.madirex.models.Funko;
import com.madirex.models.Model;
import com.madirex.repositories.funko.FunkoRepository;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.crud.funko.IdGenerator;
import com.madirex.services.database.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de testeo para la clase FunkoRepository
 */
class FunkosRepositoryTestDB {

    private FunkoRepository funkoRepository;

    /**
     * Método que se ejecuta antes de cada test
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @BeforeEach
    void setUp() throws SQLException, ExecutionException, InterruptedException {
        funkoRepository = FunkoRepositoryImpl.getInstance(IdGenerator.getInstance(), DatabaseManager.getInstance());
        CompletableFuture<Void> clearData = funkoRepository.findAll()
                .thenComposeAsync(funkos -> {
                    CompletableFuture<Void> clearResult = CompletableFuture.completedFuture(null);
                    for (Funko e : funkos) {
                        clearResult = clearResult.thenAcceptAsync(unused -> {
                            try {
                                funkoRepository.delete(e.getCod().toString());
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                    return clearResult;
                });
        clearData.get();
    }

    /**
     * Método que se ejecuta después de cada test
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @AfterEach
    void tearDown() throws SQLException, ExecutionException, InterruptedException {
        CompletableFuture<Void> clearData = funkoRepository.findAll()
                .thenComposeAsync(funkos -> {
                    CompletableFuture<Void> clearResult = CompletableFuture.completedFuture(null);
                    for (Funko e : funkos) {
                        clearResult = clearResult.thenAcceptAsync(unused -> {
                            try {
                                funkoRepository.delete(e.getCod().toString());
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                    return clearResult;
                });
        clearData.get();
    }

    /**
     * Test para comprobar que se puede guardar un Funko
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testSaveFunko() throws SQLException, ExecutionException, InterruptedException {
        LocalDate date = LocalDate.now();
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.OTROS)
                .price(23.13)
                .releaseDate(date)
                .build();

        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.get();

        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        Funko saved = savedFunko.get();
        assertAll("Funko properties",
                () -> assertNotNull(saved.getCod(), "El ID no debe ser nulo"),
                () -> assertEquals(funko.getName(), saved.getName(), "Nombre coincide"),
                () -> assertEquals(funko.getModel(), saved.getModel(), "Modelo coincide"),
                () -> assertEquals(funko.getPrice(), saved.getPrice(), "Precio coincide"),
                () -> assertEquals(funko.getReleaseDate(), saved.getReleaseDate(), "Fecha de lanzamiento coincide")
        );
    }

    /**
     * Test para comprobar excepción SQLException de Save
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testSaveWithSQLException() throws SQLException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();

        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.join();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Optional<Funko>> saveFuture = funkoRepository.save(funko);

        saveFuture.exceptionally(throwable -> {
            assertTrue(throwable.getCause() instanceof SQLException, "Se lanzó una SQLException");
            return Optional.empty();
        }).join();
    }

    /**
     * Test para comprobar FindById
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindFunkoById() throws SQLException, ExecutionException, InterruptedException {
        LocalDate date = LocalDate.now();
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.OTROS)
                .price(23.12)
                .releaseDate(date)
                .build();
        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.get();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Optional<Funko>> foundFunkoFuture = funkoRepository.findById(savedFunko.get().getCod().toString());
        Optional<Funko> foundFunko = foundFunkoFuture.get();
        assertTrue(foundFunko.isPresent(), "El Funko se encontró con éxito");

        assertAll("Funko properties",
                () -> assertEquals(funko.getName(), foundFunko.get().getName(), "Nombre coincide"),
                () -> assertEquals(funko.getModel(), foundFunko.get().getModel(), "Modelo coincide"),
                () -> assertEquals(funko.getPrice(), foundFunko.get().getPrice(), "Precio coincide"),
                () -> assertEquals(funko.getReleaseDate(), foundFunko.get().getReleaseDate(), "Fecha de lanzamiento coincide"),
                () -> assertNotNull(foundFunko.get().getCod(), "El ID no debe ser nulo")
        );
    }

    /**
     * Test para comprobar que no se encuentra un Funko
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testFindByIdWithSQLException() throws SQLException {
        String id = "invalidId";
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();

        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.join();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Optional<Funko>> findByIdFuture = funkoRepository.findById(id);

        findByIdFuture.exceptionally(throwable -> {
            assertTrue(throwable.getCause() instanceof SQLException, "Se lanzó una SQLException");
            return Optional.empty();
        }).join();
    }


    /**
     * Test para comprobar FindAll
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindAllFunkos() throws SQLException, ExecutionException, InterruptedException {
        CompletableFuture<Optional<Funko>> savedFunko1Future = funkoRepository.save(Funko.builder()
                .name("test1")
                .model(Model.ANIME)
                .price(12.52)
                .releaseDate(LocalDate.now())
                .build());
        CompletableFuture<Optional<Funko>> savedFunko2Future = funkoRepository.save(Funko.builder()
                .name("test2")
                .model(Model.ANIME)
                .price(28.52)
                .releaseDate(LocalDate.now())
                .build());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(savedFunko1Future, savedFunko2Future);
        allOf.get();

        CompletableFuture<List<Funko>> allFunkosFuture = funkoRepository.findAll();
        List<Funko> allFunkos = allFunkosFuture.get();

        assertEquals(2, allFunkos.size(), "El número de Funkos en el repositorio coincide con el esperado");
    }

    /**
     * Test para comprobar FindByName
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testFindFunkosByName() throws SQLException, ExecutionException, InterruptedException {
        LocalDate date = LocalDate.now();
        Funko funko1 = Funko.builder()
                .name("test1")
                .model(Model.ANIME)
                .price(42.23)
                .releaseDate(date)
                .build();
        Funko funko2 = Funko.builder()
                .name("test1")
                .model(Model.OTROS)
                .price(81.23)
                .releaseDate(date)
                .build();
        CompletableFuture<Optional<Funko>> savedFunko1Future = funkoRepository.save(funko1);
        savedFunko1Future.join();
        CompletableFuture<Optional<Funko>> savedFunko2Future = funkoRepository.save(funko2);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(savedFunko1Future, savedFunko2Future);
        allOf.get();

        CompletableFuture<List<Funko>> foundFunkosFuture = funkoRepository.findByName("test1");
        List<Funko> foundFunkos = foundFunkosFuture.get();

        assertAll("Funkos encontrados",
                () -> assertNotNull(foundFunkos, "La lista de Funkos no debe ser nula"),
                () -> assertEquals(2, foundFunkos.size(), "El número de Funkos encontrados no coincide con el esperado"),
                () -> assertEquals(funko1.getName(), foundFunkos.get(0).getName(), "Nombre del primer Funko no coincide"),
                () -> assertEquals(funko1.getPrice(), foundFunkos.get(0).getPrice(), "Precio del primer Funko no coincide"),
                () -> assertEquals(funko1.getReleaseDate(), foundFunkos.get(0).getReleaseDate(), "Fecha de lanzamiento del primer Funko no coincide"),
                () -> assertEquals(funko1.getModel(), foundFunkos.get(0).getModel(), "Modelo del primer Funko no coincide"),
                () -> assertEquals(funko2.getName(), foundFunkos.get(1).getName(), "Nombre del segundo Funko no coincide"),
                () -> assertEquals(funko2.getPrice(), foundFunkos.get(1).getPrice(), "Precio del segundo Funko no coincide"),
                () -> assertEquals(funko2.getReleaseDate(), foundFunkos.get(1).getReleaseDate(), "Fecha de lanzamiento del segundo Funko no coincide"),
                () -> assertEquals(funko2.getModel(), foundFunkos.get(1).getModel(), "Modelo del segundo Funko no coincide")
        );
    }

    /**
     * Test para comprobar Update
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testUpdateFunko() throws SQLException, ExecutionException, InterruptedException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();
        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.get();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        savedFunko.get().setName("Updated");
        savedFunko.get().setPrice(42.43);

        CompletableFuture<Optional<Funko>> updateFuture = funkoRepository.update(savedFunko.get().getCod().toString(), savedFunko.get());
        updateFuture.get();

        CompletableFuture<Optional<Funko>> foundFunkoFuture = funkoRepository.findById(savedFunko.get().getCod().toString());
        Optional<Funko> foundFunko = foundFunkoFuture.get();
        assertTrue(foundFunko.isPresent(), "El Funko se encontró con éxito");

        assertAll("Funko actualizado",
                () -> assertEquals(savedFunko.get().getName(), foundFunko.get().getName(), "Nombre actualizado coincide"),
                () -> assertEquals(savedFunko.get().getPrice(), foundFunko.get().getPrice(), "Precio actualizado coincide")
        );
    }

    /**
     * Test para comprobar excepción SQLException de Update
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testUpdateFunkoWithSQLException() throws SQLException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();

        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.join();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Optional<Funko>> updateFuture = funkoRepository.update("invalidId", savedFunko.get());

        updateFuture.exceptionally(throwable -> {
            assertTrue(throwable.getCause() instanceof SQLException, "Se lanzó una SQLException");
            return Optional.empty();
        }).join();
    }

    /**
     * Test para comprobar Delete
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testDeleteFunko() throws SQLException, ExecutionException, InterruptedException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();
        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.get();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Boolean> deleteFuture = funkoRepository.delete(savedFunko.get().getCod().toString());
        boolean deletionResult = deleteFuture.get();

        assertTrue(deletionResult, "La eliminación del Funko se completó con éxito");

        CompletableFuture<Optional<Funko>> foundFunkoFuture = funkoRepository.findById(savedFunko.get().getCod().toString());
        Optional<Funko> foundFunko = foundFunkoFuture.get();

        assertAll("Funko eliminado",
                () -> assertFalse(foundFunko.isPresent(), "El Funko no debe encontrarse después de la eliminación")
        );
    }

    /**
     * Test para comprobar que no se pueda eliminar un Funko que no existe
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testDeleteFunkoNotExists() throws SQLException, ExecutionException, InterruptedException {
        CompletableFuture<Boolean> deletionFuture = funkoRepository.delete("cac4c061-20ec-4e87-ad3a-b1a7ea12facc");
        Boolean deleted = deletionFuture.get();
        assertFalse(deleted, "La eliminación de un Funko que no existe debe devolver false");
    }

    /**
     * Test para comprobar excepción SQLException de Delete
     *
     * @throws SQLException         Si hay un error en la base de datos
     * @throws ExecutionException   Si hay un error en la ejecución
     * @throws InterruptedException Si hay un error en la ejecución
     */
    @Test
    void testDeleteFunkoWithSQLException() throws SQLException, ExecutionException, InterruptedException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();
        CompletableFuture<Optional<Funko>> savedFunkoFuture = funkoRepository.save(funko);
        Optional<Funko> savedFunko = savedFunkoFuture.get();
        assertTrue(savedFunko.isPresent(), "El Funko se guardó con éxito");

        CompletableFuture<Boolean> deleteFuture = funkoRepository.delete("invalidId");

        deleteFuture.exceptionally(throwable -> {
            assertTrue(throwable.getCause() instanceof SQLException, "Se lanzó una SQLException");
            return false;
        }).join();
    }
}