package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import configs.Database;
import models.Modulo;

public class ModuloHandler{
  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Modulo> rptaList = Modulo.find("sistema_id = ?", sistemaId);
      for (Modulo modulo : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", modulo.get("id"));
        obj.put("nombre", modulo.get("nombre"));
        obj.put("url", modulo.get("url"));
        obj.put("icono", modulo.get("icono"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los módulos", e.toString()};
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
          JSONObject modulo = nuevos.getJSONObject(i);
          String antiguoId = modulo.getString("id");
          String nombre = modulo.getString("nombre");
          String url = modulo.getString("url");
          String icono = modulo.getString("icono");
          Modulo n = new Modulo();
          n.set("nombre", nombre);
          n.set("url", url);
          n.set("icono", icono);
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
          JSONObject modulo = editados.getJSONObject(i);
          int id = modulo.getInt("id");
          String nombre = modulo.getString("nombre");
          String url = modulo.getString("url");
          String icono = modulo.getString("icono");
          Modulo e = Modulo.findFirst("id = ?", id);
          if(e != null){
            e.set("nombre", nombre);
            e.set("url", url);
            e.set("icono", icono);
            e.saveIt();
          }
        }
      }
      if(eliminados.length() > 0){
        for (Object eliminado : eliminados) {
          String eleminadoId = (String)eliminado;
          Modulo d = Modulo.findFirst("id = ?", eleminadoId);
          if(d != null){
            d.delete();
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en los módulos del sitema");
      cuerpoMensaje.put(listJSONNuevos);
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en  guardar los módulos del sitema", e.toString()};
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