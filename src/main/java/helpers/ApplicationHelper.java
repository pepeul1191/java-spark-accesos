package helpers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public abstract class ApplicationHelper {
  private Config constants = ConfigFactory.defaultApplication();
  
  public String getConfValue(String key){
    return constants.getString(key);
  }
  
  public String loadCSS(String[] csss) {
    String rpta = "";
    for (String css : csss) {
      String temp = "<link rel='stylesheet' type='text/css' href='" + constants.getString("static_url") + css + ".css'/>";
      rpta = rpta + temp;
    }
    return rpta;
  }    

  public String loadJS(String[] jss) {
    String rpta = "";
    for (String js : jss) {
      String temp = "<script src='" + constants.getString("static_url") + js + ".js' type='text/javascript'></script>";
      rpta = rpta + temp;
    }
    return rpta;
  }  
}
