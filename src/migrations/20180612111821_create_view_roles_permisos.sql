--DOWN
DROP VIEW IF EXISTS vw_roles_permisos;
--UP sqlite3
CREATE VIEW vw_roles_permisos AS SELECT
  P.id AS id, P.nombre AS nombre, P.llave AS llave, R.nombre AS rol, R.id rol_id , 0 AS existe  
  FROM roles_permisos RP 
  INNER JOIN roles R ON R.id = RP.rol_id 
  INNER JOIN permisos P ON P.id = RP.permiso_id 
  LIMIT 2000;