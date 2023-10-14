package com.madirex.services.cache;

import com.madirex.models.funko.Funko;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementación de la interfaz FunkoCache
 */
public class FunkoCacheImpl implements FunkoCache {
    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
    private final int maxSize;
    private final long secondsToClear;
    private final Map<String, Funko> cache;
    @Getter
    private final ScheduledExecutorService cleaner;


    /**
     * Constructor de la clase
     *
     * @param maxSize        tamaño máximo de la caché
     * @param secondsToClear minutos para limpiar la caché
     */
    public FunkoCacheImpl(int maxSize, long secondsToClear) {
        this.maxSize = maxSize;
        this.secondsToClear = secondsToClear;
        this.cache = new LinkedHashMap<String, Funko>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Funko> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Asigna un Funko a la caché
     *
     * @param key   Id
     * @param value Funko
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> put(String key, Funko value) {
        String str = "Añadiendo Funko a caché con ID: " + key + " y valor: " + value;
        logger.debug(str);
        return Mono.fromRunnable(() -> cache.put(key, value));
    }

    /**
     * Devuelve el Funko de la caché
     *
     * @param key Id
     * @return Funko
     */
    @Override
    public Mono<Funko> get(String key) {
        String str = "Obteniendo Funko de caché con ID: " + key;
        logger.debug(str);
        return Mono.defer(() -> {
            Funko funko = cache.get(key);
            if (funko != null) {
                return Mono.just(funko);
            } else {
                return Mono.empty();
            }
        });
    }

    /**
     * Elimina el Funko de la caché
     *
     * @param key Id
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> remove(String key) {
        String str = "Eliminando Funko de caché con ID: " + key;
        logger.debug(str);
        return Mono.fromRunnable(() -> cache.remove(key));
    }

    /**
     * Elimina los Funkos de la caché que hayan caducado si llevan más de secondsToClear segundos sin ser modificados
     */
    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUpdateAt().plusSeconds(secondsToClear).isBefore(LocalDateTime.now());
            if (shouldRemove) {
                logger.debug("Eliminado por caducidad Funko de caché con ID: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

    /**
     * Elimina todos los Funkos de la caché
     */
    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
