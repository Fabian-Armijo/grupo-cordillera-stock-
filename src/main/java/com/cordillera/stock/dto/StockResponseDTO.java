package com.cordillera.stock.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockResponseDTO {
    private Long id;

    // Mantenemos la independencia devolviendo solo los IDs
    private Long productoId;
    private Long sucursalId;

    private Integer cantidadDisponible;
    private Integer cantidadReservada;

    // Útil para que el frontend muestre "Actualizado hace X minutos"
    private LocalDateTime ultimaActualizacion;
}
