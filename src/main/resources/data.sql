-- Limpiamos o insertamos stock para los productos existentes
-- Producto 10 (MacBook) en dos sucursales distintas
INSERT INTO inventario (id, producto_id, sucursal_id, cantidad_disponible, cantidad_reservada, ultima_actualizacion)
VALUES (1, 10, 1, 15, 2, CURRENT_TIMESTAMP) ON CONFLICT (id) DO NOTHING;

INSERT INTO inventario (id, producto_id, sucursal_id, cantidad_disponible, cantidad_reservada, ultima_actualizacion)
VALUES (2, 10, 2, 10, 0, CURRENT_TIMESTAMP) ON CONFLICT (id) DO NOTHING;

-- Producto 11 (Dell) solo en la sucursal 1
INSERT INTO inventario (id, producto_id, sucursal_id, cantidad_disponible, cantidad_reservada, ultima_actualizacion)
VALUES (3, 11, 1, 5, 1, CURRENT_TIMESTAMP) ON CONFLICT (id) DO NOTHING;

-- Producto 12 (Samsung S24) con mucho stock en la sucursal 2
INSERT INTO inventario (id, producto_id, sucursal_id, cantidad_disponible, cantidad_reservada, ultima_actualizacion)
VALUES (4, 12, 2, 100, 5, CURRENT_TIMESTAMP) ON CONFLICT (id) DO NOTHING;

-- Sincronizamos la secuencia de IDs de la tabla inventario asdads
SELECT setval('inventario_id_seq', (SELECT MAX(id) FROM inventario));