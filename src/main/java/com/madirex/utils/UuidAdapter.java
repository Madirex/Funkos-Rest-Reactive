package com.madirex.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.UUID;

/**
 * Clase UuidAdapter que adapta la clase UUID para poder escribir y leer objetos UUID en formato String
 */
public class UuidAdapter extends TypeAdapter<UUID> {

    /**
     * Lee un objeto UUID en formato String
     *
     * @param jsonReader Instancia de la clase JsonReader
     * @return Objeto UUID leído
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public UUID read(final JsonReader jsonReader) throws IOException {
        return UUID.fromString(jsonReader.nextString());
    }

    /**
     * Escribe un objeto UUID en formato String
     *
     * @param jsonWriter Instancia de la clase JsonWriter
     * @param uuid       Objeto UUID a escribir
     * @throws IOException Excepción de entrada/salida
     */
    @Override
    public void write(JsonWriter jsonWriter, UUID uuid) throws IOException {
        jsonWriter.value(uuid.toString());
    }
}