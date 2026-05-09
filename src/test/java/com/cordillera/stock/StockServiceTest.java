package com.cordillera.stock;

import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.model.Stock;
import com.cordillera.stock.repository.StockRepository;
import com.cordillera.stock.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    // PRUEBA 1: Agregar Stock (Cuando el producto NO existía en la sucursal)
    @Test
    void agregarStock_DeberiaCrearNuevoRegistro_CuandoNoExiste() {
        // ARRANGE
        StockRequestDTO request = new StockRequestDTO();
        request.setProductoId(100L);
        request.setSucursalId(5L);
        request.setCantidadDisponible(50);
        request.setCantidadReservada(0);

        // Simulamos que al buscar, no encuentra nada
        when(stockRepository.findByProductoIdAndSucursalId(100L, 5L)).thenReturn(Optional.empty());

        // Simulamos lo que devolverá la BD al guardar
        Stock stockGuardado = new Stock();
        stockGuardado.setId(1L);
        stockGuardado.setProductoId(100L);
        stockGuardado.setSucursalId(5L);
        stockGuardado.setCantidadDisponible(50);
        stockGuardado.setCantidadReservada(0);

        when(stockRepository.save(any(Stock.class))).thenReturn(stockGuardado);

        // ACT
        StockResponseDTO response = stockService.agregarStock(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(50, response.getCantidadDisponible());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    // PRUEBA 2: Agregar Stock (Cuando el producto YA existía, debe sumar)
    @Test
    void agregarStock_DeberiaSumarCantidades_CuandoYaExiste() {
        // ARRANGE
        // Simulamos un registro que ya tiene 20 unidades en base de datos
        Stock stockExistente = new Stock();
        stockExistente.setId(1L);
        stockExistente.setProductoId(100L);
        stockExistente.setSucursalId(5L);
        stockExistente.setCantidadDisponible(20);
        stockExistente.setCantidadReservada(0);

        when(stockRepository.findByProductoIdAndSucursalId(100L, 5L)).thenReturn(Optional.of(stockExistente));

        // El request pide agregar 30 unidades más
        StockRequestDTO request = new StockRequestDTO();
        request.setProductoId(100L);
        request.setSucursalId(5L);
        request.setCantidadDisponible(30);

        // Simulamos el objeto que devolverá la BD tras la suma
        Stock stockActualizado = new Stock();
        stockActualizado.setId(1L);
        stockActualizado.setProductoId(100L);
        stockActualizado.setSucursalId(5L);
        stockActualizado.setCantidadDisponible(50);
        stockActualizado.setCantidadReservada(0);

        when(stockRepository.save(any(Stock.class))).thenReturn(stockActualizado);

        // ACT
        StockResponseDTO response = stockService.agregarStock(request);

        // ASSERT
        assertNotNull(response);
        assertEquals(50, response.getCantidadDisponible()); // Verificamos que la matemática de tu service funcionó
    }

    // PRUEBA 3: Consumir Stock Exitoso (Venta normal)
    @Test
    void consumirStock_DeberiaReducirCantidad_CuandoHaySuficiente() {
        // ARRANGE
        Stock stockActual = new Stock();
        stockActual.setId(1L);
        stockActual.setProductoId(100L);
        stockActual.setSucursalId(5L);
        stockActual.setCantidadDisponible(100);

        when(stockRepository.findByProductoIdAndSucursalId(100L, 5L)).thenReturn(Optional.of(stockActual));

        // Simulamos el objeto resultante tras consumir 30 unidades (quedarán 70)
        Stock stockResultante = new Stock();
        stockResultante.setId(1L);
        stockResultante.setProductoId(100L);
        stockResultante.setSucursalId(5L);
        stockResultante.setCantidadDisponible(70);

        when(stockRepository.save(any(Stock.class))).thenReturn(stockResultante);

        // ACT (Intentamos consumir 30)
        StockResponseDTO response = stockService.consumirStock(100L, 5L, 30);

        // ASSERT
        assertNotNull(response);
        assertEquals(70, response.getCantidadDisponible());
    }

    // PRUEBA 4: Regla de Negocio (Consumir stock que NO existe)
    @Test
    void consumirStock_DeberiaLanzarExcepcion_CuandoNoHayRegistro() {
        // ARRANGE
        when(stockRepository.findByProductoIdAndSucursalId(999L, 5L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            stockService.consumirStock(999L, 5L, 10);
        });

        // Verificamos que lance exactamente tu mensaje de error
        assertEquals("No hay registro de stock para este producto en esta sucursal", excepcion.getMessage());
        verify(stockRepository, never()).save(any(Stock.class));
    }

    // PRUEBA 5: Regla de Negocio (Evitar stock negativo)
    @Test
    void consumirStock_DeberiaLanzarExcepcion_CuandoStockEsInsuficiente() {
        // ARRANGE
        Stock stockActual = new Stock();
        stockActual.setId(1L);
        stockActual.setProductoId(100L);
        stockActual.setSucursalId(5L);
        stockActual.setCantidadDisponible(5); // Solo quedan 5

        when(stockRepository.findByProductoIdAndSucursalId(100L, 5L)).thenReturn(Optional.of(stockActual));

        // ACT & ASSERT (Intentamos consumir 20)
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            stockService.consumirStock(100L, 5L, 20);
        });

        // Verificamos que tu mensaje incluya las cantidades
        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));
        verify(stockRepository, never()).save(any(Stock.class)); // Seguridad total: la base de datos no se toca
    }

    // PRUEBA 6: Consultas Listadas (Obtener por Sucursal)
    @Test
    void obtenerPorSucursal_DeberiaRetornarListaDeStock() {
        // ARRANGE
        Stock stock1 = new Stock();
        stock1.setId(1L);
        stock1.setProductoId(100L);
        stock1.setSucursalId(5L);
        stock1.setCantidadDisponible(10);

        Stock stock2 = new Stock();
        stock2.setId(2L);
        stock2.setProductoId(200L);
        stock2.setSucursalId(5L);
        stock2.setCantidadDisponible(25);

        // Simulamos que el repositorio devuelve una lista de 2 elementos
        when(stockRepository.findBySucursalId(5L)).thenReturn(List.of(stock1, stock2));

        // ACT
        List<StockResponseDTO> response = stockService.obtenerPorSucursal(5L);

        // ASSERT
        assertNotNull(response);
        assertEquals(2, response.size()); // Verificamos que trajo los 2 registros
        assertEquals(100L, response.get(0).getProductoId());
        assertEquals(200L, response.get(1).getProductoId());
    }
}