package com.cordillera.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockResponseDTO {
    private Long id;

    private Long productoId;
    private Long sucursalId;

    private Integer cantidadDisponible;
    private Integer cantidadReservada;

    private LocalDateTime ultimaActualizacion;
}
