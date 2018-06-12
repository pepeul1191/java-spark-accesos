package helpers;

public class HomeHelper extends ApplicationHelper{
  public String indexCSS() {
    switch(getConfValue("ambiente")) {
      case "desarrollo":
        return this.loadCSS(new String[] {
          "bower_components/bootstrap/dist/css/bootstrap.min",
          "bower_components/font-awesome/css/font-awesome.min",
          "bower_components/swp-backbone/assets/css/constants",
          "bower_components/swp-backbone/assets/css/dashboard",
          "bower_components/swp-backbone/assets/css/table",
          "bower_components/swp-backbone/assets/css/autocomplete",
          "assets/css/constants",
          "assets/css/styles",
        });
      case "produccion":
        return this.loadCSS(new String[] {
          "dist/accesos.min"
        });
      default:
        return this.loadCSS(new String[] {});
    }
  }  
  
  public String indexJS() {
    switch(getConfValue("ambiente")) {
      case "desarrollo":
        return this.loadJS(new String[] {
          "bower_components/jquery/dist/jquery.min",
          "bower_components/bootstrap/dist/js/bootstrap.min",
          "bower_components/underscore/underscore-min",
          "bower_components/backbone/backbone-min",
          "bower_components/handlebars/handlebars.min",
          "bower_components/swp-backbone/layouts/application",
          "bower_components/swp-backbone/views/table",
          "bower_components/swp-backbone/views/modal",
          "bower_components/swp-backbone/views/upload",
          "bower_components/swp-backbone/views/autocomplete",
          "models/sistema",
          "models/modulo",
          "models/subtitulo",
          "models/item",
          "models/permiso",
          "collections/sistema_collection",
          "collections/modulo_collection",
          "collections/subtitulo_collection",
          "collections/item_collection",
          "collections/permiso_collection",
          "data/tabla_sistema_data",
          "data/tabla_sistema_permiso_data",
          "data/modal_sistema_menu_data",
          "data/modal_sistema_permiso_data",
          "data/modal_sistema_rol_data",
          "data/tabla_sistema_modulo_data",
          "data/tabla_modulo_subtitulo_data",
          "data/tabla_subtitulo_item_data",
          "views/sistema_view",
          "views/sistema_menu_view",
          "views/sistema_permiso_view",
          "views/sistema_rol_view",
          "routes/accesos",
        });
      case "produccion":
        return this.loadJS(new String[] {
          "dist/accesos.min"
        });
      default:
        return this.loadJS(new String[] {});
    }
  }  
}
