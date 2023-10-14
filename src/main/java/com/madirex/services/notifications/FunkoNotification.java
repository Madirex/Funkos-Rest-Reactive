package com.madirex.services.notifications;

import com.madirex.models.Notification;
import com.madirex.models.funko.Funko;
import reactor.core.publisher.Flux;

/**
 * Interfaz para la implementación de notificaciones
 */
public interface FunkoNotification {
    Flux<Notification<Funko>> getNotificationAsFlux();

    void notify(Notification<Funko> notification);
}
