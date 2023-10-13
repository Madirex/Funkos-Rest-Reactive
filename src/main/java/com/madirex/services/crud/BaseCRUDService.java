package com.madirex.services.crud;

import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.exceptions.FunkoNotSavedException;
import com.madirex.exceptions.FunkoNotValidException;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz que define las operaciones CRUD de BaseCRUDService
 */
public interface BaseCRUDService<I> {
    CompletableFuture<List<I>> findAll() throws SQLException;

    CompletableFuture<Optional<I>> findById(String id) throws SQLException, FunkoNotFoundException;

    CompletableFuture<Optional<I>> save(I item) throws SQLException, FunkoNotSavedException;

    CompletableFuture<Optional<I>> update(String id, I newI) throws SQLException, FunkoNotValidException;

    CompletableFuture<Boolean> delete(String id) throws SQLException, FunkoNotRemovedException;
}
