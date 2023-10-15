package com.madirex.models.funko;

import com.madirex.utils.Utils;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo Funko
 */
@Data
@Builder
public class Funko {
    @Builder.Default
    private UUID cod = UUID.randomUUID();
    @Builder.Default
    private Long myId = -1L;
    private String name;
    private Model model;
    private double price;
    private LocalDate releaseDate;
    @Builder.Default
    private LocalDateTime updateAt = LocalDateTime.now();

    /**
     * Constructor
     */
    @Override
    public String toString() {
        return "Funko:" +
                "\n\tCod=" + cod +
                "\n\tMyId=" + myId +
                "\n\tNombre='" + name + '\'' +
                "\n\tModelo=" + model +
                "\n\tPrecio=" + Utils.getInstance().doubleToESLocal(price) +
                "\n\tFecha lanzamiento=" + releaseDate +
                '\n';
    }
}