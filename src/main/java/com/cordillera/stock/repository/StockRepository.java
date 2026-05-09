package com.cordillera.stock.repository;

import com.cordillera.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // 1. Saber en qué sucursales está disponible un producto y cuánto hay en cada una
    List<Stock> findByProductoId(Long productoId);

    // 2. Ver todo el inventario que tiene una sucursal en particular
    List<Stock> findBySucursalId(Long sucursalId);

    // 3. El método más importante: Buscar el registro exacto de un producto en una sucursal
    // Útil para cuando un operario escanea un producto para sumar o restar stock
    Optional<Stock> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    // 4. (Opcional pero útil) Verificar si un producto ya tiene un registro creado en una sucursal
    boolean existsByProductoIdAndSucursalId(Long productoId, Long sucursalId);
}
