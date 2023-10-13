package com.madirex.services.crud.funko;

import com.madirex.exceptions.FunkoException;
import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.models.Funko;
import com.madirex.services.crud.BaseCRUDService;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interfaz que define las operaciones CRUD de FunkoService
 */
public interface FunkoService<T> extends BaseCRUDService<Funko> {

    CompletableFuture<List<Funko>> findByName(String nombre) throws SQLException, FunkoNotFoundException;

    CompletableFuture<Void> exportData(String path, String fileName, T data) throws SQLException;

    CompletableFuture<T> importData(String path, String fileName);
}