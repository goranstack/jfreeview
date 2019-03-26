package se.bluebrim.desktop.jdbc.connect;

import se.bluebrim.desktop.DesktopApp;

/**
 * Desktop application that let the user make JDBC connections in a user interface and save the
 * JDBC properties in a file.
 * 
 * @author GStack
 *
 */
public class JdbcConnectionMaker extends DesktopApp
{
	public static void main(String[] args)
	{
		new JdbcConnectionMaker().run();
	}

	private void run()
	{
		JdbcConnectionModel model = new JdbcConnectionModel();
		JdbcConnectionFileEditor editor = new JdbcConnectionFileEditor(model, this);
		editor.createWindow();
	}

	@Override
	protected String getApplicationPath()
	{
		return "BlueBrim/JdbcConnectionMaker";
	}	
}
