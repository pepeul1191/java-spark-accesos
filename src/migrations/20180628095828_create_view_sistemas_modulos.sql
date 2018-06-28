--DOWN
DROP VIEW IF EXISTS vw_sistemas_modulos;
--UP sqlite3
CREATE VIEW vw_sistemas_modulos AS SELECT 
  S.id AS sistema_id, S.nombre AS nombre_sistema, M.id AS modulo_id, M.nombre AS nombre_modulo, M.url
  FROM sistemas S  
  INNER JOIN modulos M ON S.id = M.sistema_id
  LIMIT 2000;