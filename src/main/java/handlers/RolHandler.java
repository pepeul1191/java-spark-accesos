package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import configs.Database;
import models.Rol;
import models.ViewRolPermiso;
import models.RolPermiso;

public class RolHandler{
  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Rol> rptaList = Rol.find("sistema_id = ?", sistemaId);
      for (Rol rol : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", rol.get("id"));
        obj.put("nombre", rol.get("nombre"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los roles", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", error);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      db.close();
    }
    return rpta;
  };

  public static Route guardar = (Request request, Response response) -> {
    String rpta = "";
    List<JSONObject> listJSONNuevos = new ArrayList<JSONObject>();
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray nuevos = data.getJSONArray("nuevos");
      JSONArray editados = data.getJSONArray("editados");
      JSONArray eliminados = data.getJSONArray("eliminados");
      int sistemaId = data.getJSONObject("extra").getInt("sistema_id");
      db.open();
      db.getDb().openTransaction();
      if(nuevos.length() > 0){
        for (int i = 0; i < nuevos.length(); i++) {
          JSONObject rol = nuevos.getJSONObject(i);
          String antiguoId = rol.getString("id");
          String nombre = rol.getString("nombre");
          Rol n = new Rol();
          n.set("nombre", nombre);
          n.set("sistema_id", sistemaId);
          n.saveIt();
          int nuevoId = (int) n.get("id"); 
          JSONObject temp = new JSONObject();
          temp.put("temporal", antiguoId);
          temp.put("nuevo_id", nuevoId);
          listJSONNuevos.add(temp);
        }
      }
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject rol = editados.getJSONObject(i);
          int id = rol.getInt("id");
          String nombre = rol.getString("nombre");
          Rol e = Rol.findFirst("id = ?", id);
          if(e != null){
            e.set("nombre", nombre);
            e.saveIt();
          }
        }
      }
      if(eliminados.length() > 0){
        for (Object eliminado : eliminados) {
          String eleminadoId = (String)eliminado;
          Rol d = Rol.findFirst("id = ?", eleminadoId);
          if(d != null){
            d.delete();
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en los roles del sitema");
      cuerpoMensaje.put(listJSONNuevos);
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en  guardar los roles del sitema", e.toString()};
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "error");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      response.status(500);
      rpta = rptaMensaje.toString();
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route listarPermisos = (Request request, Response response) -> {
    String rpta = "";
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    int rolId = Integer.parseInt(request.params(":rol_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      String sql = 
        "SELECT T.id AS id, T.nombre AS nombre, (CASE WHEN (P.existe = 1) THEN 1 ELSE 0 END) AS existe, T.llave AS llave FROM " +
        "(" +
        "SELECT id, nombre, llave, 0 AS existe FROM permisos WHERE sistema_id = ? " +
        ") T " +
        "LEFT JOIN " +
        "(" +
          "SELECT P.id, P.nombre,  P.llave, 1 AS existe  FROM permisos P " +
          "INNER JOIN roles_permisos RP ON P.id = RP.permiso_id " +
          "WHERE RP.rol_id =  ? " +
        ") P " +
        "ON T.id = P.id";
      List<ViewRolPermiso> rptaList = ViewRolPermiso.findBySQL(sql, sistemaId, rolId);
      for (ViewRolPermiso rolPermiso : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", rolPermiso.get("id"));
        obj.put("nombre", rolPermiso.get("nombre"));
        obj.put("llave", rolPermiso.get("llave"));
        obj.put("existe", rolPermiso.get("existe"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los permisos del rol", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", error);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      db.close();
    }
    return rpta;
  };

  public static Route guardarPermisos = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray editados = data.getJSONArray("editados");
      int rolId = data.getJSONObject("extra").getInt("rol_id");
      db.open();
      db.getDb().openTransaction();
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject permiso = editados.getJSONObject(i);
          int permisoId = permiso.getInt("id");
          int existe = permiso.getInt("existe");
          RolPermiso e = RolPermiso.findFirst("rol_id = ? AND permiso_id = ?", rolId, permisoId);
          if (existe == 0){//borrar si existe
            if(e != null){
              e.delete();
            }
          }else if(existe == 1){//crear si no existe
            if(e == null){
              RolPermiso n = new RolPermiso();
              n.set("permiso_id", permisoId);
              n.set("rol_id", rolId);
              n.saveIt();
            }
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado la asociación de permisos al rol");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en asociar los permisos al rol", e.toString()};
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "error");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      response.status(500);
      rpta = rptaMensaje.toString();
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };  
}