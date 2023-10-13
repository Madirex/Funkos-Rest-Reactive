package com.madirex.controllers;

import com.madirex.exceptions.FunkoException;
import com.madirex.exceptions.FunkoNotFoundException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador base
 *
 * @param <T> Entity
 */
public interface BaseController<T> {
    CompletableFuture<List<T>> findAll() throws SQLException, FunkoNotFoundException;

    CompletableFuture<Optional<T>> findById(String id) throws SQLException, FunkoNotFoundException;

    CompletableFuture<List<T>> findByName(String name) throws SQLException, FunkoNotFoundException;

    CompletableFuture<Optional<T>> save(T entity) throws SQLException, FunkoException;

    CompletableFuture<Optional<T>> update(String id, T entity) throws SQLException, FunkoException;

    CompletableFuture<Optional<T>> delete(String id) throws SQLException, FunkoException;
}
