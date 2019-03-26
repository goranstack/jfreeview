package se.bluebrim.dependency.model.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import se.bluebrim.dependency.model.eclipse.DependentsSorter.CycleException;

/**
 * Used to generate a <code>build_classpath.xml</code> file for each Eclipse project that participate in the Ant build
 * process. You can exclude Eclipse projects by listing them in <code>projects-ignore.config</code> file. The
 * generation is done by creating a <code>EclipseProject</code> instance for each Eclipse project. These instances
 * parse the <code>.classPath</code> file in the Eclipse project folder. That information is then used to create the
 * <code>build_classpath.xml</code> file by using the JDOM api. Previous <code>build_classpath.xml</code> file is
 * overwritten without any questions. <br>
 * 
 * Have look at: http://www.geocities.com/richard_hoefter/eclipse2ant/
 * 
 * @author G�ran St�ck
 *  
 */
public class AntClassPathGen
{
	private static final String WORK_SPACE_DIR = "../";
	private static final String PROJECTS_IGNORE_FILENAME = "/projects-ignore.config";

	public File workSpaceDir;
	private Map<String, EclipseProject> projects = new HashMap<String, EclipseProject>();
	private List<String> ignoredProjects = null;
	public EclipseProject currentEclipseProject;

	public static void main(String[] args)
	{
		AntClassPathGen instance = new AntClassPathGen(new File(WORK_SPACE_DIR));
		instance.run();
	}
	
	public AntClassPathGen()
	{
		this(new File(WORK_SPACE_DIR));
	}


	public AntClassPathGen(File workSpaceDir)
	{
		this.workSpaceDir = workSpaceDir;
	}
	
	protected void log(String s)
	{
		System.out.println(s);
	}

	private void run()
	{
		try
		{
			collectProjects();
			parseClassPathFiles();
			createAntClassPathFiles();
			createBuildXml();
			// The following lines are for testing and are not intended for regular use
			//		printOrderedDependents();
			log("Done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (currentEclipseProject != null)
				System.err.println("Current project: " + currentEclipseProject);
		}
	}

	public void createAntClassPathFiles() throws IOException, CycleException
	{
		Iterator<EclipseProject> iterator = projects.values().iterator();
		while (iterator.hasNext())
		{
			this.currentEclipseProject = iterator.next();
			this.currentEclipseProject.createAntClassPathFile();
			this.currentEclipseProject = null;
		}
	}

	public void parseClassPathFiles() throws JDOMException, IOException
	{
		Iterator<EclipseProject> iterator = projects.values().iterator();
		while (iterator.hasNext())
		{
			this.currentEclipseProject = iterator.next();
			this.currentEclipseProject.parseClassPathFile();
			this.currentEclipseProject = null;
		}
	}

	private void printOrderedDependents() throws CycleException
	{
		Collection<EclipseProject> referedProjects = getProject("SingleUserDesktopApp").getProjectsInClasspath();
		Collection projects = DependentsSorter.orderDependents(referedProjects.iterator());
		printProjectCollection(referedProjects);
		log("------------------------------");
		printProjectCollection(projects);
	}

	private void printProjectCollection(Collection<EclipseProject> projects)
	{
		Iterator<EclipseProject> iterator = projects.iterator();
		while (iterator.hasNext())
			log((iterator.next()).getName());
	}

	public void collectProjects()
	{
		File[] projectFolders = workSpaceDir.listFiles();
		for (int i = 0; i < projectFolders.length; i++)
		{
			String projectName = projectFolders[i].getName();
			if (isProjectIgnored(projectName))
				continue;
			File classPathFile = new File(projectFolders[i], ".classpath");
			File mavenPomFile = new File(projectFolders[i], "pom.xml");
			if (classPathFile.exists())
			{
				EclipseProject eclipseProject = new EclipseProject(this, projectName, classPathFile, mavenPomFile);
				projects.put(eclipseProject.getName(), eclipseProject);
			}
		}
	}

	/** Is specified project in ignore list? */
	protected boolean isProjectIgnored(String projectName)
	{
		// init?
		if (this.ignoredProjects == null)
		{
			this.ignoredProjects = new ArrayList<String>();
			BufferedReader in = null;
			try
			{
				in = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(PROJECTS_IGNORE_FILENAME)));
				String line = null;
				while ((line = in.readLine()) != null)
				{
					if (!(line.startsWith("#") || (line.trim().length() == 0)))
						this.ignoredProjects.add(line);
				}
				in.close();
			}
			catch (IOException e)
			{
				System.err.println("WARNING: Can not read ignore-file: " + PROJECTS_IGNORE_FILENAME);
//				e.printStackTrace();
				try
				{
					in.close();
				}
				catch (Throwable ignored)
				{
				}
			}
		}

		// exists project in ignore list?
		for (Iterator<String> iter = this.ignoredProjects.iterator(); iter.hasNext();)
		{
			String ignoredProject = iter.next();
			if (projectName.equalsIgnoreCase(ignoredProject))
				return true;
		}
		return false;
	}

	public EclipseProject getProject(String name)
	{
		return (EclipseProject)projects.get(name);
	}

	/**
	 * This method is not intended for regular use but can be useful for creating a build.xml file for each project in a
	 * workspace. The <code>resources/template_build.xml</code> are copied to each project and the two
	 * <code>@ProjectName@</code> strings in template file are replaced by the Eclipse project name.
	 * @throws IOException
	 */
	public void createBuildXml() throws IOException
	{
		Iterator<EclipseProject> iterator = projects.values().iterator();
		while (iterator.hasNext())
		{
			this.currentEclipseProject = iterator.next();
			this.currentEclipseProject.createBuildFile();
			this.currentEclipseProject = null;
		}
	}

	public Map<String, EclipseProject> getProjects()
	{
		return projects;
	}

}
