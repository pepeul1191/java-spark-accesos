package configs;

import org.javalite.activejdbc.DB;
import org.javalite.activejdbc.DBException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Database{
  private DB db;
	
  public Database(){
    this.db = new DB();
  }

  public DB getDb(){
    return this.db; 
  }

  public void open() throws DBException{
    Config constants = ConfigFactory.defaultApplication();
    String driver = constants.getString("database.driver");
    String url = constants.getString("database.url");
    String user = constants.getString("database.user");
    String password = constants.getString("database.password");
	  this.db.open(driver, url, user, password);
  }

  public void close(){
    this.db.close();
  }
}