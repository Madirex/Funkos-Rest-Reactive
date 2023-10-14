package com.madirex.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.SQLException;

/**
 * Interfaz que define las operaciones CRUD sobre un repositorio
 *
 * @param <T> Tipo de la entidad
 * @param <I> Tipo del ID de la entidad
 */
public interface CRUDRepository<T, I> {
    Flux<T> findAll() throws SQLException;

    Mono<T> findById(I id) throws SQLException;

    Mono<T> save(T entity) throws SQLException;

    Mono<T> update(I id, T entity) throws SQLException;

    Mono<Boolean> delete(I id) throws SQLException;
}