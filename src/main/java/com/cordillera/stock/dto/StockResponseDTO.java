package com.cordillera.stock.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StockResponseDTO {
    private Long id;

    private Long productoId;
    private Long sucursalId;

    private Integer cantidadDisponible;
    private Integer cantidadReservada;

    private LocalDateTime ultimaActualizacion;
}
