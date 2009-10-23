package se.bluebrim.view.dependencygraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.jdom.JDOMException;

import se.bluebrim.crud.esox.DirtyPredicateModel;
import se.bluebrim.dependency.model.eclipse.AntClassPathGen;
import se.bluebrim.dependency.model.eclipse.EclipseProject;
import se.bluebrim.desktop.graphical.PropertyPersistableView;
import se.bluebrim.view.impl.AbstractParentView;

/**
 * Represents the aspects of a Project Graph that are stored in or
 * read from a file. Since the views geometry is stored they are also
 * part of the model.
 * 
 * @author GStack
 *
 */
public class ProjectGraphModel extends DirtyPredicateModel
{
	private static final String FOREIGN_WORKSPACE = "foreign_workspace";
	private static final String ROOT_PROJECTS = "root-projects";
	private List<String> rootProjectNames;
	private File workspaceDir;
	private Collection<EclipseProject> projects;
	private AbstractParentView rootView;
	private Properties properties;
	
	public ProjectGraphModel()
	{
		rootProjectNames = new ArrayList<String>();
		projects = new HashSet<EclipseProject>();
		workspaceDir = new File("../");
	}
	
	public ProjectGraphModel(String rootProjectName)
	{
		this(createList(rootProjectName), new File("../"));
	}
		
	public ProjectGraphModel(List<String> rootProjectNames, File workspaceDir)
	{
		this();
		this.rootProjectNames = rootProjectNames;
		this.workspaceDir = workspaceDir;
		createProjectList();		
	}
	
	public void refresh()
	{
		projects = new HashSet<EclipseProject>();
		createProjectList();
	}
	
	private void createProjectList()
	{
		AntClassPathGen antClassPathGen = new AntClassPathGen(workspaceDir)
		{
			@Override
			protected boolean isProjectIgnored(String projectName)
			{
				return false;
			}
		};
		antClassPathGen.collectProjects();
		parseClassPathFiles(antClassPathGen.getProjects().values());
		
		for (String rootProjectName : rootProjectNames)
		{
			EclipseProject rootProject = antClassPathGen.getProject(rootProjectName);
			projects.addAll(rootProject.getRequiredProjectsRecursivly());
			projects.add(rootProject);			
		}
	}
	
	public void setRootView(AbstractParentView rootView)
	{
		this.rootView = rootView;
	}

	/**
	 * Apply the geometry that is stored in the properties on the views
	 */
	public void applyStoredGeometry()
	{
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

	
	private static List<String> createList(String element)
	{
		List<String> list = new ArrayList<String>();
		list.add(element);
		return list;
	}

	
	private void parseClassPathFiles(Collection<EclipseProject> workspaceProjects)
	{
		for (EclipseProject project : workspaceProjects)
		{
			try
			{
				project.parseClassPathFile();
			} catch (JDOMException e)
			{
				throw new RuntimeException(e);
			} catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}		
	}

	public Collection<EclipseProject> getProjects()
	{
		return projects;
	}

	public AbstractParentView getRootView()
	{
		return rootView;
	}

	public List<String> getRootProjectNames()
	{
		return rootProjectNames;
	}
	
	public void updateProperties()
	{
		write(new Properties());
	}
	
	public void write(Properties properties)
	{
		this.properties = properties;
		try
		{
			if (!workspaceDir.getCanonicalFile().equals(new File("../").getCanonicalFile()))
				properties.put(FOREIGN_WORKSPACE, workspaceDir.getAbsolutePath());
		} catch (IOException e)
		{
		}
		properties.put(ROOT_PROJECTS, getCommaSeparatedRootProjects());
		List views = rootView.getAllDescendents();	
		for (Object view : views)
		{
			if (view instanceof PropertyPersistableView)
			{
				((PropertyPersistableView)view).writeProperties(properties);
			}
		}
	}
	
	private String getCommaSeparatedRootProjects()
	{
		StringBuffer buf = new StringBuffer();
		for (String name : rootProjectNames)
		{
			if (buf.length() > 0)
				buf.append(",");
			buf.append(name);
		}
		return buf.toString();
	}

	public void readRootProjects(Properties properties)
	{
		String foreignWorkspace = ((String)properties.get(FOREIGN_WORKSPACE));
		if (foreignWorkspace != null)			
			workspaceDir = new File(foreignWorkspace);
		if (!workspaceDir.exists())
			throw new RuntimeException("The specified Eclipse workspace directory does not exist: " + workspaceDir.getAbsolutePath());
		String[] rootProjects = ((String)properties.get(ROOT_PROJECTS)).split(",");
		for (int i = 0; i < rootProjects.length; i++)
		{
			rootProjectNames.add(rootProjects[i].trim());
		}
		this.properties = properties;
		createProjectList();
	}

	public File getWorkspaceDir()
	{
		return workspaceDir;
	}


}

