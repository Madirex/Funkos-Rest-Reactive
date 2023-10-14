package com.madirex.controllers;

import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.exceptions.FunkoNotSavedException;
import com.madirex.exceptions.FunkoNotValidException;
import com.madirex.models.funko.Funko;
import com.madirex.services.crud.funko.FunkoServiceImpl;
import com.madirex.validators.FunkoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Controlador de Funko
 */
public class FunkoController implements BaseController<Funko, UUID> {
    private static FunkoController funkoControllerInstance;
    private final Logger logger = LoggerFactory.getLogger(FunkoController.class);

    private final FunkoServiceImpl funkoService;

    /**
     * Constructor
     *
     * @param funkoService servicio de Funko
     */
    private FunkoController(FunkoServiceImpl funkoService) {
        this.funkoService = funkoService;
    }

    /**
     * Constructor de la clase
     *
     * @param funkoService servicio de Funko
     */
    public static synchronized FunkoController getInstance(FunkoServiceImpl funkoService) {
        if (funkoControllerInstance == null) {
            funkoControllerInstance = new FunkoController(funkoService);
        }
        return funkoControllerInstance;
    }


    /**
     * Busca todos los Funkos
     *
     * @return Funkos encontrados
     * @throws SQLException           si hay un error en la base de datos
     * @throws FunkoNotFoundException si no se encuentran Funkos
     */
    public Flux<Funko> findAll() throws SQLException, FunkoNotFoundException {
        logger.debug("FindAll");
        return funkoService.findAll();
    }

    /**
     * Busca un Funko por id
     *
     * @param id id del Funko
     * @return Funko encontrado
     * @throws SQLException           si hay un error en la base de datos
     * @throws FunkoNotFoundException si no se encuentra el Funko
     */
    public Mono<Funko> findById(UUID id) throws SQLException, FunkoNotFoundException {
        String msg = "FindById " + id;
        logger.debug(msg);
        return funkoService.findById(id);
    }

    /**
     * Busca Funkos por nombre
     *
     * @param name nombre del Funko
     * @return Funkos encontrados
     * @throws SQLException           si hay un error en la base de datos
     * @throws FunkoNotFoundException si no se encuentra el Funko
     */
    public Flux<Funko> findByName(String name) throws FunkoNotFoundException {
        String msg = "FindByName " + name;
        logger.debug(msg);
        return funkoService.findByName(name);
    }

    /**
     * Guarda un Funko
     *
     * @param funko Funko a guardar
     * @return Funko guardado
     * @throws SQLException           si hay un error en la base de datos
     * @throws FunkoNotSavedException si no se guarda el Funko
     * @throws FunkoNotValidException si el Funko no es válido
     */
    public Mono<Funko> save(Funko funko) throws SQLException, FunkoNotSavedException, FunkoNotValidException {
        String msg = "Save " + funko;
        logger.debug(msg);
        FunkoValidator.validate(funko);
        return funkoService.save(funko);
    }

    /**
     * Actualiza un Funko
     *
     * @param id    id del Funko
     * @param funko Funko a actualizar
     * @return Funko actualizado
     * @throws FunkoNotValidException si el Funko no es válido
     * @throws SQLException           si hay un error en la base de datos
     */
    public Mono<Funko> update(UUID id, Funko funko) throws FunkoNotValidException, SQLException {
        String msg = "Update " + funko;
        logger.debug(msg);
        FunkoValidator.validate(funko);
        return funkoService.update(id, funko);
    }

    /**
     * Elimina un Funko
     *
     * @param id id del Funko
     * @return Funko eliminado
     * @throws SQLException             si hay un error en la base de datos
     * @throws FunkoNotRemovedException si no se elimina el Funko
     */
    public Mono<Funko> delete(UUID id) throws SQLException, FunkoNotRemovedException {
        String msg = "Delete " + id;
        logger.debug(msg);
        return funkoService.delete(id);
    }

    /**
     * Exporta los datos de la base de datos a un archivo JSON
     *
     * @param url      url de la base de datos
     * @param fileName nombre del archivo
     * @throws SQLException si hay un error en la base de datos
     * @throws IOException  si hay un error en el archivo
     */
    public Mono<Void> exportData(String url, String fileName) throws SQLException, IOException, FunkoNotFoundException {
        return findAll()
                .collectList()
                .flatMap(dataList -> {
                    try {
                        var exp = funkoService.exportData(url, fileName, dataList);
                        return exp.onErrorResume(e -> {
                            logger.error("Error al exportar los datos: ", e);
                            return Mono.empty();
                        });
                    } catch (SQLException e) {
                        logger.error("Error al exportar los datos: ", e);
                        return Mono.empty();
                    }
                });
    }

    /**
     * Importa los datos de un archivo JSON a la base de datos
     *
     * @param url      url de la base de datos
     * @param fileName nombre del archivo
     */
    public Flux<Funko> importData(String url, String fileName) {
        var fl = funkoService.importData(url, fileName);
        return fl;
    }

    /**
     * Cierra el caché
     */
    public void shutdown() {
        funkoService.shutdown();
    }
}