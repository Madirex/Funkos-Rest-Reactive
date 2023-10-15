package com.madirex.services.notifications;

import com.madirex.models.Notification;
import com.madirex.models.funko.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * Implementación de la notificación de Funko
 */
public class FunkoNotificationImpl implements FunkoNotification {
    private static FunkoNotificationImpl INSTANCE = new FunkoNotificationImpl();

    private final Flux<Notification<Funko>> funkoNotificationFlux;
    private FluxSink<Notification<Funko>> fluxNotification;

    /**
     * Constructor privado
     */
    private FunkoNotificationImpl() {
        this.funkoNotificationFlux = Flux.<Notification<Funko>>create(emitter ->
                this.fluxNotification = emitter).share();
    }

    /**
     * Devuelve la instancia de la notificación
     *
     * @return Instancia de la notificación
     */
    public static FunkoNotificationImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FunkoNotificationImpl();
        }
        return INSTANCE;
    }

    /**
     * Devuelve la notificación como un flujo
     *
     * @return Flujo de notificaciones
     */
    @Override
    public Flux<Notification<Funko>> getNotificationAsFlux() {
        return funkoNotificationFlux;
    }

    /**
     * Notifica a los suscriptores de la notificación
     *
     * @param notification Notificación a enviar
     */
    @Override
    public void notify(Notification<Funko> notification) {
        fluxNotification.next(notification);
    }
}