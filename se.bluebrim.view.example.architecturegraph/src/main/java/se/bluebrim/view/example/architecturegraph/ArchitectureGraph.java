package se.bluebrim.view.example.architecturegraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.swing.UIManager;

import org.springframework.core.io.ClassPathResource;

import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;

/**
 * Creates an example of an architecture graph. The blocks in the graph are created programmatically and the layout of the blocks
 * are handled by Swing layout managers. The position of the blocks and additional graphic elements that are created from resources
 * files can be manually adjusted in the GUI by dragging around the elements. The resulting layout is saved in
 * a properties file. <br>
 * A nice example of a graph would be: http://java.sun.com/products/hotspot/images/javase.gif
 * 
 * @author GStack
 *
 */
public class ArchitectureGraph extends DesktopApp
{

	public static void main(String[] args) throws IOException
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ignore){}
		
		new ArchitectureGraph().run();
	}

	/**
	 * Handles three cases:
	 * <ol>
	 * <li>The "architecture-graph.properties" resource does exist and is a file in the file system</li> 
	 * <li>The "architecture-graph.properties" resource does not exist</li> 
	 * <li>The "architecture-graph.properties" resource does exist and is embedded in a jar file, which is the case
	 * when running as Java Web Start</li> 
	 * </ol>
	 * 
	 */
	private void run() throws IOException
	{
		ClassPathResource classPathResource = new ClassPathResource("architecture-graph.properties", getClass());
		if (classPathResource.exists())
		{
			try {
				File file = classPathResource.getFile();
				new FileEditor.OpenFileCommand(new ArchitectureGraphFileEditor(null, this), file).run();
			} catch (FileNotFoundException e) {
				Properties properties = new Properties();
				properties.load(classPathResource.getInputStream());
				openFromProperties(properties);
			}
			
		} else
		{
			openFromProperties(null);
		}
	}

	private void openFromProperties(Properties properties) 
	{
		ArchitectureGraphModel model = new ArchitectureGraphModel(properties);
		ArchitectureGraphFileEditor editor = new ArchitectureGraphFileEditor(model, this);
		editor.createWindow();
	}

	@Override
	protected String getApplicationPath()
	{
		return "BlueBrim/ArchitectureGraph";
	}

}
