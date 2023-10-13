package com.madirex.repositories.funko;

import com.madirex.models.Funko;
import com.madirex.models.Model;
import com.madirex.services.crud.funko.IdGenerator;
import com.madirex.services.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación de la interfaz FunkoRepository
 */
public class FunkoRepositoryImpl implements FunkoRepository {
    private static FunkoRepositoryImpl funkoRepositoryImplInstance;
    private final IdGenerator idGenerator;
    private final DatabaseManager database;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);

    /**
     * Constructor de la clase
     *
     * @param idGenerator Instancia de la clase IdGenerator
     * @param database    Instancia de la clase DatabaseManager
     */
    private FunkoRepositoryImpl(IdGenerator idGenerator, DatabaseManager database) {
        this.idGenerator = idGenerator;
        this.database = database;
    }

    /**
     * Devuelve la instancia de la clase
     *
     * @return Instancia de la clase
     */
    public static synchronized FunkoRepositoryImpl getInstance(IdGenerator idGenerator, DatabaseManager database) {
        if (funkoRepositoryImplInstance == null) {
            funkoRepositoryImplInstance = new FunkoRepositoryImpl(idGenerator, database);
        }
        return funkoRepositoryImplInstance;
    }

    /**
     * Devuelve todos los elementos del repositorio
     *
     * @return Optional de la lista de elementos
     */
    @Override
    public CompletableFuture<List<Funko>> findAll() {
        return CompletableFuture.supplyAsync(() -> {
            List<Funko> list = new ArrayList<>();
            var sql = "SELECT * FROM funko";
            try {
                database.beginTransaction();
                var res = database.select(sql);
                if (res.isPresent()) {
                    var resGet = res.get();
                    while (resGet.next()) {
                        list.add(Funko.builder()
                                .cod(UUID.fromString(resGet.getString("cod")))
                                .myId(resGet.getLong("myId"))
                                .name(resGet.getString("nombre"))
                                .model(Model.valueOf(resGet.getString("modelo")))
                                .price(resGet.getDouble("precio"))
                                .releaseDate(resGet.getDate("fecha_lanzamiento").toLocalDate())
                                .updateAt(resGet.getTimestamp("updated_at").toLocalDateTime())
                                .build());
                    }
                }
                database.commit();
            } catch (SQLException e) {
                String str = "Error en el findAll: " + e;
                logger.error(str);
            }
            return list;
        });
    }

    /**
     * Busca un elemento en el repositorio por su id
     *
     * @param id Id del elemento a buscar
     * @return Optional del elemento encontrado
     */
    @Override
    public CompletableFuture<Optional<Funko>> findById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Funko> optReturn = Optional.empty();
            try {
                database.beginTransaction();
                var sql = "SELECT * FROM funko WHERE cod = ?";
                var res = database.select(sql, id).orElseThrow();
                if (res.next()) {
                    optReturn = Optional.of(Funko.builder()
                            .cod(UUID.fromString(res.getString("cod")))
                            .myId(res.getLong("myId"))
                            .name(res.getString("nombre"))
                            .model(Model.valueOf(res.getString("modelo")))
                            .price(res.getDouble("precio"))
                            .releaseDate(res.getDate("fecha_lanzamiento").toLocalDate())
                            .updateAt(res.getTimestamp("updated_at").toLocalDateTime())
                            .build());
                }
                database.commit();
            } catch (SQLException e) {
                String str = "Error en el findById: " + e;
                logger.error(str);
            }
            return optReturn;
        });
    }

    /**
     * Guarda un elemento en el repositorio
     *
     * @param entity Elemento a guardar
     * @return Optional del elemento guardado
     */
    @Override
    public CompletableFuture<Optional<Funko>> save(Funko entity) {
        return CompletableFuture.supplyAsync(() -> {
            entity.setUpdateAt(LocalDateTime.now());
            var sql = "INSERT INTO funko (cod, myId, nombre, modelo, precio, fecha_lanzamiento, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                database.beginTransaction();
                database.insertAndGetKey(sql, entity.getCod().toString(),
                        idGenerator.newId(),
                        entity.getName(),
                        entity.getModel().toString(),
                        entity.getPrice(),
                        entity.getReleaseDate(),
                        LocalDateTime.now(),
                        entity.getUpdateAt());
                database.commit();
            } catch (SQLException e) {
                String str = "Error en el save: " + e;
                logger.error(str);
            }
            return Optional.of(entity);
        });
    }

    /**
     * Borra un elemento del repositorio
     *
     * @param id Id del elemento a borrar
     * @return ¿Borrado?
     */
    @Override
    public CompletableFuture<Boolean> delete(String id) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            var sql = "DELETE FROM funko WHERE cod= ?";
            try {
                database.beginTransaction();
                var rs = database.delete(sql, id);
                database.commit();
                return (rs == 1);
            } catch (SQLException e) {
                String str = "Error en el delete: " + e;
                logger.error(str);
            }
            return false;
        });
    }

    /**
     * Actualiza un elemento del repositorio
     *
     * @param id     Id del elemento a actualizar
     * @param entity Elemento con los nuevos datos
     * @return Optional del elemento actualizado
     */
    @Override
    public CompletableFuture<Optional<Funko>> update(String id, Funko entity) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            entity.setUpdateAt(LocalDateTime.now());
            var sql = "UPDATE funko SET myId = ?, nombre = ?, modelo = ?, precio = ?, fecha_lanzamiento = ?, " +
                    "updated_at = ? WHERE cod = ?";
            try {
                database.beginTransaction();
                database.update(sql,
                        entity.getMyId(),
                        entity.getName(),
                        entity.getModel().toString(),
                        entity.getPrice(),
                        entity.getReleaseDate(),
                        entity.getUpdateAt(),
                        id);
                database.commit();
            } catch (SQLException e) {
                String str = "Error en el update: " + e;
                logger.error(str);
            }
            return Optional.of(entity);
        });
    }

    /**
     * Busca un elemento en el repositorio por su nombre
     *
     * @param name Nombre del elemento a buscar
     * @return Lista de elementos encontrados
     */
    @Override
    public CompletableFuture<List<Funko>> findByName(String name) {
        return findAll().thenApplyAsync(allFunkos -> allFunkos.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name.toLowerCase()))
                .toList());
    }
}