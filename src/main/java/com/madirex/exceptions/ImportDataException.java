package com.madirex.exceptions;

/**
 * Excepción al importar datos
 */
public class ImportDataException extends Exception {
    /**
     * Constructor
     *
     * @param message mensaje de error
     */
    public ImportDataException(String message) {
        super("Error al importar los datos: " + message);
    }
}
