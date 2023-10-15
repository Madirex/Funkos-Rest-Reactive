package com.madirex.exceptions;

/**
 * Excepción al exportar datos
 */
public class ExportDataException extends Exception {
    /**
     * Constructor
     *
     * @param message mensaje de error
     */
    public ExportDataException(String message) {
        super("Error al exportar los datos: " + message);
    }
}
