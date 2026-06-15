package com.cordillera.stock.controller;

import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@Tag(name = "Gestión de Inventario", description = "Endpoints para administrar, consultar y actualizar el stock de las sucursales de Grupo Cordillera")
public class StockController {

    @Autowired
    private StockService stockService;

    // 1. Agregar stock (Llegada de mercadería)
    @Operation(
            summary = "Agregar stock de producto",
            description = "Registra la llegada de mercadería para un producto en una sucursal específica, creando el registro o sumando a la cantidad existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Stock agregado o actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de validación incorrectos en el DTO")
    })
    @PostMapping
    public ResponseEntity<StockResponseDTO> agregar(
            @Parameter(description = "Objeto con los datos del stock a agregar (Producto, Sucursal y Cantidad)")
            @Valid @RequestBody StockRequestDTO request) {
        StockResponseDTO nuevoStock = stockService.agregarStock(request);
        return new ResponseEntity<>(nuevoStock, HttpStatus.CREATED);
    }

    // 2. Consumir stock (Venta o salida de mercadería)
    @Operation(
            summary = "Consumir inventario",
            description = "Registra una venta o salida de mercadería, descontando la cantidad especificada del stock disponible de una sucursal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock descontado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente para realizar la operación"),
            @ApiResponse(responseCode = "404", description = "Registro de inventario no encontrado")
    })
    @PutMapping("/producto/{productoId}/sucursal/{sucursalId}/consumir")
    public ResponseEntity<StockResponseDTO> consumir(
            @Parameter(description = "ID único del producto", example = "1") @PathVariable Long productoId,
            @Parameter(description = "ID único de la sucursal", example = "1") @PathVariable Long sucursalId,
            @Parameter(description = "Cantidad exacta de unidades a descontar", example = "5") @RequestParam Integer cantidad) {

        StockResponseDTO stockActualizado = stockService.consumirStock(productoId, sucursalId, cantidad);
        return ResponseEntity.ok(stockActualizado);
    }

    // 3. Consultar todo el stock de un producto específico (en todas sus sucursales)
    @Operation(
            summary = "Consultar stock por producto",
            description = "Obtiene una lista con la disponibilidad de inventario de un producto específico, distribuido en todas las sucursales donde exista registro."
    )
    @ApiResponse(responseCode = "200", description = "Consulta realizada con éxito")
    @GetMapping("/producto/{productoId}")
    public ResponseEntity<List<StockResponseDTO>> obtenerPorProducto(
            @Parameter(description = "ID único del producto a buscar", example = "1") @PathVariable Long productoId) {
        return ResponseEntity.ok(stockService.obtenerPorProducto(productoId));
    }

    // 4. Consultar todo el inventario de una sucursal específica
    @Operation(
            summary = "Consultar inventario por sucursal",
            description = "Obtiene el catálogo completo de todos los productos y sus cantidades disponibles en una sucursal específica."
    )
    @ApiResponse(responseCode = "200", description = "Consulta realizada con éxito")
    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<List<StockResponseDTO>> obtenerPorSucursal(
            @Parameter(description = "ID único de la sucursal", example = "1") @PathVariable Long sucursalId) {
        return ResponseEntity.ok(stockService.obtenerPorSucursal(sucursalId));
    }
}