package handlers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import java.util.HashMap;
import java.util.Map;
import configs.App;
import helpers.LoginHelper;

public class LoginHandler {
  public static Route index = (Request request, Response response) -> {
    LoginHelper helper = new LoginHelper();
    Config constants = ConfigFactory.defaultApplication();
    Map<String, Object> model = new HashMap<>();
    model.put("partial", "templates/login/index.vm");
    model.put("title", "Bienvenido");
    model.put("constants", constants);
    model.put("mensaje", "");
    model.put("load_css", helper.indexCSS());
    model.put("load_js", helper.indexJS());
    return App.renderTemplate("templates/layouts/blank.vm", model);
  };

  public static Route acceder = (Request request, Response response) -> {
    Config constants = ConfigFactory.defaultApplication();
    String csrfKey = constants.getString("csrf.key");
    String csrfValue = constants.getString("csrf.secret");
    boolean continuar = true;
    String mensaje = "";
    try{
      String csrfRequestValue = request.queryParams(csrfKey);
      //validar csrf token
      if(!csrfRequestValue.equalsIgnoreCase(csrfValue) ){
        mensaje = "Token CSRF no es el correcto";
        continuar = false;
      }
      //validar usuario y contraseña si csrf token es correcto
      if(continuar == true){
        String usuarioConfig = constants.getString("login.usuario");
        String contraseniaConfig = constants.getString("login.contrasenia");
        String usuarioRequest = request.queryParams("usuario");
        String contraseniaRequest = request.queryParams("contrasenia");
        if(!usuarioConfig.equalsIgnoreCase(usuarioRequest) || !contraseniaConfig.equalsIgnoreCase(contraseniaRequest)){
          continuar = false;
          mensaje = "Usuario y/o contraenia no coinciden";
        }
      }
    }catch(NullPointerException e){
      //e.printStackTrace();
      mensaje = "Token CSRF no existe en POST request";
      continuar = false;
    }
    if(continuar == true){
      request.session(true);
      request.session().attribute("activo", true);
      String baseURL = constants.getString("base_url");
      response.redirect(baseURL + "accesos/");
      return "";
    }else{
      LoginHelper helper = new LoginHelper();
      Map<String, Object> model = new HashMap<>();
      model.put("partial", "templates/login/index.vm");
      model.put("title", "Bienvenido");
      model.put("constants", constants);
      model.put("mensaje", mensaje);
      model.put("load_css", helper.indexCSS());
      model.put("load_js", helper.indexJS());
      response.status(500);
      return App.renderTemplate("templates/layouts/blank.vm", model);
    }
  };

  public static Route cerrar = (Request request, Response response) -> {
    try{
      request.session().removeAttribute("activo");
    }catch(NullPointerException e){
      //e.printStackTrace();
    }
    Config constants = ConfigFactory.defaultApplication();
    String baseURL = constants.getString("base_url");
    response.redirect(baseURL + "login");
    return "";
  };
}
