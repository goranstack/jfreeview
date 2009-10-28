package se.bluebrim.view.example.architecturegraph;

import java.io.IOException;

import javax.swing.UIManager;

import org.springframework.core.io.ClassPathResource;

import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;

/**
 * Creates an example of an architecture graph The blocks in the graph is created programmatically and the layout of the blocks
 * are handled by Swing layout managers. The position of the blocks and additional graphic elements that are created from resources
 * files can be manually adjusted in the GUI by dragging around the elements. The resulting layout is saved in
 * a properties file.
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

	private void run() throws IOException
	{
		ArchitectureGraphModel model = new ArchitectureGraphModel();
		ClassPathResource classPathResource = new ClassPathResource("architecture-graph.properties", getClass());
		if (classPathResource.exists())
		{
			new FileEditor.OpenFileCommand(new ArchitectureGraphFileEditor(null, this), classPathResource.getFile()).run();
		} else
		{
			ArchitectureGraphFileEditor editor = new ArchitectureGraphFileEditor(model, this);
			editor.createWindow();
		}
	}

	@Override
	protected String getApplicationPath()
	{
		return "BlueBrim/ArchitectureGraph";
	}

}
