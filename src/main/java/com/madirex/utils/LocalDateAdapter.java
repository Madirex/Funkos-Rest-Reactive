package com.madirex.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Clase LocalDateAdapter que adapta la clase LocalDate para poder escribir y leer objetos LocalDate en formato String
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    /**
     * Lee un objeto LocalDate en formato String
     *
     * @param jsonReader Instancia de la clase JsonReader
     * @return Objeto LocalDate leído
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        return LocalDate.parse(jsonReader.nextString());
    }

    /**
     * Escribe un objeto LocalDate en formato String
     *
     * @param jsonWriter Instancia de la clase JsonWriter
     * @param localDate  Objeto LocalDate a escribir
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public void write(JsonWriter jsonWriter, LocalDate localDate) throws IOException {
        jsonWriter.value(localDate.toString());

    }
}
