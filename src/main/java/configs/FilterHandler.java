package configs;

import spark.Filter;
import spark.Request;
import spark.Response;
import static spark.Spark.halt;
import org.json.JSONObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class FilterHandler{
  public static Filter setHeaders = (Request request, Response response) -> {
    response.header("Access-Control-Allow-Origin", "*");
    response.header("Access-Control-Request-Method",  "*");
    response.header("Access-Control-Allow-Headers",  "*");
    response.header("Access-Control-Allow-Credentials", "true");
    response.header("Server",  "Ubuntu, Jetty");
    // Note: this may or may not be necessary in your particular application
    //response.type("application/json");
  };

  public static Filter ambinteLogs = (Request request, Response response) -> {
    Config constants = ConfigFactory.defaultApplication();
    if(constants.getString("ambiente_request_logs").equalsIgnoreCase("activo")){
      System.out.println(request.requestMethod() + " - " + request.pathInfo());
    }
  };

  public static Filter ambienteCSRF = (Request request, Response response) -> {
    Config constants = ConfigFactory.defaultApplication();
    if(constants.getString("ambiente_csrf").equalsIgnoreCase("activo")){
      String csrfKey = constants.getString("csrf.key");
      String csrfValue = constants.getString("csrf.secret");
      String[] error = new String[2];
      boolean continuar = true;
      try{
        String csrfRequestValue = request.headers(csrfKey);
        if(!csrfRequestValue.equalsIgnoreCase(csrfValue) ){
          error[0] = "No se puede acceder al recurso"; 
          error[1] = "CSRF Token error";
          continuar = false;
        }
      }catch(NullPointerException e){
        //e.printStackTrace();
        error[0] = "No se puede acceder al recurso"; 
        error[1] = "CSRF Token key error";
        continuar = false;
      }
      if(continuar == false){
        JSONObject rptaJSON = new JSONObject();
        rptaJSON.put("tipo_mensaje", "error");
        rptaJSON.put("mensaje", error);
        String rpta = rptaJSON.toString();
        halt(500, rpta);
      }      
    }
  };

  public static Filter sessionTrue = (Request request, Response response) -> {
    Config constants = ConfigFactory.defaultApplication();
    if(constants.getString("ambiente_session").equalsIgnoreCase("activo")){
      boolean error = false;
      request.session(true);
      try {
        Boolean sessionActiva = (Boolean)request.session().attribute("activo");
        if(sessionActiva == true){
          error = false;
        }
      } catch (java.lang.NullPointerException e) {
        error = true;
      }
      if(error == true){
        String baseURL = constants.getString("base_url");
        response.redirect(baseURL + "access/error/505");
      }
    }
  };

  public static Filter sessionFalse = (Request request, Response response) -> {
    Config constants = ConfigFactory.defaultApplication();
    if(constants.getString("ambiente_session").equalsIgnoreCase("activo")){
      boolean error = false;
      request.session(true);
      try {
        Boolean sessionActiva = (Boolean)request.session().attribute("activo");
        if(sessionActiva == true){
          error = false;
        }
      } catch (java.lang.NullPointerException e) {
        error = true;
      }
      if(error == false){
        String baseURL = constants.getString("base_url");
        response.redirect(baseURL + "accesos/");
      }
    }
  };
}