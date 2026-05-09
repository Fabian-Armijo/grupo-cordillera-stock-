package com.cordillera.stock.controller;

import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.service.StockService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private StockService stockService;

    // 1. Agregar stock (Llegada de mercadería)
    @PostMapping
    public ResponseEntity<StockResponseDTO> agregar(@Valid @RequestBody StockRequestDTO request) {
        StockResponseDTO nuevoStock = stockService.agregarStock(request);
        return new ResponseEntity<>(nuevoStock, HttpStatus.CREATED);
    }

    // 2. Consumir stock (Venta o salida de mercadería)
    @PutMapping("/producto/{productoId}/sucursal/{sucursalId}/consumir")
    public ResponseEntity<StockResponseDTO> consumir(
            @PathVariable Long productoId,
            @PathVariable Long sucursalId,
            @RequestParam Integer cantidad) {

        StockResponseDTO stockActualizado = stockService.consumirStock(productoId, sucursalId, cantidad);
        return ResponseEntity.ok(stockActualizado);
    }

    // 3. Consultar todo el stock de un producto específico (en todas sus sucursales)
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<StockResponseDTO>> obtenerPorProducto(@PathVariable Long productoId) {
        return ResponseEntity.ok(stockService.obtenerPorProducto(productoId));
    }

    // 4. Consultar todo el inventario de una sucursal específica
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<StockResponseDTO>> obtenerPorSucursal(@PathVariable Long sucursalId) {
        return ResponseEntity.ok(stockService.obtenerPorSucursal(sucursalId));
    }
}
