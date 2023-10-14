package com.madirex;

import com.madirex.models.funko.Funko;
import com.madirex.services.cache.FunkoCacheImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Clase de test para la clase FunkoCacheImpl
 */
public class FunkoCacheImplTest {

    private FunkoCacheImpl cache;
    private long secondsToClear = 1;

    /**
     * Inicializa la caché antes de cada test
     */
    @BeforeEach
    public void setUp() {
        cache = new FunkoCacheImpl(15, secondsToClear);
    }

    /**
     * Test put and get
     */
    @Test
    public void testPutAndGet() {
        Funko funko = Funko.builder().build();
        cache.put("1", funko).block();
        assertEquals(funko, cache.get("1").block());
    }

    /**
     * Test remove
     */
    @Test
    public void testRemove() {
        Funko funko = Funko.builder().build();
        cache.put("2", funko).block();
        cache.remove("2").block();
        assertNull(cache.get("2").block());
    }

    /**
     * Test shutdown
     */
    @Test
    public void testShutdown() {
        cache.shutdown();
        assertTrue(cache.getCleaner().isShutdown());
    }

    /**
     * Test clear
     *
     * @throws InterruptedException si hay un error de interrupción
     */
    @Test
    public void testClear() throws InterruptedException {
        Funko funko = Funko.builder().build();
        cache.put("1", funko).block();
        Thread.sleep(secondsToClear * 1000);
        cache.clear();
        assertNull(cache.get("1").block());
    }
}
