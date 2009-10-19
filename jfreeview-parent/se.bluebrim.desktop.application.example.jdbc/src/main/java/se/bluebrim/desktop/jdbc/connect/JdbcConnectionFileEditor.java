package se.bluebrim.desktop.jdbc.connect;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import se.bluebrim.crud.client.DirtyPredicateProvider;
import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;

/**
 * 
 * @author GStack
 *
 */
public class JdbcConnectionFileEditor extends FileEditor
{
	private JdbcConnectionModel jdbcConnectionModel;
	private JdbcConnectionPanel jdbcConnectionPanel;

	public JdbcConnectionFileEditor(JdbcConnectionModel jdbcConnectionModel, DesktopApp desktopApp)
	{
		super(jdbcConnectionModel, desktopApp);
		this.jdbcConnectionModel = jdbcConnectionModel;
	}
	
	@Override
	protected void createWindowContent(JFrame frame)
	{
		jdbcConnectionPanel = new JdbcConnectionPanel(jdbcConnectionModel);
		frame.getContentPane().add(jdbcConnectionPanel);
	}
	
	@Override
	protected DirtyPredicateProvider getDirtyPredicateProvider()
	{
		if (jdbcConnectionPanel == null)
			throw new RuntimeException("createWindowContent must be called first");
		return jdbcConnectionPanel;
	}

	@Override
	protected String getFileContentName()
	{
		return jdbcConnectionModel.getHostName() == null ? DesktopApp.DESKTOP_APP_BUNDLE.getString("noName") : jdbcConnectionModel.getHostName();
	}
	
	@Override
	protected FileEditor readModel(File file) throws Exception
	{
		JdbcConnectionModel model = readModelFromFile(file);
		return open(model, file);					
	}
	
	private JdbcConnectionModel readModelFromFile(File file) throws FileNotFoundException, IOException
	{
		JdbcConnectionModel model = new JdbcConnectionModel();
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		model.readFrom(properties);
		return model;
	}


	@Override
	protected FileEditor openNew()
	{
		JdbcConnectionModel model = new JdbcConnectionModel();
		return open(model, null);		
	}
	
	private FileEditor open(JdbcConnectionModel model, File file)
	{
		JdbcConnectionFileEditor editor = new JdbcConnectionFileEditor(model, desktopApp);
		editor.boundTo(file);
		editor.createWindow();
		return editor;
	}


	@Override
	protected void writeToStream(FileOutputStream stream)
	{
		Properties properties = new Properties();
		jdbcConnectionModel.write(properties);
		try
		{
			properties.store(stream, "Created by " + System.getProperty("user.name"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getFileExtension()
	{
		return "properties";
	}

	@Override
	protected String getFileFilterDescription()
	{
		return "JDBC Connection properties file";
	}
	
	@Override
	protected Image getIcon()
	{
		return new ImageIcon(getClass().getResource("database.png")).getImage();
	}
	
	@Override
	public JComponent getTumbnailSource()
	{
		return jdbcConnectionPanel;
	}

	@Override
	protected void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);		
		createFileMenu(menuBar);		
	}
	
	private void createFileMenu(JMenuBar menuBar)
	{
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		createMenuItemForNew(fileMenu);
		createMenuItemForOpen(fileMenu);
		createMenuItemForSave(fileMenu);		
		createMenuItemForSaveAs(fileMenu);
		createMenuItemForRecentFiles(this, fileMenu);
		
		fileMenu.addSeparator();		
		createMenuItemForQuitWithoutSaving(fileMenu);
	}

}
