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
          "collections/sistema_collection",
          "data/tabla_sistema_data",
          "views/sistema_view",
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
