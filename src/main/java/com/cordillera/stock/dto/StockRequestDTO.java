package com.cordillera.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El ID de la sucursal es obligatorio")
    private Long sucursalId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad disponible no puede ser negativa")
    private Integer cantidadDisponible;

    // La cantidad reservada suele ser opcional al crear,
    // pero si la envían, no puede ser menor a 0.
    @Min(value = 0, message = "La cantidad reservada no puede ser negativa")
    private Integer cantidadReservada = 0;
}
