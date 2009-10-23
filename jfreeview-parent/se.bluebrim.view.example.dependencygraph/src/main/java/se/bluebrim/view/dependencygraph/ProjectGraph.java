package se.bluebrim.view.dependencygraph;

import javax.swing.UIManager;

import se.bluebrim.desktop.DesktopApp;

/**
 * Creates and visualizes a graph over the projects that makes up a system. The jar files that the project
 * are dependent on is also displayed. There is no automatic algorithm that creates an optimal layout instead
 * the layout is created manually in a GUI by dragging around the symbols. The resulting layout is saved in
 * a properties file.
 * 
 * @author GStack
 *
 */
public class ProjectGraph extends DesktopApp
{

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception ignore){}
		
		new ProjectGraph().run();
	}

	private void run()
	{
		ProjectGraphFileEditor editor = new ProjectGraphFileEditor(null, this);
		editor.createWindow();		
	}

	@Override
	protected String getApplicationPath()
	{
		return "BlueBrim/ProjectGraph";
	}


}
