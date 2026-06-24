package com.cordillera.stock;

import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.model.Stock;
import com.cordillera.stock.repository.StockRepository;
import com.cordillera.stock.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    private StockRequestDTO requestDTO;
    private Stock stockExistente;

    @BeforeEach
    void setUp() {
        // Preparar DTO de entradaaaaaa
        requestDTO = new StockRequestDTO();
        requestDTO.setProductoId(1L);
        requestDTO.setSucursalId(10L);
        requestDTO.setCantidadDisponible(50);
        requestDTO.setCantidadReservada(5);

        // Preparar Entidad simulada
        stockExistente = new Stock();
        stockExistente.setId(100L);
        stockExistente.setProductoId(1L);
        stockExistente.setSucursalId(10L);
        stockExistente.setCantidadDisponible(100);
        stockExistente.setCantidadReservada(10);
        stockExistente.setUltimaActualizacion(LocalDateTime.now());
    }

    // --- TEST: AGREGAR STOCK ---

    @Test
    void agregarStock_CuandoNoExiste_DeberiaCrearNuevoRegistro() {
        // Arrange: Simulamos que NO existe el stock
        when(stockRepository.findByProductoIdAndSucursalId(1L, 10L)).thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockResponseDTO response = stockService.agregarStock(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(50, response.getCantidadDisponible());
        assertEquals(5, response.getCantidadReservada());
        verify(stockRepository, times(1)).save(any(Stock.class));
    }

    @Test
    void agregarStock_CuandoYaExiste_DeberiaSumarCantidades() {
        // Arrange: Simulamos que SÍ existe el stock (100 disp, 10 res)
        when(stockRepository.findByProductoIdAndSucursalId(1L, 10L)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Agregamos 50 disp y 5 res
        StockResponseDTO response = stockService.agregarStock(requestDTO);

        // Assert
        assertNotNull(response);
        assertEquals(150, response.getCantidadDisponible()); // 100 + 50
        assertEquals(15, response.getCantidadReservada());   // 10 + 5
        verify(stockRepository, times(1)).save(stockExistente);
    }

    // --- TEST: CONSUMIR STOCK ---

    @Test
    void consumirStock_CuandoHaySuficiente_DeberiaRestarCantidad() {
        // Arrange: Stock actual es 100
        when(stockRepository.findByProductoIdAndSucursalId(1L, 10L)).thenReturn(Optional.of(stockExistente));
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Consumimos 30
        StockResponseDTO response = stockService.consumirStock(1L, 10L, 30);

        // Assert
        assertNotNull(response);
        assertEquals(70, response.getCantidadDisponible()); // 100 - 30
        verify(stockRepository, times(1)).save(stockExistente);
    }

    @Test
    void consumirStock_CuandoNoExisteRegistro_DeberiaLanzarExcepcion() {
        // Arrange
        when(stockRepository.findByProductoIdAndSucursalId(1L, 10L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.consumirStock(1L, 10L, 10);
        });

        assertEquals("No hay registro de stock para este producto en esta sucursal", exception.getMessage());
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void consumirStock_CuandoNoHaySuficiente_DeberiaLanzarExcepcion() {
        // Arrange: Stock actual es 100
        when(stockRepository.findByProductoIdAndSucursalId(1L, 10L)).thenReturn(Optional.of(stockExistente));

        // Act & Assert: Intentamos consumir 150
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.consumirStock(1L, 10L, 150);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(stockRepository, never()).save(any(Stock.class));
    }

    // --- TEST: CONSULTAS ---

    @Test
    void obtenerPorProducto_DeberiaRetornarListaDeStock() {
        // Arrange
        when(stockRepository.findByProductoId(1L)).thenReturn(Arrays.asList(stockExistente));

        // Act
        List<StockResponseDTO> lista = stockService.obtenerPorProducto(1L);

        // Assert
        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(1L, lista.get(0).getProductoId());
        verify(stockRepository, times(1)).findByProductoId(1L);
    }

    @Test
    void obtenerPorSucursal_DeberiaRetornarListaDeStock() {
        // Arrange
        when(stockRepository.findBySucursalId(10L)).thenReturn(Arrays.asList(stockExistente));

        // Act
        List<StockResponseDTO> lista = stockService.obtenerPorSucursal(10L);

        // Assert
        assertNotNull(lista);
        assertEquals(1, lista.size());
        assertEquals(10L, lista.get(0).getSucursalId());
        verify(stockRepository, times(1)).findBySucursalId(10L);
    }
}