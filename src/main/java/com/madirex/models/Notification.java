package com.madirex.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Modelo de notificación
 *
 * @param <T> Tipo de contenido de la notificación
 */
@Data
@AllArgsConstructor
public class Notification<T> {
    private Type type;
    private T content;

    /**
     * Enumerado de tipos de notificaciones
     */
    public enum Type {
        NEW, UPDATED, DELETED
    }
}