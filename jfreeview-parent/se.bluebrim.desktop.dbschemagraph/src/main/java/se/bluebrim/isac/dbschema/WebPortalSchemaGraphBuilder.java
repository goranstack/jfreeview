package se.bluebrim.isac.dbschema;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Subclass to create a schema of the web portal database.
 * 
 * @author GStack
 *
 */
public class WebPortalSchemaGraphBuilder extends SchemaGraphBuilder
{
	
	public static void main(String[] args) throws Exception
	{
		new WebPortalSchemaGraphBuilder().run();
	}

	public WebPortalSchemaGraphBuilder() throws Exception
	{
		super("bluebrimPortal");
	}
	
	@Override
	protected Connection createConnection(String databaseName) throws Exception
	{
	   Class.forName("net.sourceforge.jtds.jdbc.Driver"); // Load the JDBC driver
	   return DriverManager.getConnection("jdbc:jtds:sqlserver://192.168.211.11:1433;databasename=bluebrimPortal", "sa", "sa");	
	}
	
	@Override
	protected void readSQLManagementTable() throws SQLException
	{
	}
	
	@Override
	protected String getSchemaPropertiesFilePath()
	{
		return "web-portal-config/schema.properties";
	}

}
