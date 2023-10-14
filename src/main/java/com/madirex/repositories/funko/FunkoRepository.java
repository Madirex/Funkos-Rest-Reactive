package com.madirex.repositories.funko;

import com.madirex.models.Funko;
import com.madirex.repositories.CRUDRepository;
import reactor.core.publisher.Flux;

import java.sql.SQLException;

/**
 * Interfaz que define las operaciones CRUD de FunkoRepository
 */
public interface FunkoRepository extends CRUDRepository<Funko, String> {
    Flux<Funko> findByName(String name) throws SQLException;
}
