package com.madirex.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Clase Utils que contiene métodos útiles para la aplicación
 */
public class Utils {

    private static Utils utilsInstance;

    /**
     * Constructor privado de la clase Utils
     */
    private Utils() {
    }

    /**
     * Devuelve una instancia de la clase Utils
     *
     * @return Instancia de la clase Utils
     */
    public static synchronized Utils getInstance() {
        if (utilsInstance == null) {
            utilsInstance = new Utils();
        }
        return utilsInstance;
    }

    /**
     * Devuelve un String con el formato de moneda de España
     *
     * @param dbl cantidad de tipo double
     * @return Moneda con formato de España
     */
    public String doubleToESLocal(double dbl) {
        return String.format("%,.2f", dbl).replace(".", ",");
    }

    /**
     * Devuelve los bytes de un archivo
     *
     * @param dataFile Archivo del que se quieren obtener los bytes
     * @return Bytes del archivo
     */
    public byte[] getFileBytes(File dataFile) {
        try {
            return Files.readAllBytes(dataFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Escribe un String en un archivo
     *
     * @param dest Ruta del archivo
     * @param json String a escribir
     */
    public void writeString(String dest, String json)  {
        try {
            Files.writeString(new File(dest).toPath(), json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
