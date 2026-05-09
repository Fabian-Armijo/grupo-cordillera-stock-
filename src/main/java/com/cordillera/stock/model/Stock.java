package com.cordillera.stock.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inventario") // Usamos 'inventario' para la tabla, es una buena práctica en español
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia al microservicio de Productos
    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    // Referencia al futuro microservicio de Sucursales
    @Column(name = "sucursal_id", nullable = false)
    private Long sucursalId;

    // Cantidad física real disponible
    @Column(nullable = false)
    private Integer cantidadDisponible;

    // (Opcional) Útil si en el futuro implementas un microservicio de Ventas
    // y quieres separar lo que está en bodega de lo que ya está en el carrito de compras
    @Column(nullable = false)
    private Integer cantidadReservada = 0;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    // Estos métodos de JPA actualizan la fecha automáticamente cada vez que
    // se guarda o se modifica el stock en la base de datos
    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
}
