package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import configs.Database;
import models.Sistema;
import models.ViewSistemaModulo;
import models.ViewUsuarioSistema;

public class SistemaHandler{
  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Sistema> rptaList = Sistema.findAll();
      for (Sistema sistema : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", sistema.get("id"));
        obj.put("nombre", sistema.get("nombre"));
        obj.put("version", sistema.get("version"));
        obj.put("repositorio", sistema.get("repositorio"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los sistemas", e.toString()};
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
      db.open();
      db.getDb().openTransaction();
      if(nuevos.length() > 0){
        for (int i = 0; i < nuevos.length(); i++) {
          JSONObject sistema = nuevos.getJSONObject(i);
          String antiguoId = sistema.getString("id");
          String nombre = sistema.getString("nombre");
          String version = sistema.getString("version");
          String repositorio = sistema.getString("repositorio");
          Sistema n = new Sistema();
          n.set("nombre", nombre);
          n.set("version", version);
          n.set("repositorio", repositorio);
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
          JSONObject sistema = editados.getJSONObject(i);
          int id = sistema.getInt("id");
          String nombre = sistema.getString("nombre");
          String version = sistema.getString("version");
          String repositorio = sistema.getString("repositorio");
          Sistema e = Sistema.findFirst("id = ?", id);
          if(e != null){
            e.set("nombre", nombre);
            e.set("version", version);
            e.set("repositorio", repositorio);
            e.saveIt();
          }
        }
      }
      if(eliminados.length() > 0){
        for (Object eliminado : eliminados) {
          String eleminadoId = (String)eliminado;
          Sistema d = Sistema.findFirst("id = ?", eleminadoId);
          if(d != null){
            d.delete();
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en los sistemas");
      cuerpoMensaje.put(listJSONNuevos);
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      String[] cuerpoMensaje = {"Se ha producido un error en  guardar los sistemas", e.toString()};
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "error");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      response.status(500);
      rpta = rptaMensaje.toString();
      e.printStackTrace();
    } finally {
      db.close();
    }
    return rpta;
  };

  public static Route menuModulos = (Request request, Response response) -> {
    String rpta = "";
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<ViewSistemaModulo> rptaList = ViewSistemaModulo.find("sistema_id = ?", sistemaId);
      for (ViewSistemaModulo sistemaModulo : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("url", sistemaModulo.get("url"));
        obj.put("nombre", sistemaModulo.get("nombre_modulo"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en el menú de módulos del sistema", e.toString()};
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

  public static Route existeUsuario = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      String usuario = request.queryParams("usuario");
      String sistemaId = request.queryParams("sistema_id");
      db.open();
      System.out.println("1A +++++++++++++++++++++++++++++++++++++++");
      System.out.println(request.body());
      System.out.println(request.uri());
      System.out.println(usuario);
      System.out.println(sistemaId);
      System.out.println("2A +++++++++++++++++++++++++++++++++++++++");
      rpta = ViewUsuarioSistema.count("usuario = ? AND id = ?", usuario, sistemaId) + "";
    }catch (Exception e) {
      e.printStackTrace();
      String[] error = {"Se ha producido un error en validar si el usuario tiene acceso a dicho sistema", e.toString()};
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
}