package com.madirex;

import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de testeo para la clase FunkoRepository
 */
class FunkosRepositoryTestDB {

    private FunkoRepository funkoRepository;

    /**
     * Método que se ejecuta antes de cada test
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @BeforeEach
    void setUp() throws SQLException {
        funkoRepository = FunkoRepositoryImpl.getInstance(IdGenerator.getInstance(), DatabaseManager.getInstance());
        var list = funkoRepository.findAll().collectList().block();
        list.forEach(e -> {
            try {
                funkoRepository.delete(e.getCod()).block();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Método que se ejecuta después de cada test
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @AfterEach
    void tearDown() throws SQLException {
        var list = funkoRepository.findAll().collectList().block();
        list.forEach(e -> {
            try {
                funkoRepository.delete(e.getCod()).block();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Test para comprobar que se puede guardar un Funko
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testSaveFunko() throws SQLException {
        LocalDate date = LocalDate.now();
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.OTROS)
                .price(23.13)
                .releaseDate(date)
                .build();
        var saved = funkoRepository.save(funko).block();
        assertAll("Funko properties",
                () -> assertNotNull(saved.getCod(), "El ID no debe ser nulo"),
                () -> assertEquals(funko.getName(), saved.getName(), "Nombre coincide"),
                () -> assertEquals(funko.getModel(), saved.getModel(), "Modelo coincide"),
                () -> assertEquals(funko.getPrice(), saved.getPrice(), "Precio coincide"),
                () -> assertEquals(funko.getReleaseDate(), saved.getReleaseDate(), "Fecha de lanzamiento coincide")
        );
    }

    /**
     * Test para comprobar FindById
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testFindFunkoById() throws SQLException {
        LocalDate date = LocalDate.now();
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.OTROS)
                .price(23.12)
                .releaseDate(date)
                .build();
        var foundFunko = funkoRepository.save(funko).block();
        assertAll("Funko properties",
                () -> assertEquals(funko.getName(), foundFunko.getName(), "Nombre coincide"),
                () -> assertEquals(funko.getModel(), foundFunko.getModel(), "Modelo coincide"),
                () -> assertEquals(funko.getPrice(), foundFunko.getPrice(), "Precio coincide"),
                () -> assertEquals(funko.getReleaseDate(), foundFunko.getReleaseDate(), "Fecha de lanzamiento coincide"),
                () -> assertNotNull(foundFunko.getCod(), "El ID no debe ser nulo")
        );
    }


    /**
     * Test para comprobar FindAll
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testFindAllFunkos() throws SQLException {
        funkoRepository.save(Funko.builder()
                .name("test1")
                .model(Model.ANIME)
                .price(12.52)
                .releaseDate(LocalDate.now())
                .build()).block();
        funkoRepository.save(Funko.builder()
                .name("test2")
                .model(Model.ANIME)
                .price(28.52)
                .releaseDate(LocalDate.now())
                .build()).block();

        assertEquals(2, funkoRepository.findAll().collectList().block().size(), "El número de Funkos debe de coincidir con el esperado");
    }

    /**
     * Test para comprobar FindByName
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testFindFunkosByName() throws SQLException {
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

        funkoRepository.save(funko1).block();
        funkoRepository.save(funko2).block();
        List<Funko> foundFunkos = funkoRepository.findByName("test1").collectList().block();

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
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testUpdateFunko() throws SQLException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();
        var savedFunko = funkoRepository.save(funko).block();
        assertNotNull(savedFunko);
        funko.setName("Test2");
        funko.setModel(Model.DISNEY);
        funko.setPrice(23.23);
        funko.setReleaseDate(LocalDate.now());
        funkoRepository.update(funko.getCod(), savedFunko).block();
        var foundFunko = funkoRepository.findById(savedFunko.getCod()).block();
        assertAll("Funko actualizado",
                () -> assertEquals(funko.getName(), foundFunko.getName(), "Nombre actualizado debe coincidir"),
                () -> assertEquals(funko.getPrice(), foundFunko.getPrice(), "Precio actualizado debe coincidir"),
                () -> assertEquals(funko.getReleaseDate(), foundFunko.getReleaseDate(), "Fecha de lanzamiento debe coincidir"),
                () -> assertEquals(funko.getModel(), foundFunko.getModel(), "Modelo debe coincidir"),
                () -> assertEquals(funko.getName(), foundFunko.getName(), "Nombre debe coincidir"),
                () -> assertEquals(funko.getPrice(), foundFunko.getPrice(), "Precio debe coincidir"),
                () -> assertEquals(funko.getReleaseDate(), foundFunko.getReleaseDate(), "Fecha de lanzamiento debe coincidir"),
                () -> assertEquals(funko.getModel(), foundFunko.getModel(), "Modelo debe coincidir")
        );
    }

    /**
     * Test para comprobar Delete
     *
     * @throws SQLException Si hay un error en la base de datos
     */
    @Test
    void testDeleteFunko() throws SQLException {
        Funko funko = Funko.builder()
                .name("Test")
                .model(Model.ANIME)
                .price(4.42)
                .releaseDate(LocalDate.now())
                .build();
        var savedFunko = funkoRepository.save(funko).block();
        assertNotNull(savedFunko);

        var deleteFuture = funkoRepository.delete(savedFunko.getCod()).block();
        assertTrue(deleteFuture, "La eliminación del Funko se completó con éxito");

        var foundFunko = funkoRepository.findById(savedFunko.getCod()).block();

        assertAll("Funko eliminado",
                () -> assertNull(foundFunko, "El Funko no debe encontrarse después de la eliminación")
        );
    }

}