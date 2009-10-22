package se.bluebrim.desktop.jdbc.connect;

import java.util.Properties;

import se.bluebrim.crud.esox.DirtyPredicateModel;

/**
 * 
 * @author GStack
 * 
 */
public class JdbcConnectionModel extends DirtyPredicateModel
{
	private static final String PASSWORD = "password";
	private static final String USER_NAME = "userName";
	private static final String DATABASE_NAME = "databaseName";
	private static final String PORT = "port";
	private static final String HOST_NAME = "hostName";
	
	private String hostName;
	private Integer port;
	private String databaseName;
	private String userName;
	private String password;

	public String getHostName()
	{
		return hostName;
	}

	public void setHostName(String hostName)
	{
		if (!equals(this.hostName, hostName))
		{
			this.hostName = hostName;
			fireValueChanged(HOST_NAME, hostName);
		}
	}

	public Integer getPort()
	{
		return port;
	}

	public void setPort(Integer port)
	{
		if (!equals(this.port, port))
		{
			this.port = port;
			fireValueChanged(PORT, port);
		}
	}

	public String getDatabaseName()
	{
		return databaseName;
	}

	public void setDatabaseName(String databaseName)
	{
		if (!equals(this.databaseName, databaseName))
		{
			this.databaseName = databaseName;
			fireValueChanged(DATABASE_NAME, databaseName);
		}
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		if (!equals(this.userName, userName))
		{
			this.userName = userName;
			fireValueChanged(USER_NAME, userName);
		}
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		if (!equals(this.password, password))
		{
			this.password = password;
			fireValueChanged(PASSWORD, password);
		}
	}

	public void write(Properties properties)
	{
		properties.put(PASSWORD, password);
		properties.put(USER_NAME, userName);
		properties.put(DATABASE_NAME, databaseName);
		properties.put(PORT, port.toString());
		properties.put(HOST_NAME, hostName);		
	}

	public void readFrom(Properties properties)
	{
		password = properties.getProperty(PASSWORD);
		userName = properties.getProperty(USER_NAME);
		databaseName = properties.getProperty(DATABASE_NAME);
		port = Integer.valueOf(properties.getProperty(PORT));
		hostName = properties.getProperty(HOST_NAME);		
	}

}
