package com.madirex.services.crud.funko;

import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.models.funko.Funko;
import com.madirex.services.crud.BaseCRUDService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

/**
 * Interfaz que define las operaciones CRUD de FunkoService
 */
public interface FunkoService<T> extends BaseCRUDService<Funko> {

    Flux<Funko> findByName(String name) throws SQLException, FunkoNotFoundException;

    Mono<Void> exportData(String path, String fileName, T data) throws SQLException;

    Flux<Funko> importData(String path, String fileName);
}