package se.bluebrim.view.example.dbschemagraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.UIManager;


/**
 * Migrate the database to the current schema by running scripts listed in the
 * "install/ListOfSQLFiles.txt" that is <strong>not</strong> specified in the
 * IP_SQLScriptMgmt table. This table is updated by this class and contains the name
 * of all migration scripts applied to the database. <br>
 * If the database don't exists the database is created and all scripts specified
 * in the "install/ListOfSQLFiles.txt" is run.
 * 
 * @deprecated use c5-db-migration instead
 * @author GStack
 *
 */
public class SchemaMigrationTool
{
	private interface LineVisitor
	{
		public void visit(String line);
	}
	
	private static class SqlScript
	{
		enum Type {BASE, PATCH};
		String version;
		Type type;
		String description;
		String filePath;
		
		public SqlScript(String semiColonSeparatedProperties)
		{
			String[] parts = semiColonSeparatedProperties.split(";");
			version = parts[0];
			type = Type.valueOf(parts[1]);
			description = parts[2];
			filePath = parts[3];
		}

		public SqlScript(ResultSet rs) throws SQLException
		{
			filePath = rs.getString("SqlPatchName").toLowerCase();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return ((SqlScript)obj).filePath.equalsIgnoreCase(filePath);
		}
		
		@Override
		public int hashCode()
		{
			return filePath.hashCode();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		new SchemaMigrationTool().run(null);
	}

	
	public void run(String databaseName) throws Exception
	{
    Class.forName("net.sourceforge.jtds.jdbc.Driver"); // Load the JDBC driver
    Connection connection = databaseName == null ? createConnection(true) : createConnection(false);
		runInTransaction(databaseName, connection);
		connection.close();
	}


	/**
	 * Create a connection to the database server without specifying any particular
	 * database. This kind of connection is used for running a different database from
	 * the database specified in the properties file.
	 */
	public static Connection createConnectionNoParticularDatabase() throws Exception
	{
		return createConnection(false);
	}
		
	private static Connection createConnection(boolean useDatabaseInProperties) throws Exception
	{
		String userName = System.getProperty("user.name");
		String userSpecificFilename = "/iserver." + userName + ".properties";
		Properties properties = new Properties();
		properties.load(SchemaMigrationTool.class.getResourceAsStream(userSpecificFilename));
		String password = properties.getProperty("db.password");
		String user = properties.getProperty("db.user");
		String dbUrl = properties.getProperty("db.dsn");
		if (!useDatabaseInProperties)
			dbUrl = removeDatabaseName(dbUrl);
		return DriverManager.getConnection(dbUrl, user, password);	
	}


	private static String removeDatabaseName(String dbUrl)
	{
		int indexOf = dbUrl.indexOf(";databasename");
		if (indexOf > -1)
			return dbUrl.substring(0, indexOf);
		else
			return dbUrl;
	}


	/**
	 * Should run in transaction as the name implies, but when tried that got the following message:
	 * "ALTER DATABASE statement not allowed within multi-statement transaction"
	 */
	private void runInTransaction(String databaseName, Connection connection) throws SQLException, IOException
	{
		Statement statement = connection.createStatement();
		if (databaseName != null)
		{
			createDataBaseIfNotExist(databaseName, connection);
			statement.execute("use " + databaseName);
		}
		createSQLScriptMgmtTable(statement);
		List<SqlScript> appliedSqlScripts = readAppliedScripts(statement);
		File baseDir = new File ("../IsacServer/runtime");
		File listOfSqlFiles = new File(baseDir, "install/ListOfSQLFiles.txt");

		List<SqlScript> sqlScripts = getSqlScripts(listOfSqlFiles);
		String databaseURL = connection.getMetaData().getURL() + (databaseName == null ? "" : ";databasename=" + databaseName);
		sqlScripts.removeAll(appliedSqlScripts);
		if (confirmedByUser(databaseURL, sqlScripts))
		{
			for (SqlScript sqlScript : sqlScripts)
			{
				updateDatabase(statement, baseDir, databaseURL, sqlScript);
			}
			System.out.println("The following script(s) was applied to the database: \"" + databaseURL + "\"");
			for (SqlScript sqlScript : sqlScripts)	
				System.out.println(sqlScript.filePath);	
		} else
			System.out.println("No changes where done in the database: \"" + databaseURL + "\"");
				
		statement.close();
	}

	
	private boolean confirmedByUser(String databaseURL, List<SqlScript> sqlScripts)
	{
		String message = "<html>Do you want to update the the schema in the database:<br><br> " + databaseURL + "<br><br>with the script(s): " + getSqlScriptNamesAsHtml(sqlScripts) + "</html>";
		int answer = JOptionPane.showConfirmDialog(null, message, UIManager.getString("OptionPane.titleText"), JOptionPane.YES_NO_OPTION);
		return answer == JOptionPane.YES_OPTION;
	}

