package se.bluebrim.view.example.architecturegraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import se.bluebrim.crud.esox.DirtyPredicateModel;
import se.bluebrim.desktop.graphical.PropertyPersistableView;
import se.bluebrim.view.impl.AbstractParentView;

/**
 * Represents the aspects of a Architecture Graph that are stored in or
 * read from a file. Since the views geometry is stored they are also
 * part of the model.
 * 
 * @author GStack
 *
 */
public class ArchitectureGraphModel extends DirtyPredicateModel
{
	private AbstractParentView rootView;
	private Properties properties;
	
	public ArchitectureGraphModel()
	{
	}
	
	public ArchitectureGraphModel(Properties properties)
	{
		this.properties = properties;
	}
	
	public void read(File file)
	{
		this.properties = new Properties();
		try
		{
			properties.load(new FileInputStream(file));
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

		
	public void setRootView(AbstractParentView rootView)
	{
		this.rootView = rootView;
		if (properties != null)
		{
			List views = rootView.getAllDescendents();	
			for (Object view : views)
			{
				if (view instanceof PropertyPersistableView)
				{
					((PropertyPersistableView)view).readProperties(properties);
				}
			}			
		}
	}

	

	public AbstractParentView getRootView()
	{
		return rootView;
	}

	
	public void write(Properties properties)
	{
		List views = rootView.getAllDescendents();	
		for (Object view : views)
		{
			if (view instanceof PropertyPersistableView)
			{
				((PropertyPersistableView)view).writeProperties(properties);
			}
		}
	}
	


}

