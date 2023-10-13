package com.madirex.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Clase LocalDateTimeAdapter que adapta la clase LocalDateTime para poder escribir y leer objetos LocalDateTime en formato String
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    /**
     * Lee un objeto LocalDateTime en formato String
     *
     * @param jsonReader Instancia de la clase JsonReader
     * @return Objeto LocalDateTime leído
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString());
    }

    /**
     * Escribe un objeto LocalDateTime en formato String
     *
     * @param jsonWriter    Instancia de la clase JsonWriter
     * @param localDateTime Objeto LocalDateTime a escribir
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(localDateTime.toString());
    }
}