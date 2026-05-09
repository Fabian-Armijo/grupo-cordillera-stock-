package com.cordillera.stock.service;

import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.model.Stock;
import com.cordillera.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    // 1. Agregar Stock (Si ya existe, suma; si no existe, lo crea)
    @Transactional
    public StockResponseDTO agregarStock(StockRequestDTO request) {

        // Buscamos si ya hay un registro de este producto en esta sucursal
        Optional<Stock> stockExistente = stockRepository
                .findByProductoIdAndSucursalId(request.getProductoId(), request.getSucursalId());

        Stock stockGuardar;

        if (stockExistente.isPresent()) {
            // REGLA DE NEGOCIO: Si existe, SUMAMOS la nueva cantidad a la que ya había
            stockGuardar = stockExistente.get();
            int nuevaCantidad = stockGuardar.getCantidadDisponible() + request.getCantidadDisponible();
            stockGuardar.setCantidadDisponible(nuevaCantidad);

            // Opcional: sumar también reservas si las envían
            if (request.getCantidadReservada() != null) {
                stockGuardar.setCantidadReservada(stockGuardar.getCantidadReservada() + request.getCantidadReservada());
            }
        } else {
            // REGLA DE NEGOCIO: Si no existe, creamos un registro nuevo desde cero
            stockGuardar = new Stock();
            stockGuardar.setProductoId(request.getProductoId());
            stockGuardar.setSucursalId(request.getSucursalId());
            stockGuardar.setCantidadDisponible(request.getCantidadDisponible());
            stockGuardar.setCantidadReservada(request.getCantidadReservada() != null ? request.getCantidadReservada() : 0);
        }

        Stock guardado = stockRepository.save(stockGuardar);
        return mapToResponseDTO(guardado);
    }

    // 2. Reducir o Consumir Stock (Ej: cuando se concreta una venta)
    @Transactional
    public StockResponseDTO consumirStock(Long productoId, Long sucursalId, Integer cantidadAconsumir) {

        Stock stock = stockRepository.findByProductoIdAndSucursalId(productoId, sucursalId)
                .orElseThrow(() -> new RuntimeException("No hay registro de stock para este producto en esta sucursal"));

        // REGLA DE NEGOCIO: Validar que haya suficiente stock antes de restar
        if (stock.getCantidadDisponible() < cantidadAconsumir) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + stock.getCantidadDisponible() +
                    ", Solicitado: " + cantidadAconsumir);
        }

        // Restamos la cantidad
        stock.setCantidadDisponible(stock.getCantidadDisponible() - cantidadAconsumir);

        Stock guardado = stockRepository.save(stock);
        return mapToResponseDTO(guardado);
    }

    // 3. Consultar Stock de un Producto en todas las sucursales
    @Transactional(readOnly = true)
    public List<StockResponseDTO> obtenerPorProducto(Long productoId) {
        return stockRepository.findByProductoId(productoId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // 4. Consultar todo el inventario de una Sucursal específica
    @Transactional(readOnly = true)
    public List<StockResponseDTO> obtenerPorSucursal(Long sucursalId) {
        return stockRepository.findBySucursalId(sucursalId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Método privado para mapear Entidad a DTO
    private StockResponseDTO mapToResponseDTO(Stock stock) {
        return StockResponseDTO.builder()
                .id(stock.getId())
                .productoId(stock.getProductoId())
                .sucursalId(stock.getSucursalId())
                .cantidadDisponible(stock.getCantidadDisponible())
                .cantidadReservada(stock.getCantidadReservada())
                .ultimaActualizacion(stock.getUltimaActualizacion())
                .build();
    }
}
