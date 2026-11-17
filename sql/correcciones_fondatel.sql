-- ============================================================
-- Correcciones para la base `fondatel`  (correr en phpMyAdmin, en este orden)
-- ============================================================

-- 1. Quitar los alimentos de prueba creados desde FAlimento (nadie los referencia)
DELETE FROM alimentos WHERE idalimentos IN (51, 392, 1928, 6352);

-- 2. Corregir la errata en los datos: 'entragado' -> 'entregado'
--    (la vista `tickets` filtra por 'entregado')
UPDATE ordenes SET estadoo = 'entregado' WHERE estadoo = 'entragado';

-- 3. Vista del COCINERO: el filtro decía 'ordeado' (typo). Se agregan mesa y silla.
CREATE OR REPLACE VIEW comandas AS
SELECT o.folio, o.fecha_hora, o.mesas_numero, o.silla, o.observaciones,
       o.estadoo, o.alimentos_idalimentos, a.nombre, a.descripcion
FROM ordenes o
JOIN alimentos a ON a.idalimentos = o.alimentos_idalimentos
WHERE o.estadoo = 'ordenado' AND DATE(o.fecha_hora) = CURDATE()
ORDER BY o.fecha_hora ASC;

-- 4. Órdenes de HOY para que las ventanas tengan datos (para demo y video).
--    Si repites la demo otro día, corre solo este INSERT otra vez.
INSERT INTO ordenes (fecha_hora, observaciones, estadoo, silla, clientes_codigoc,
                     mesas_numero, alimentos_idalimentos,
                     empleados_codigo_mesero, empleados_codigo_cocinero) VALUES
(NOW(), 'Sin picante',   'ordenado',  1, 'CLI001', 2,  11, 'MES003', 'COC002'),
(NOW(), 'Extra picante', 'ordenado',  2, 'CLI001', 2,  17, 'MES003', 'COC002'),
(NOW(), 'Sin cebolla',   'ordenado',  1, 'CLI002', 6,  35, 'MES003', 'COC002'),
(NOW(), 'Salsa aparte',  'ordenado',  3, 'CLI002', 6,   9, 'MES003', 'COC002'),
(NOW(), 'Para llevar',   'entregado', 1, 'CLI003', 11, 26, 'MES004', 'COC003'),
(NOW(), 'Con palillos',  'entregado', 2, 'CLI003', 11, 45, 'MES004', 'COC003');
