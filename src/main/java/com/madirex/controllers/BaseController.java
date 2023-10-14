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
public interface BaseController<T> {
    Flux<T> findAll() throws SQLException, FunkoNotFoundException;

    Mono<T> findById(String id) throws SQLException, FunkoNotFoundException;

    Flux<T> findByName(String name) throws SQLException, FunkoNotFoundException;

    Mono<T> save(T entity) throws SQLException, FunkoException;

    Mono<T> update(String id, T entity) throws SQLException, FunkoException;

    Mono<T> delete(String id) throws SQLException, FunkoException;
}
