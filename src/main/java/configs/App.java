package configs;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import static spark.Spark.exception;
import static spark.Spark.staticFiles;
import static spark.Spark.port;
import static spark.Spark.options;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.post;
import spark.ModelAndView;
import spark.template.velocity.*;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import handlers.DepartamentoHandler;
import handlers.DistritoHandler;
import handlers.HomeHandler;
import handlers.ProvinciaHandler;

public class App {
  public static void main(String args[]){
    exception(Exception.class, (e, req, res) -> e.printStackTrace());
		staticFiles.location("/public");
		staticFiles.header("Access-Control-Allow-Origin", "*");
		staticFiles.header("Access-Control-Request-Method",  "*");
		staticFiles.header("Access-Control-Allow-Headers",  "*");
		//staticFiles.expireTime(600);
		//puerto
		port(2000);
		//CORS
		options("/*", (request, response) -> {
			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}
			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}
			return "OK";
		});
		//before filter
		before((request, response) -> {
			response.header("Access-Control-Allow-Origin", "*");
			response.header("Access-Control-Request-Method",  "*");
			response.header("Access-Control-Allow-Headers",  "*");
			response.header("Access-Control-Allow-Credentials", "true");
			response.header("Server",  "Ubuntu, Jetty");
			// Note: this may or may not be necessary in your particular application
			//response.type("application/json");
		});
		//ruta de test/conexion
		get("/test/conexion", (request, response) -> {
			return "Conxión OK";
		});	
		//rutas a handlers
		get("/", HomeHandler.index);
		get("/departamento/listar", DepartamentoHandler.listar);
		post("/departamento/guardar", DepartamentoHandler.guardar);
		get("/distrito/listar/:provincia_id", DistritoHandler.listar);
		get("/distrito/buscar", DistritoHandler.buscar);
		//post("/distrito/guardar", DistritoHandler.guardar);
		get("/provincia/listar/:departamento_id", ProvinciaHandler.listar);
		post("/provincia/guardar", ProvinciaHandler.guardar);
  }

  public static String renderTemplate(String template, Map model) {
		//usar velocity como motor de templates
    Config constants = ConfigFactory.parseResources("configs/application.conf");
		model.put("constants", constants);
		VelocityTemplateEngine vt = new VelocityTemplateEngine();
		ModelAndView mv = new ModelAndView(model, template);		
		String rptaLatin = vt.render(mv);
		try {
			byte[] isoBytes = rptaLatin.getBytes("ISO-8859-1");
			return new String(isoBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error en codificación de vista Apache Velocity";
		}
		//HelperView hv = new HelperView();
		//rpta = hv.correcionUTF8(rpta);	
  }
}