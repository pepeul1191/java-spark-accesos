--DOWN
DROP VIEW IF EXISTS vw_subtitulos_items;
--UP sqlite3
CREATE VIEW vw_subtitulos_items AS SELECT 
  S.modulo_id, S.id AS subtitulo_id, S.nombre AS subtitulo, I.nombre AS item, I.url
  FROM subtitulos S  
  INNER JOIN items I ON S.id = i.subtitulo_id
  LIMIT 2000;