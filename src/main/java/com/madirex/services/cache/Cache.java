package com.madirex.services.cache;

/**
 * Interfaz para la implementación de una caché
 *
 * @param <K> Tipo de la clave
 * @param <V> Tipo del valor
 */
public interface Cache<K, V> {
    void put(K key, V value);

    V get(K key);

    void remove(K key);

    void clear();

    void shutdown();
}