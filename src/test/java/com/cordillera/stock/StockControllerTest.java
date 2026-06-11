package com.cordillera.stock;

import com.cordillera.stock.controller.StockController;
import com.cordillera.stock.dto.StockRequestDTO;
import com.cordillera.stock.dto.StockResponseDTO;
import com.cordillera.stock.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros de seguridad para aislar el test del controlador
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockService stockService;

    @Autowired
    private ObjectMapper objectMapper;

    private StockRequestDTO requestDTO;
    private StockResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // Inicializar datos de prueba antes de cada test
        requestDTO = new StockRequestDTO();
        requestDTO.setProductoId(1L);
        requestDTO.setSucursalId(10L);
        requestDTO.setCantidadDisponible(100);
        requestDTO.setCantidadReservada(0);

        responseDTO = new StockResponseDTO();
        // Asumiendo la estructura de tu ResponseDTO. Ajusta los setters según corresponda.
        responseDTO.setProductoId(1L);
        responseDTO.setSucursalId(10L);
        responseDTO.setCantidadDisponible(100);
        responseDTO.setCantidadReservada(0);
    }

    @Test
    void agregar_DeberiaRetornarStockYStatusCreated() throws Exception {
        // Arrange: Configuramos el comportamiento del mock
        Mockito.when(stockService.agregarStock(any(StockRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert: Ejecutamos la petición y verificamos resultados
        mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated()) // Espera 201
                .andExpect(jsonPath("$.productoId").value(1L))
                .andExpect(jsonPath("$.cantidadDisponible").value(100));
    }

    @Test
    void consumir_DeberiaRetornarStockActualizadoYStatusOK() throws Exception {
        // Arrange
        responseDTO.setCantidadDisponible(80); // Simulamos que se consumieron 20
        Mockito.when(stockService.consumirStock(eq(1L), eq(10L), eq(20))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/stock/producto/1/sucursal/10/consumir")
                        .param("cantidad", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera 200
                .andExpect(jsonPath("$.cantidadDisponible").value(80));
    }

    @Test
    void obtenerPorProducto_DeberiaRetornarListaYStatusOK() throws Exception {
        // Arrange
        List<StockResponseDTO> lista = Arrays.asList(responseDTO);
        Mockito.when(stockService.obtenerPorProducto(1L)).thenReturn(lista);

        // Act & Assert
        mockMvc.perform(get("/api/stock/producto/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].productoId").value(1L));
    }

    @Test
    void obtenerPorSucursal_DeberiaRetornarListaYStatusOK() throws Exception {
        // Arrange
        List<StockResponseDTO> lista = Arrays.asList(responseDTO);
        Mockito.when(stockService.obtenerPorSucursal(10L)).thenReturn(lista);

        // Act & Assert
        mockMvc.perform(get("/api/stock/sucursal/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].sucursalId").value(10L));
    }
}
