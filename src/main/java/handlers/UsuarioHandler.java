package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import configs.Database;
import models.Usuario;
import models.Acceso;
import models.ViewUsuarioCorreoEstado;
import models.ViewUsuarioSistema;
import models.UsuarioSistema;
import models.UsuarioPermiso;
import models.UsuarioRol;
import models.ViewUsuarioRol;
import models.ViewUsuarioPermiso;

public class UsuarioHandler{
  public static Route validar = (Request request, Response response) -> {
    String rpta = "";
    boolean continuar = true;
    String[] error = new String[2];
    Database db = new Database();
    try {
      Config constants = ConfigFactory.defaultApplication();
      if(constants.getString("ambiente_csrf").equalsIgnoreCase("activo")){
        String csrfKey = constants.getString("csrf.key");
        String csrfValue = constants.getString("csrf.secret");
        String csrfRequestValue = request.headers(csrfKey);
        if(!csrfRequestValue.equalsIgnoreCase(csrfValue) ){
          error[0] = "No se puede acceder al recurso"; 
          error[1] = "CSRF Token error";
          continuar = false;
        }
      }
      if(continuar == true){
        String usuario = request.queryParams("usuario");
        String contrasenia = request.queryParams("contrasenia");
        db.open();
        rpta = Usuario.count("usuario = ? AND contrasenia = ?", usuario, contrasenia) + "";
        if (rpta.equalsIgnoreCase("1")){
          //guardar acceso
          Usuario u = Usuario.findFirst("usuario = ? AND contrasenia = ?", usuario, contrasenia);
          if(u != null){
            int usuarioId = u.getInteger("id");
            Acceso n = new Acceso();
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
            n.set("momento", sqlDate);
            n.set("usuario_id", usuarioId);
            n.saveIt();
          }
        }
      }else{
        JSONObject rptaTry = new JSONObject();
        rptaTry.put("tipo_mensaje", "error");
        rptaTry.put("mensaje", error);
        rpta = rptaTry.toString();
        response.status(500);
      }
    }catch(NullPointerException e){
      e.printStackTrace();
      error[0] = "CSRF Token key error"; 
      error[1] = "CSRF no presente en el form del POST";
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", error);
      rpta = rptaTry.toString();
      response.status(500);
    }catch (Exception e) {
      e.printStackTrace();
      error[0] = "Se ha producido un error en validar al usuario"; 
      error[1] = e.toString();
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

  public static Route validarREST = (Request request, Response response) -> {
    String rpta = "";
    String usuario = request.queryParams("usuario");
    String contrasenia = request.queryParams("contrasenia");
    System.out.println("1B +++++++++++++++++++++++++++++++++++++++");
    System.out.println(request.body());
    System.out.println(usuario);
    System.out.println(contrasenia);
    System.out.println("2B +++++++++++++++++++++++++++++++++++++++");
    Database db = new Database();
    try {
      db.open();
      rpta = Usuario.count("usuario = ? AND contrasenia = ?", usuario, contrasenia) + "";
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en obtener validar el usuario y correo", e.toString()};
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

  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Usuario> rptaList = Usuario.findAll();
      for (Usuario usuario : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", usuario.get("id"));
        obj.put("usuario", usuario.get("usuario"));
        obj.put("correo", usuario.get("correo"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los usuarios", e.toString()};
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

  public static Route usuarioCorreo = (Request request, Response response) -> {
    String rpta = "";
    int usuarioId = Integer.parseInt(request.params(":usuario_id"));
    Database db = new Database();
    try {
      db.open();
      ViewUsuarioCorreoEstado usuario = ViewUsuarioCorreoEstado.findFirst("id = ?", usuarioId);
      JSONObject obj = new JSONObject();
      obj.put("id", usuario.get("id"));
      obj.put("usuario", usuario.get("usuario"));
      obj.put("correo", usuario.get("correo"));
      obj.put("estado_usuario_id", usuario.get("estado_usuario_id"));
      obj.put("estado_usuario_nombre", usuario.get("estado_usuario_nombre"));
      rpta = obj.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  obtener el usuario y correo", e.toString()};
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

  public static Route nombreRepetido = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      String usuarioId = data.getString("id");
      String usuario = data.getString("usuario");
      db.open();
      rpta = "1";
      if (usuarioId.equalsIgnoreCase("E")){
        //SELECT COUNT"(*) AS cantidad FROM usuarios WHERE usuario = ?
        rpta = Usuario.count("usuario = ?", usuario) + "";
      }else{
        //SELECT COUNT(*) AS cantidad FROM usuarios WHERE usuario = ? AND id = ?
        long cantidad = Usuario.count("usuario = ? AND id = ?", usuario, usuarioId);
        if (cantidad == 1){
          rpta = "0";
        }else{
          //SELECT COUNT(*) AS cantidad FROM usuarios WHERE usuario = ?
          rpta = Usuario.count("usuario = ?", usuario) + "";
        }
      }
    }catch (Exception e) {
      e.printStackTrace();
      String[] errorArray = {"Se ha producido un error en validar si el nombre es repetido", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", errorArray);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route contraseniaRepetida = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      String usuarioId = data.getString("id");
      String contrasenia = data.getString("contrasenia");
      db.open();
      rpta = Usuario.count("contrasenia = ? AND id = ?", contrasenia, usuarioId) + "";
    }catch (Exception e) {
      e.printStackTrace();
      String[] errorArray = {"Se ha producido un error en validar si la contraseña del usuario", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", errorArray);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route correoRepetido = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      String usuarioId = data.getString("id");
      String correo = data.getString("correo");
      db.open();
      rpta = "1";
      if (usuarioId.equalsIgnoreCase("E")){
        //SELECT COUNT(*) AS cantidad FROM usuarios WHERE correo = ?
        rpta = Usuario.count("correo = ?", correo) + "";
      }else{
        //SELECT COUNT(*) AS cantidad FROM usuarios WHERE correo = ? AND id = ?
        long cantidad = Usuario.count("correo = ? AND id = ?", correo, usuarioId);
        if (cantidad == 1){
          rpta = "0";
        }else{
          //SELECT COUNT(*) AS cantidad FROM usuarios WHERE correo = ?
          rpta = Usuario.count("correo = ?", correo) + "";
        }
      }
    }catch (Exception e) {
      e.printStackTrace();
      String[] errorArray = {"Se ha producido un error en validar si el correo es repetido", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", errorArray);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route guardarUsuarioCorreo = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("usuario"));
      String usuarioId = data.getString("id");
      String correo = data.getString("correo");
      String usuario = data.getString("usuario");
      String estado_usuario_id = data.getString("estado_usuario_id");
      db.open();
      Usuario e = Usuario.findFirst("id = ?", usuarioId);
      if(e != null){
        e.set("correo", correo);
        e.set("usuario", usuario);
        e.set("estado_usuario_id", estado_usuario_id);
        e.saveIt();
      }
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en los datos generales del usuario");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] errorArray = {"Se ha producido un error en actualizar el usaurio", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", errorArray);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route guardarContrasenia = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("contrasenia"));
      String usuarioId = data.getString("id");
      String contrasenia = data.getString("contrasenia");
      db.open();
      Usuario e = Usuario.findFirst("id = ?", usuarioId);
      if(e != null){
        e.set("contrasenia", contrasenia);
        e.saveIt();
      }
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha el cambio de contraseña del usuario");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] errorArray = {"Se ha producido un error en actualizar la contraseña del usaurio", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", errorArray);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    return rpta;
  };

  public static Route listarSistemas = (Request request, Response response) -> {
    String rpta = "";
    int usuarioId = Integer.parseInt(request.params(":usuario_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      String sql = 
        "SELECT T.id AS id, T.nombre AS nombre, (CASE WHEN (P.existe = 1) THEN 1 ELSE 0 END) AS existe FROM " +
        "(" +
          "SELECT id, nombre, 0 AS existe FROM sistemas" +
        ") T " +
        "LEFT JOIN " +
        "(" +
          "SELECT S.id, S.nombre, 1 AS existe FROM sistemas S " +
          "INNER JOIN usuarios_sistemas US ON US.sistema_id = S.id  " +
          "WHERE US.usuario_id = ?  " +
        ") P " +
        "ON T.id = P.id";
      List<ViewUsuarioSistema> rptaList = ViewUsuarioSistema.findBySQL(sql, usuarioId);
      for (ViewUsuarioSistema rolPermiso : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", rolPermiso.get("id"));
        obj.put("nombre", rolPermiso.get("nombre"));
        obj.put("existe", rolPermiso.get("existe"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los sistema del usuario", e.toString()};
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

  public static Route guardarSistemas = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray editados = data.getJSONArray("editados");
      int usuarioId = data.getJSONObject("extra").getInt("usuario_id");
      db.open();
      db.getDb().openTransaction();
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject usuarioSistema = editados.getJSONObject(i);
          int sistemaId = usuarioSistema.getInt("id");
          int existe = usuarioSistema.getInt("existe");
          UsuarioSistema e = UsuarioSistema.findFirst("sistema_id = ? AND usuario_id = ?", sistemaId, usuarioId);
          if (existe == 0){//borrar si existe
            if(e != null){
              e.delete();
            }
          }else if(existe == 1){//crear si no existe
            if(e == null){
              UsuarioSistema n = new UsuarioSistema();
              n.set("sistema_id", sistemaId);
              n.set("usuario_id", usuarioId);
              n.saveIt();
            }
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado la asociación de sistemas al usuario");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en asociar los sistemas al usuario", e.toString()};
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

  public static Route listarUsuarioSistemaRoles = (Request request, Response response) -> {
    String rpta = "";
    int usuarioId = Integer.parseInt(request.params(":usuario_id"));
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      String sql = 
        "SELECT T.id AS id, T.nombre AS nombre, (CASE WHEN (P.existe = 1) THEN 1 ELSE 0 END) AS existe FROM " +
        "(" +
          "SELECT id, nombre, 0 AS existe FROM roles WHERE sistema_id = ?" + 
        ") T " + 
        "LEFT JOIN " +
        "(" +
          "SELECT R.id, R.nombre, 1 AS existe  FROM roles R " +
          "INNER JOIN usuarios_roles UR ON R.id = UR.rol_id " +
          "WHERE UR.usuario_id = ? " +
        ") P " +
        "ON T.id = P.id";
      List<ViewUsuarioRol> rptaList = ViewUsuarioRol.findBySQL(sql, sistemaId,  usuarioId);
      for (ViewUsuarioRol usurioRol : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", usurioRol.get("id"));
        obj.put("nombre", usurioRol.get("nombre"));
        obj.put("existe", usurioRol.get("existe"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en listar los roles del usuario", e.toString()};
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

  public static Route guardarSistemaRoles = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray editados = data.getJSONArray("editados");
      int usuarioId = data.getJSONObject("extra").getInt("usuario_id");
      db.open();
      db.getDb().openTransaction();
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject usuarioRol = editados.getJSONObject(i);
          int rolId = usuarioRol.getInt("id");
          int existe = usuarioRol.getInt("existe");
          UsuarioRol e = UsuarioRol.findFirst("rol_id = ? AND usuario_id = ?", rolId, usuarioId);
          if (existe == 0){//borrar si existe
            if(e != null){
              e.delete();
            }
          }else if(existe == 1){//crear si no existe
            if(e == null){
              UsuarioRol n = new UsuarioRol();
              n.set("rol_id", rolId);
              n.set("usuario_id", usuarioId);
              n.saveIt();
            }
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado la asociación de roles al usuario");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en asociar los roles al usuario", e.toString()};
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

  public static Route listarUsuarioSistemaPermisos = (Request request, Response response) -> {
    String rpta = "";
    int usuarioId = Integer.parseInt(request.params(":usuario_id"));
    int sistemaId = Integer.parseInt(request.params(":sistema_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      String sql = 
        "SELECT T.id AS id, T.nombre AS nombre, (CASE WHEN (P.existe = 1) THEN 1 ELSE 0 END) AS existe, T.llave AS llave FROM "+
        "(" +
          "SELECT id, nombre, llave, 0 AS existe FROM permisos WHERE sistema_id = ?" +
        ") T " +
        "LEFT JOIN " +
        "(" +
          "SELECT P.id, P.nombre,  P.llave, 1 AS existe  FROM permisos P " +
          "INNER JOIN usuarios_permisos UP ON P.id = UP.permiso_id " +
          "WHERE UP.usuario_id = ? " +
          ") P " +
          "ON T.id = P.id";
      List<ViewUsuarioPermiso> rptaList = ViewUsuarioPermiso.findBySQL(sql, sistemaId,  usuarioId);
      for (ViewUsuarioPermiso usuarioPermiso : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", usuarioPermiso.get("id"));
        obj.put("nombre", usuarioPermiso.get("nombre"));
        obj.put("existe", usuarioPermiso.get("existe"));
        obj.put("llave", usuarioPermiso.get("llave"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en listar los permisos del usuario", e.toString()};
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

  public static Route guardarSistemaPermisos = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray editados = data.getJSONArray("editados");
      int usuarioId = data.getJSONObject("extra").getInt("usuario_id");
      db.open();
      db.getDb().openTransaction();
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject usuarioPermiso = editados.getJSONObject(i);
          int permisoId = usuarioPermiso.getInt("id");
          int existe = usuarioPermiso.getInt("existe");
          UsuarioPermiso e = UsuarioPermiso.findFirst("permiso_id = ? AND usuario_id = ?", permisoId, usuarioId);
          if (existe == 0){//borrar si existe
            if(e != null){
              e.delete();
            }
          }else if(existe == 1){//crear si no existe
            if(e == null){
              UsuarioPermiso n = new UsuarioPermiso();
              n.set("permiso_id", permisoId);
              n.set("usuario_id", usuarioId);
              n.saveIt();
            }
          }
        }
      }
      db.getDb().commitTransaction();
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado la asociación de permisos al usuario");
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }catch (Exception e) {
      e.printStackTrace();
      String[] cuerpoMensaje = {"Se ha producido un error en asociar los permisos al usuario", e.toString()};
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