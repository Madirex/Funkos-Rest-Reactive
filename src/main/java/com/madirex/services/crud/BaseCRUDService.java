package com.madirex.services.crud;

import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.exceptions.FunkoNotSavedException;
import com.madirex.exceptions.FunkoNotValidException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

/**
 * Interfaz que define las operaciones CRUD de BaseCRUDService
 */
public interface BaseCRUDService<I> {
    Flux<I> findAll() throws SQLException;

    Mono<I> findById(String id) throws SQLException, FunkoNotFoundException;

    Mono<I> save(I item) throws SQLException, FunkoNotSavedException;

    Mono<I> update(String id, I newI) throws SQLException, FunkoNotValidException;

    Mono<I> delete(String id) throws SQLException, FunkoNotRemovedException;
}
