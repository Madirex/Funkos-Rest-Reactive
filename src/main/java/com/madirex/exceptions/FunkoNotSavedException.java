package com.madirex.exceptions;

/**
 * Excepción al no guardar un Funko
 */
public class FunkoNotSavedException extends FunkoException {
    /**
     * Constructor
     *
     * @param message mensaje de error
     */
    public FunkoNotSavedException(String message) {
        super("Funko no guardado: " + message);
    }
}
