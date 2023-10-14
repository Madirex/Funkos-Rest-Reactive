package com.madirex.services.cache;

import reactor.core.publisher.Mono;

/**
 * Interfaz para la implementación de una caché
 *
 * @param <K> Tipo de la clave
 * @param <V> Tipo del valor
 */
public interface Cache<K, V> {
    Mono<Void> put(K key, V value);

    Mono<V> get(K key);

    Mono<Void> remove(K key);

    void clear();

    void shutdown();
}