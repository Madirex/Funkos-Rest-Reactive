package com.madirex.services.crud.funko;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clase IdGenerator
 */
public class IdGenerator {

    private static IdGenerator idGeneratorInstance;
    private int counter = 0;
    private Lock lock = new ReentrantLock();

    /**
     * Constructor de la clase
     */
    private IdGenerator() {
    }

    /**
     * Devuelve la instancia de la clase
     *
     * @return Instancia de la clase
     */
    public static synchronized IdGenerator getInstance() {
        if (idGeneratorInstance == null) {
            idGeneratorInstance = new IdGenerator();
        }
        return idGeneratorInstance;
    }

    /**
     * Genera un ID
     * Aumenta a 1 el contador (counter) y retorna el valor actual
     *
     * @return Valor del contador actual
     */
    public int newId() {
        lock.lock();
        try {
            counter++;
            return counter;
        } finally {
            lock.unlock();
        }
    }
}