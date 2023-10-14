package com.madirex.repositories.funko;

import com.madirex.models.funko.Funko;
import com.madirex.models.funko.Model;
import com.madirex.services.crud.funko.IdGenerator;
import com.madirex.services.database.DatabaseManager;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementación de la interfaz FunkoRepository
 */
public class FunkoRepositoryImpl implements FunkoRepository {
    private static FunkoRepositoryImpl funkoRepositoryImplInstance;
    private final IdGenerator idGenerator;
    private final ConnectionPool connectionFactory;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);

    /**
     * Constructor de la clase
     *
     * @param idGenerator Instancia de la clase IdGenerator
     * @param database    Instancia de la clase DatabaseManager
     */
    private FunkoRepositoryImpl(IdGenerator idGenerator, DatabaseManager database) {
        this.idGenerator = idGenerator;
        this.connectionFactory = database.getPool();
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
     * @return Lista de elementos
     */
    @Override
    public Flux<Funko> findAll() {
        var sql = "SELECT * FROM funko";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                {
                                    Double price = row.get("precio", BigDecimal.class).doubleValue();
                                    return Funko.builder()
                                            .cod(UUID.fromString(Objects.requireNonNull(row.get("cod", UUID.class)).toString()))
                                            .myId(row.get("myId", Long.class))
                                            .name(row.get("nombre", String.class))
                                            .model(Model.valueOf(row.get("modelo", String.class)))
                                            .price(price != null ? price : 0.0)
                                            .releaseDate(row.get("fecha_lanzamiento", LocalDate.class))
                                            .updateAt(row.get("updated_at", LocalDateTime.class))
                                            .build();
                                }
                        )),
                Connection::close
        );
    }

    /**
     * Busca un elemento en el repositorio por su id
     *
     * @param id Id del elemento a buscar
     * @return Elemento encontrado
     */
    @Override
    public Mono<Funko> findById(UUID id) {
        var sql = "SELECT * FROM funko WHERE cod = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        {
                            Double price = row.get("precio", BigDecimal.class).doubleValue();
                            return Funko.builder()
                                    .cod(Objects.requireNonNull(row.get("cod", UUID.class)))
                                    .myId(row.get("myId", Long.class))
                                    .name(row.get("nombre", String.class))
                                    .model(Model.valueOf(row.get("modelo", String.class)))
                                    .price(price != null ? price : 0.0)
                                    .releaseDate(row.get("fecha_lanzamiento", LocalDate.class))
                                    .updateAt(row.get("updated_at", LocalDateTime.class))
                                    .build();
                        }
                ))),
                Connection::close
        );
    }

    /**
     * Guarda un elemento en el repositorio
     *
     * @param entity Elemento a guardar
     * @return Elemento guardado
     */
    @Override
    public Mono<Funko> save(Funko entity) {
        var sql = "INSERT INTO funko (cod, myId, nombre, modelo, precio, fecha_lanzamiento, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, entity.getCod())
                        .bind(1, idGenerator.newId())
                        .bind(2, entity.getName())
                        .bind(3, entity.getModel().toString())
                        .bind(4, entity.getPrice())
                        .bind(5, entity.getReleaseDate())
                        .bind(6, LocalDateTime.now())
                        .bind(7, entity.getUpdateAt())
                        .execute()
                ).then(Mono.just(entity)),
                Connection::close
        );
    }

    /**
     * Borra un elemento del repositorio
     *
     * @param id Id del elemento a borrar
     * @return ¿Borrado?
     */
    @Override
    public Mono<Boolean> delete(UUID id) {
        var sql = "DELETE FROM funko WHERE cod= ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                                .bind(0, id)
                                .execute()
                        ).flatMapMany(Result::getRowsUpdated)
                        .hasElements(),
                Connection::close
        );
    }

    /**
     * Actualiza un elemento del repositorio
     *
     * @param id     Id del elemento a actualizar
     * @param entity Elemento con los nuevos datos
     * @return Elemento actualizado
     */
    @Override
    public Mono<Funko> update(UUID id, Funko entity) {
        var sql = "UPDATE funko SET myId = ?, nombre = ?, modelo = ?, precio = ?, fecha_lanzamiento = ?, " +
                "updated_at = ? WHERE cod = ?";

        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, entity.getMyId())
                        .bind(1, entity.getName())
                        .bind(2, entity.getModel().toString())
                        .bind(3, entity.getPrice())
                        .bind(4, entity.getReleaseDate())
                        .bind(5, entity.getUpdateAt())
                        .bind(6, id)
                        .execute()
                ).then(Mono.just(entity)),
                Connection::close
        );
    }

    /**
     * Busca un elemento en el repositorio por su nombre
     *
     * @param name Nombre del elemento a buscar
     * @return Lista de elementos encontrados
     */
    @Override
    public Flux<Funko> findByName(String name) {
        return findAll()
                .filter(funko -> funko.getName().equalsIgnoreCase(name.toLowerCase()));
    }
}