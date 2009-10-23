package se.bluebrim.view.example.architecturegraph;

import java.io.IOException;

import javax.swing.UIManager;

import org.springframework.core.io.ClassPathResource;

import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;

/**
 * Creates and visualizes a graph over the projects that makes up a system. The jar files that the project
 * are dependent on is also displayed. There is no automatic algorithm that creates an optimal layout instead
 * the layout is created manually in a GUI by dragging around the symbols. The resulting layout is saved in
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
