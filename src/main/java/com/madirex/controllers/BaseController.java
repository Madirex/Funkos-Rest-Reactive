package com.madirex.controllers;

import com.madirex.exceptions.FunkoException;
import com.madirex.exceptions.FunkoNotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

/**
 * Controlador base
 *
 * @param <T> Entity
 */
public interface BaseController<T, J> {
    Flux<T> findAll() throws SQLException, FunkoNotFoundException;

    Mono<T> findById(J id) throws SQLException, FunkoNotFoundException;

    Flux<T> findByName(String name) throws SQLException, FunkoNotFoundException;

    Mono<T> save(T entity) throws SQLException, FunkoException;

    Mono<T> update(J id, T entity) throws SQLException, FunkoException;

    Mono<T> delete(J id) throws SQLException, FunkoException;
}
