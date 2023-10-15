package com.madirex.exceptions;

/**
 * Excepción al no encontrar un Funko
 */
public class FunkoNotFoundException extends FunkoException {
    public FunkoNotFoundException(String message) {
        /**
         * Constructor
         *
         * @param message mensaje de error
         */
        super("Funko/a no encontrado: " + message);
    }
}
