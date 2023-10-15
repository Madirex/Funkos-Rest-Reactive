package com.madirex.exceptions;

/**
 * Excepción al leer el archivo CSV
 */
public class ReadCSVFailException extends Exception {
    /**
     * Constructor
     *
     * @param message mensaje de error
     */
    public ReadCSVFailException(String message) {
        super("Error al leer el archivo CSV: " + message);
    }
}
