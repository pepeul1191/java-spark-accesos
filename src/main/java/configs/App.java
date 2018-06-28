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
import configs.FilterHandler;
import handlers.LoginHandler;
import handlers.HomeHandler;
import handlers.SistemaHandler;
import handlers.ModuloHandler;
import handlers.SubtituloHandler;
import handlers.ItemHandler;
import handlers.PermisoHandler;
import handlers.RolHandler;
import handlers.UsuarioHandler;
import handlers.EstadoUsuarioHandler;
import handlers.ErrorHandler;

public class App {
  public static void main(String args[]){
    exception(Exception.class, (e, req, res) -> e.printStackTrace());
		staticFiles.location("/public");
		staticFiles.header("Access-Control-Allow-Origin", "*");
		staticFiles.header("Access-Control-Request-Method",  "*");
		staticFiles.header("Access-Control-Allow-Headers",  "*");
		//staticFiles.expireTime(600);
		//puerto
		port(4000);
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
		//filters
		before("*", FilterHandler.setHeaders);
		before("*", FilterHandler.ambinteLogs);
		before("/sistema/*", FilterHandler.ambienteCSRF);
		before("/item/*", FilterHandler.ambienteCSRF);
		before("/modulo/*", FilterHandler.ambienteCSRF);
		before("/subtitulo/*", FilterHandler.ambienteCSRF);
		before("/item/*", FilterHandler.ambienteCSRF);
		before("/permiso/*", FilterHandler.ambienteCSRF);
		before("/rol/*", FilterHandler.ambienteCSRF);
		before("/estado_usuario/*", FilterHandler.ambienteCSRF);
		before("/usuario/*", FilterHandler.ambienteCSRF);
		//ruta de test/conexion
		get("/test/conexion", (request, response) -> {
			return "Conxión OK";
		});
		get("/", (request, response) -> {
			//Config constants = ConfigFactory.parseResources("config/application.conf");
			//constants.getString("base_url") + 
			response.redirect("login");
      return "";
		});
		get("/accesos", (request, response) -> {
			response.redirect("/accesos/");
			return "";
		});
		//rutas a login
		before("/login", FilterHandler.sessionFalse);
		get("/login", LoginHandler.index);
		post("/login/acceder", LoginHandler.acceder);
		get("/login/cerrar", LoginHandler.cerrar);
		//rutas a error
		get("/access/error/:error", ErrorHandler.index);
		//rutas de vista para usar los servicios REST
		before("/accesos/", FilterHandler.sessionTrue);
		get("/accesos/", HomeHandler.index);
		//rutas de servicios REST a handlers
		get("/sistema/listar", SistemaHandler.listar);
		post("/sistema/guardar", SistemaHandler.guardar);
		get("/modulo/listar/:sistema_id", ModuloHandler.listar);
		post("/modulo/guardar", ModuloHandler.guardar);
		get("/modulo/menu/:sistema_id", SistemaHandler.menuModulos);
		get("/subtitulo/listar/:modulo_id", SubtituloHandler.listar);
		post("/subtitulo/guardar", SubtituloHandler.guardar);
		get("/item/listar/:subtitulo_id", ItemHandler.listar);
		get("/item/menu/:modulo_id", ItemHandler.menu);
		post("/item/guardar", ItemHandler.guardar);
		get("/permiso/listar/:sistema_id", PermisoHandler.listar);
		post("/permiso/guardar", PermisoHandler.guardar);
		get("/rol/listar/:sistema_id", RolHandler.listar);
		post("/rol/guardar", RolHandler.guardar);
		get("/rol/permiso/listar/:sistema_id/:rol_id", RolHandler.listarPermisos);
		post("/rol/permiso/guardar", RolHandler.guardarPermisos);
		get("/estado_usuario/listar", EstadoUsuarioHandler.listar);
		get("/usuario/listar", UsuarioHandler.listar);
		get("/usuario/obtener_usuario_correo/:usuario_id", UsuarioHandler.usuarioCorreo);
		post("/usuario/nombre_repetido", UsuarioHandler.nombreRepetido);
		post("/usuario/contrasenia_repetida", UsuarioHandler.contraseniaRepetida);
		post("/usuario/correo_repetido", UsuarioHandler.correoRepetido);
		post("/usuario/guardar_usuario_correo", UsuarioHandler.guardarUsuarioCorreo);
		post("/usuario/guardar_contrasenia", UsuarioHandler.guardarContrasenia);
		post("/usuario/validar", UsuarioHandler.validar);
		get("/usuario/sistema/:usuario_id", UsuarioHandler.listarSistemas);
		post("/usuario/sistema/guardar", UsuarioHandler.guardarSistemas);
		get("/usuario/rol/:sistema_id/:usuario_id", UsuarioHandler.listarUsuarioSistemaRoles);
		post("/usuario/rol/guardar", UsuarioHandler.guardarSistemaRoles);
		get("/usuario/permiso/:sistema_id/:usuario_id", UsuarioHandler.listarUsuarioSistemaPermisos);
		post("/usuario/permiso/guardar", UsuarioHandler.guardarSistemaPermisos);
		//errors si no encuentra recurso
		get("/*", ErrorHandler.errorGET);
		post("/*", ErrorHandler.errorPOST);
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