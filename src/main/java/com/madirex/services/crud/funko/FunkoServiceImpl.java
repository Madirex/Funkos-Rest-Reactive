package com.madirex.services.crud.funko;

import com.madirex.exceptions.DirectoryException;
import com.madirex.exceptions.FunkoNotFoundException;
import com.madirex.exceptions.FunkoNotRemovedException;
import com.madirex.exceptions.FunkoNotValidException;
import com.madirex.models.Funko;
import com.madirex.repositories.funko.FunkoRepositoryImpl;
import com.madirex.services.cache.FunkoCache;
import com.madirex.services.io.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Implementación de la interfaz FunkoService
 */
public class FunkoServiceImpl implements FunkoService<List<Funko>> {
    private static FunkoServiceImpl funkoServiceImplInstance;

    private final FunkoCache cache;
    private final Logger logger = LoggerFactory.getLogger(FunkoServiceImpl.class);
    private final FunkoRepositoryImpl funkoRepository;
    private final BackupService<List<Funko>> backupService;

    /**
     * Constructor de la clase
     *
     * @param funkoRepository Instancia de la clase FunkoRepository
     * @param cache           Instancia de la clase FunkoCache
     * @param backupService   Instancia de la clase BackupService
     */
    private FunkoServiceImpl(FunkoRepositoryImpl funkoRepository, FunkoCache cache, BackupService<List<Funko>> backupService) {
        this.funkoRepository = funkoRepository;
        this.cache = cache;
        this.backupService = backupService;
    }

    /**
     * Devuelve la instancia de la clase
     *
     * @param funkoRepository Instancia de la clase FunkoRepository
     * @param cache           Instancia de la clase FunkoCache
     * @param backupService   Instancia de la clase BackupService
     * @return Instancia de la clase
     */
    public static synchronized FunkoServiceImpl getInstance(FunkoRepositoryImpl funkoRepository,
                                                            FunkoCache cache,
                                                            BackupService<List<Funko>> backupService) {
        if (funkoServiceImplInstance == null) {
            funkoServiceImplInstance = new FunkoServiceImpl(funkoRepository, cache, backupService);
        }
        return funkoServiceImplInstance;
    }


    /**
     * Devuelve todos los elementos del repositorio
     *
     * @return Optional de la lista de elementos
     */
    @Override
    public CompletableFuture<List<Funko>> findAll() {
        logger.debug("Obteniendo todos los Funkos");
        return funkoRepository.findAll();
    }

    /**
     * Busca un elemento en el repositorio por su nombre
     *
     * @param name Nombre del elemento a buscar
     * @return Lista de elementos encontrados
     */
    @Override
    public CompletableFuture<List<Funko>> findByName(String name) {
        logger.debug("Obteniendo todos los Funkos ordenados por nombre");
        return funkoRepository.findByName(name)
                .thenComposeAsync(list -> {
                    if (list.isEmpty()) {
                        CompletableFuture<List<Funko>> future = new CompletableFuture<>();
                        future.completeExceptionally(new FunkoNotFoundException("No se encontraron Funkos con el nombre: " + name));
                        return future;
                    } else {
                        return CompletableFuture.completedFuture(list);
                    }
                });
    }

    /**
     * Realiza un backup de los datos del repositorio
     *
     * @param path     Ruta del directorio donde se guardará el backup
     * @param fileName Nombre del archivo del backup
     * @param data     Datos a guardar
     * @throws SQLException       Si hay un error en la base de datos
     * @throws DirectoryException El directorio no existe
     */
    @Override
    public CompletableFuture<Void> exportData(String path, String fileName, List<Funko> data) throws SQLException {
        return findAll().thenComposeAsync(s -> backupService.exportData(path, fileName, s));
    }

    /**
     * Importa los datos de un archivo JSON
     *
     * @param path     Ruta del directorio donde se guardará el backup
     * @param fileName Nombre del archivo del backup
     * @return Datos importados
     */
    @Override
    public CompletableFuture<List<Funko>> importData(String path, String fileName) {
        return backupService.importData(path, fileName);
    }

    /**
     * Devuelve un elemento del repositorio
     *
     * @param id Id del elemento a buscar
     * @return Optional del elemento encontrado
     */
    @Override
    public CompletableFuture<Optional<Funko>> findById(String id) throws SQLException {
        logger.debug("Obteniendo Funko por id");
        Funko funko = cache.get(id);
        if (funko != null) {
            logger.debug("Funko encontrado en caché");
            return CompletableFuture.supplyAsync(() -> Optional.of(funko));
        }
        logger.debug("Funko no encontrado en caché, buscando en base de datos");
        return funkoRepository.findById(id).thenApplyAsync(r -> {
            r.ifPresent(value -> cache.put(id, value));
            return r;
        });
    }

    /**
     * Guarda un elemento en el repositorio
     *
     * @param funko Elemento a guardar
     * @return Optional del elemento guardado
     */
    @Override
    public CompletableFuture<Optional<Funko>> save(Funko funko) {
        logger.debug("Guardando Funko");
        cache.put(funko.getCod().toString(), funko);
        return funkoRepository.save(funko);
    }

    /**
     * Actualiza un elemento del repositorio
     *
     * @param funkoId  Id del elemento a actualizar
     * @param newFunko Elemento con los nuevos datos
     * @return Optional del elemento actualizado
     */
    @Override
    public CompletableFuture<Optional<Funko>> update(String funkoId, Funko newFunko) throws SQLException, FunkoNotValidException {
        logger.debug("Actualizando Funko");
        cache.put(newFunko.getCod().toString(), newFunko);
        return funkoRepository.update(funkoId, newFunko);
    }

    /**
     * Borra un elemento del repositorio
     *
     * @param id Id del elemento a borrar
     * @return ¿Borrado?
     */
    @Override
    public CompletableFuture<Boolean> delete(String id) throws SQLException, FunkoNotRemovedException {
        logger.debug("Eliminando Funko");
        return funkoRepository.delete(id).thenApplyAsync(a -> {
            if (Boolean.TRUE.equals(a)) {
                cache.remove(id);
            }
            return a;
        });
    }

    /**
     * Cierra el caché
     */
    public void shutdown() {
        cache.shutdown();
    }
}