	private String getSqlScriptNamesAsHtml(List<SqlScript> sqlScripts)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("<ol>");
		for (SqlScript sqlScript : sqlScripts)
		{
			buffer.append("<li>" + sqlScript.filePath + "</li>");
		}
		buffer.append("</ol>");

		return buffer.toString();
	}


	/**
	 * Update the database by running the specified SQL script.
	 */
	private void updateDatabase(Statement statement, File baseDir, String databaseURL, SqlScript sqlScript) throws IOException, SQLException
	{
		File sqlScriptFile = new File(baseDir, sqlScript.filePath);
		if (sqlScript.type != SqlScript.Type.BASE)	// TODO: Temp until all databases have the base script in the management table as well
		{
			List<String> sqls = splitAtGo(sqlScriptFile);
			for (String sql : sqls)
			{
				statement.execute(sql);
			}
		}
		writeAppliedScript(statement, sqlScript);
		System.out.println("Updated the database: \"" + databaseURL + "\" with sql script: \"" + sqlScript.filePath + "\"");
	}

	private void createSQLScriptMgmtTable(Statement statement) throws SQLException
	{
		statement.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name = 'IP_SQLScriptMgmt') " +
				"BEGIN  " +
					"PRINT 'Create IP_SQLScriptMgmt table' " +
					"CREATE TABLE [IP_SQLScriptMgmt]([SqlPatchName] [varchar](250) NOT NULL) " +
				"END");		
	}
	
	private List<SqlScript> readAppliedScripts(Statement statement) throws SQLException
	{
		List<SqlScript> appliedScripts = new ArrayList<SqlScript>();
		ResultSet rs = statement.executeQuery("SELECT SqlPatchName FROM IP_SQLScriptMgmt");
		while (rs.next())
		{
			appliedScripts.add(new SqlScript(rs));
		}
		return appliedScripts;
	}
	
	private void writeAppliedScript(Statement statement, SqlScript sqlScript) throws SQLException
	{
		statement.execute("INSERT INTO IP_SQLScriptMgmt (SqlPatchName) VALUES ('"+ sqlScript.filePath +"')");
	}

	private void createDataBaseIfNotExist(String databaseName, Connection connection) throws SQLException
	{
		Statement statement = connection.createStatement();
		statement.execute("if not exists(select * from sys.databases where name = '" + databaseName + "') create database " + databaseName);
	}
	
	/**
	 * Split the specified script file into several scripts. One for each GO statement.
	 */
	private List<String> splitAtGo(File sqlScriptFile) throws IOException
	{
		final List<String> splits = new ArrayList<String>();
		eachLine(sqlScriptFile, new LineVisitor(){
			
			StringBuffer buffer = new StringBuffer();

			@Override
			public void visit(String line)
			{
				line = line.trim();
				if (line.length() < 0)
					return;
				if (line.equalsIgnoreCase("GO"))
				{
					splits.add(buffer.toString());
					buffer = new StringBuffer();
				} else
					buffer.append(line + "\n");
			}});
		return splits;
	}
	
	private List<SqlScript> getSqlScripts(File file) throws IOException
	{
		final List<SqlScript> scripts = new ArrayList<SqlScript>();
		eachLine(file, new LineVisitor()
		{
			@Override
			public void visit(String line)
			{
				line = line.trim();
				if (line.length() < 1 || line.startsWith("#"))
					return;
				else
				{
					scripts.add(new SqlScript(line));
				}
			}
		});
		return scripts;

	}

	private void eachLine(File file, LineVisitor visitor) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		for (String line; (line = reader.readLine()) != null;)
			visitor.visit(line);
		reader.close();		
	}
}
