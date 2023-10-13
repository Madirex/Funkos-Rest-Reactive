package com.madirex.exceptions;

/**
 * Excepción al crear una carpeta
 */
public class CreateFolderException extends Exception {
    /**
     * Constructor
     *
     * @param message mensaje de error
     */
    public CreateFolderException(String message) {
        super("Error al crear la carpeta: " + message);
    }
}
