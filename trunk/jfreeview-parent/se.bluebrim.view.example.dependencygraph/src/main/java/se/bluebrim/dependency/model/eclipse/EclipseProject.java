package se.bluebrim.dependency.model.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format.TextMode;

import se.bluebrim.dependency.model.eclipse.DependentsSorter.CycleException;

/**
 * An instance of this class is created for each project in an Eclipse workspace that should participate in the building
 * process. Eclipse projects that don't participate in the building process are excluded by <code>AntClassPathGen</code>
 * <br>
 * An <code>EclipseProject</code> is initialized with information from the <code>.classPath</code> file in the
 * Eclipse project folder. The the <code>.classPath</code> is a XML-file that is parsed by the JDOM api. <br>
 * When <code>createBuildFile()</code> is called a <code>build_classpath.xml</code> file is created. The
 * <code>build_classpath.xml</code> is included in the build.xml file and is formed as a valid build file although it
 * is never used for running any ant targets. The only target contained is a dummy target required to specify the
 * mandatory default target. <br>
 * The <code>build_classpath.xml</code> contains two main parts:
 * <ul>
 * <li><strong>required-projects </strong> is a path element that lists the path to build.xml files in all projects
 * that a project is dependent on. The list is ordered such as projects only is dependent on previous projects in the
 * list. The required-project list is used in the xxxx-all tasks to call the xxxx task for each build.xml file in the
 * list.</li>
 * <li></li>
 * <strong>classpath </strong> is a path element containing three file sets described below.
 * </ul>
 * 
 * The three filesets together makes up the class path used in the compile target. The filesets are:
 * <ul>
 * <li><strong>merged-jars </strong> contains jar-files that are merged into the jar file that the
 * <code>build.xml</code> creates in its jar-target. In addition to be a part of the class path for compilation the
 * <code>merged-jars</code> file set is also used in the jar-target. An example of a merged jar is prepressbase.jar
 * that is merged into the prepressserver.jar. The merge is done because the PrepressServer project has included
 * PrepressBase in its class path and also marked it as "exported". (See Eclipse project property GUI)</li>
 * <li><strong>external-jars </strong> contains the external jars part of the class path. They are separated because
 * they should not be merged and they are handled separately in the deploy target.</li>
 * <li><strong>other-required-jars </strong> contains other jar-files that is part of the compile class path but don't
 * fit in one of the file sets above.</li>
 * </ul>
 * A good place to start learning processing XML with Java is:
 * http://www.cafeconleche.org/books/xmljava/chapters/index.html
 * 
 * @author Göran Stäck
 *  
 */
public class EclipseProject implements OrderDependent, Comparable<EclipseProject>, JarFilePathProvider
{

	private interface Visitor
	{
		void visit(EclipseProject eclipseProject);
	}

	public static class ExternalJarFile implements Comparable, JarFilePathProvider
	{
		private String jarFilePath;
		private EclipseProject jarOwner; // The project that has the jar file in the external-jars directory
		private boolean exported;

		public ExternalJarFile(String jarFilePath, EclipseProject jarOwner, boolean exported)
		{
			this.jarFilePath = jarFilePath;
			this.jarOwner = jarOwner;
			this.exported = exported;
		}

		public String getJarFilePath()
		{
			if (jarFilePath.startsWith("/"))
				return jarFilePath.substring(1);
			return jarOwner.name + "/" + jarFilePath;
		}
		
		public File getJarFile()
		{
			return new File(getJarFilePath());
		}

		public int compareTo(Object o)
		{
			return jarFilePath.compareTo(((ExternalJarFile)o).jarFilePath);
		}

		public String getName()
		{			
			return getJarFile().getName();
		}

		public boolean isExported()
		{
			return exported;
		}
	}

	private AntClassPathGen projectMap;
	private File projectDir;
	private File classPathFile;
	private String name;

	// Projects checked in the "Projects" tab of the project properties
	private Collection<EclipseProject> requiredProjects;

	// Projects checked in the "Order and export tab" of the project properties
	private Collection<EclipseProject> exportedRequiredProjects;

	// Jars listed in the "Libraries" tab of the project properties
	private Collection<ExternalJarFile> externalJars;

	// Jars checked in the "Order and export tab" of the project properties
	private Collection<ExternalJarFile> exportedExternalJars; 

	public EclipseProject(AntClassPathGen projectMap, String name, File classPathFile)
	{
		this.projectMap = projectMap;
		this.name = name;
		this.projectDir = classPathFile.getParentFile();
		this.classPathFile = classPathFile;
	}
	
	/**
	 * Here is an example of a .classPath file:
	 * 
	 * <xmp> 
	 * <?xml version="1.0" encoding="UTF-8"?> 
	 * <classpath> 
	 * 	 <classpathentry kind="src" path="src"/> 
	 * 	 <classpathentry sourcepath="JRE_SRC" kind="var" path="JRE_LIB"/> 
	 * 	 <classpathentry kind="src" path="/XLib"/> 
	 * 	 <classpathentry exported="true" kind="lib" path="external-jars/activation.jar"/>
	 * 	 <classpathentry exported="true" kind="lib" path="external-jars/mail.jar"/> 
	 * 	 <classpathentry exported="true" kind="lib" path="external-jars/xml.jar"/> 
	 * 	 <classpathentry exported="true" kind="lib" path="external-jars/tar.jar"/> 
	 * 	 <classpathentry kind="lib" path="external-jars/tools.jar"/> 
	 * 	 <classpathentry kind="output" path="classes-eclipse"/> 
	 * </classpath> 
	 * </xmp>
	 */
	public void parseClassPathFile() throws JDOMException, IOException
	{
		initCollections();
		if (!classPathFile.exists())
			throw new FileNotFoundException("Hittar inte filen: " + classPathFile.getPath());
		Document doc = createDocument(classPathFile);
		Element classpath = doc.getRootElement();
		Iterator iterator = classpath.getChildren().iterator();
		while (iterator.hasNext())
		{
			Element element = (Element)iterator.next();
			String kind = element.getAttributeValue("kind");
			String path = element.getAttributeValue("path");
			String exported = element.getAttributeValue("exported");
			exported = (exported == null) ? "" : exported;
			boolean isExported = exported.equals("true");
			if (kind.equals("src") && path.startsWith("/"))
			{
				String projectName = path.substring(1);
				EclipseProject project = projectMap.getProject(projectName);
				if (project != null) // The project will be null in case the project is one of the ignored projects
				{
					requiredProjects.add(project);
					if (isExported)
						exportedRequiredProjects.add(project);
				}
			}
			else if (kind.equals("lib"))
			{
				externalJars.add(new ExternalJarFile(path, this, isExported));
				if (isExported)
					exportedExternalJars.add(new ExternalJarFile(path, this, isExported));
			}
		}
	}
	
	private void initCollections()
	{
		requiredProjects = new ArrayList<EclipseProject>();
		exportedRequiredProjects = new ArrayList<EclipseProject>();
		externalJars = new ArrayList<ExternalJarFile>();
		exportedExternalJars = new ArrayList<ExternalJarFile>(); 
	}


	private Document createDocument(File file) throws JDOMException, IOException
	{
		SAXBuilder builder = new SAXBuilder();
		return builder.build(file);
	}

	public String getName()
	{
		return name;
	}
	
	public void setName(String name) 
	{
		this.name = name;
	}

	/**
	 * Visit each required project inclusive required projects in the required projects and so on.
	 */
	private void visitEachRequiredProject(Visitor visitor)
	{
		visit(requiredProjects, visitor);
	}

	/**
	 * Visit each project that are included in the classpath of this project. The projects they include in classpath and
	 * export are also visited and so on
	 */
	private void visitEachProjectInClassPath(Visitor visitor)
	{
		visit(requiredProjects, visitor, true);
	}

	/**
	 * Visit each project that are included in the classpath of this project and also is exported. The projects they
	 * include and export are also visited and so on
	 */
	private void visitEachProjectToMerge(Visitor visitor)
	{
		visit(exportedRequiredProjects, visitor, true);
	}

	/**
	 * Call the visit method in the visitor once for each project in the visits collection. Depending on the recursive
	 * flag the visits carry on to the exported required projects of the visited projects.
	 * @param a collection of <code>EclipseProject</code>'s where the visits begin
	 * @param visitor The visit method of this object i called once for each project
	 * @param recursive If true the method also visits exported required projects of the visited projects
	 */
	private void visit(Collection visits, Visitor visitor, boolean recursive)
	{
		Iterator iterator = visits.iterator();
		while (iterator.hasNext())
		{
			EclipseProject project = (EclipseProject)iterator.next();
			visitor.visit(project);
			if (recursive)
				visit(project.exportedRequiredProjects, visitor, recursive);
		}
	}

	/**
	 * Call the visit method in the visitor once for each project in the visits collection. The visits the carry on to
	 * the required projects of the visited projects.
	 * @param a collection of <code>EclipseProject</code>'s where the visits begin
	 * @param visitor The visit method of this object i called once for each project
	 */
	private void visit(Collection visits, Visitor visitor)
	{
		Iterator iterator = visits.iterator();
		while (iterator.hasNext())
		{
			EclipseProject project = (EclipseProject)iterator.next();
			visitor.visit(project);
			visit(project.requiredProjects, visitor);
		}
	}

	/**
	 * Return a collection with projects that are included in the classpath of this project and also is exported. The
	 * projects they include and export are also included
	 */
	public Collection getProjectsToMerge()
	{
		final Set projects = new TreeSet();
		Visitor visitor = new Visitor()
		{
			public void visit(EclipseProject eclipseProject)
			{
				projects.add(eclipseProject);
			}
		};
		visitEachProjectToMerge(visitor);
		return projects;
	}

	/**
	 * Collects all external jars that are included in the classpath of this project. That includes the external jars
	 * added to the classpath in this project together with all external jars that are exported from projects that this
	 * project is dependent on.
	 */
	public Collection<ExternalJarFile> getExternalJarsInClassPath()
	{
		final Set<ExternalJarFile> externalJarsInClassPath = new TreeSet<ExternalJarFile>();
		externalJarsInClassPath.addAll(externalJars);
		Visitor visitor = new Visitor()
		{
			public void visit(EclipseProject eclipseProject)
			{
				externalJarsInClassPath.addAll(eclipseProject.exportedExternalJars);
			}
		};
		visitEachProjectInClassPath(visitor);
		return externalJarsInClassPath;
	}

	/**
	 * Return a collection with all projects included in this projects classpath exclusive this projects.
	 */
	public Collection<EclipseProject> getProjectsInClasspath()
	{
		final Set<EclipseProject> projects = new TreeSet<EclipseProject>();
		Visitor visitor = new Visitor()
		{
			public void visit(EclipseProject eclipseProject)
			{
				projects.add(eclipseProject);
			}
		};
		visitEachProjectInClassPath(visitor);
		return projects;
	}
	
	public boolean isExportedRequiredProject(EclipseProject project)
	{
		return exportedRequiredProjects.contains(project);
	}

	/**
	 * Return a collection all required projects and the required projects of the required projects and so in. A required
	 * project is a project that is checked in the Project tab in the class path section of the project properties.
	 */
	public Collection<EclipseProject> getRequiredProjectsRecursivly()
	{
		final Set<EclipseProject> projects = new TreeSet<EclipseProject>();
		Visitor visitor = new Visitor()
		{
			public void visit(EclipseProject eclipseProject)
			{
				projects.add(eclipseProject);
			}
		};
		visitEachRequiredProject(visitor);
		return projects;
	}

	private Collection<EclipseProject> getSortedProjectsInClasspath() throws CycleException
	{
		return DependentsSorter.orderDependents(getProjectsInClasspath().iterator());
	}

	private Collection<EclipseProject> getSortedRequiredProjects() throws CycleException
	{
		return DependentsSorter.orderDependents(getRequiredProjectsRecursivly().iterator());
	}

	//	private void printProjectCollection(Collection projects)
	//	{
	//		Iterator iterator = projects.iterator();
	//		while (iterator.hasNext())
	//		{
	//			System.out.println(((EclipseProject)iterator.next()).getName());
	//		}
	//	}

	/**
	 * Creates a <code>build_classpath.xml</code> file that is included in build.xml file. This arrangement makes the
	 * <code>build.xml</code> -file almost identical between projects. The difference is the project name and that a
	 * few build.xml contains a deploy target.
	 * @throws CycleException
	 */
	public void createAntClassPathFile() throws IOException, CycleException
	{
		Document doc = new Document();
		// header comment
		doc.addContent(new Comment(" This file have been generated by " + AntClassPathGen.class.getName() + "."));
		doc.addContent(new Comment(" **** Do NOT edit **** "));

		// project
		Element root = new Element("project");
		root.setAttribute("name", getName() + "-classpath");
		root.setAttribute("default", "nop");

		// Extra projects
		/*
		 * root.addContent(new Text("")); root.addContent(new Comment(" Extra projects - override in build.xml to add
		 * additional projects required for deployment ")); Element path = new Element("path"); path.setAttribute("id",
		 * "extra-deployment-projects"); root.addContent(path); root.addContent(new Text(""));
		 */

		// Required projects
		root.addContent(new Text(""));
		root.addContent(new Comment(" Required projects "));
		Element path = new Element("path");
		path.setAttribute("id", "required-projects");
		addRequiredProjects(path);
		/*
		 * Element extrapath = new Element("path"); extrapath.setAttribute("refid", "extra-deployment-projects");
		 * path.addContent(extrapath);
		 */
		root.addContent(path);
		root.addContent(new Text(""));

		// Class path
		root.addContent(new Comment(" Classpath "));
		Element classPath = new Element("path");
		classPath.setAttribute("id", "classpath");
		addClassPath(classPath);
		root.addContent(classPath);
		root.addContent(new Text(""));

		// Targets
		root.addContent(new Comment(" Dummy target "));
		Element target = new Element("target");
		target.setAttribute("name", "nop");
		root.addContent(target);
		root.addContent(new Text(""));

		doc.setRootElement(root);

		Format prettyFormat = Format.getPrettyFormat();
		prettyFormat.setTextMode(TextMode.PRESERVE);
		XMLOutputter outputter = new XMLOutputter(prettyFormat);
		File file = new File(projectDir, "build_classpath.xml");
		if (isAntClassPathFileChangedOrMissing(doc, outputter, file))
		{
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fileWriter);
			outputter.output(doc, out);
			out.close();
			System.out.println("Replaced " + file.getParentFile().getName() + "/" + file.getName());
		}
	}

	/**
	 * The class path element contains three filesets
	 */
	private void addClassPath(Element classPath)
	{
		// 1) External jars
		Collection externalJarsInClasspath = getExternalJarsInClassPath();
		classPath.addContent(new Comment(" External jars "));
		createJarFileSet(classPath, externalJarsInClasspath, "external-jars");
		classPath.addContent(new Text(""));

		// 2) Merged jars
// The merge feature is not wanted at here
//		Collection projectsToMerge = getProjectsToMerge();
		Collection projectsToMerge = new ArrayList();
		classPath.addContent(new Comment(" Merged jars "));
		createJarFileSet(classPath, projectsToMerge, "merged-jars");

		// 3) Other required jars
		// otherRequiredJars is the difference between projects in classpath and projects to merge
		Collection otherRequiredJars = new ArrayList(getProjectsInClasspath());
		otherRequiredJars.removeAll(projectsToMerge);
		classPath.addContent(new Comment(" Other required jars "));
		createJarFileSet(classPath, otherRequiredJars, "other-required-jars");
		classPath.addContent(new Text(""));

	}

	/**
	 * 
	 * @return A <code>String</code> to be used in the name attribute of the include element in ant FileSets containing
	 *         jars
	 */
	public String getJarFilePath()
	{
		return name + "/${jars-dir}/" + name + ".jar";
	}

	/**
	 * Creates a file set containing an include element with a jar-file reference for each <code>EclipseProject</code>
	 * or <code>String</code> in the iterator.
	 */
	private void createJarFileSet(Element classPath, Collection jarFilePathProviders, String id)
	{
		if (jarFilePathProviders.isEmpty())
		{
			createEmptyFileSet(classPath, id);
			return;
		}
		Iterator iterator = jarFilePathProviders.iterator();
		Element filesetElement = new Element("fileset");
		filesetElement.setAttribute("dir", "..");
		filesetElement.setAttribute("id", id);
		classPath.addContent(filesetElement);
		while (iterator.hasNext())
		{
			JarFilePathProvider jarFilePathProvider = (JarFilePathProvider)iterator.next();
			Element pathElement = new Element("include");
			pathElement.setAttribute("name", jarFilePathProvider.getJarFilePath());
			filesetElement.addContent(pathElement);
		}
	}

	/**
	 * Creates an empty file set as the example below
	 * 
	 * <fileset dir=".." id="merged-jars"> <include name="" /> </fileset>
	 */
	private void createEmptyFileSet(Element classPath, String id)
	{
		Element filesetElement = new Element("fileset");
		filesetElement.setAttribute("dir", "..");
		filesetElement.setAttribute("id", id);
		classPath.addContent(filesetElement);
		Element pathElement = new Element("include");
		pathElement.setAttribute("name", "");
		filesetElement.addContent(pathElement);
	}

	/**
	 * <pathelement location="../ImageClient/build.xml" />
	 */
	private void addRequiredProjects(Element path) throws CycleException
	{
		Iterator iterator = getSortedRequiredProjects().iterator();
		while (iterator.hasNext())
		{
			String projectName = ((EclipseProject)iterator.next()).getName();
			Element pathElement = new Element("pathelement");
			pathElement.setAttribute("location", "../" + projectName + "/build.xml");
			path.addContent(pathElement);
		}

	}

	public Object getDependencyKey()
	{
		return getName();
	}

	/**
	 * Keys
	 */
	public Object[] getPrerequisites()
	{
		Collection<EclipseProject> referedProjects = getProjectsInClasspath();
		int size = referedProjects.size();
		Object[] prerequisites = new Object[size];
		Iterator<EclipseProject> iter = referedProjects.iterator();
		for (int i = 0; i < size; i++)
		{
			prerequisites[i] = ((EclipseProject)iter.next()).getDependencyKey();
		}
		return prerequisites;
	}

	public Iterator<EclipseProject> getDependentOn()
	{
		return requiredProjects.iterator();
	}

	public int compareTo(EclipseProject anotherEclipseProject)
	{
		return name.compareTo(anotherEclipseProject.name);
	}
	
	@Override
	public boolean equals(Object obj)
	{

	  if (obj == null) { return false; }
	   if (obj == this) { return true; }
	   if (obj.getClass() != getClass()) {
	     return false;
	   }
	   EclipseProject another = (EclipseProject) obj;
	   return new EqualsBuilder()
	                 .append(name, another.name)
	                 .isEquals();
		}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(3, 11).append(name).toHashCode();
	}
	
	@Override
	public String toString()
	{
		return name;
	}

	/**
	 * This method is not intended for regular use but can be useful for creating a build.xml file for each project in a
	 * workspace. The <code>template_build.xml</code> are copied and the two <code>@ProjectName@</code> strings in template file are replaced by the Eclipse project name. <br>
	 *               The method will not overwrite an existing <code>build.xml</code> file.
	 * @throws IOException
	 */
	public void createBuildFile() throws IOException
	{
		File buildXmlFile = new File(projectDir, "build.xml");
		if (buildXmlFile.exists())
			return;
		System.out.println("Creating build.xml for: " + getName());
		injectProjectName(buildXmlFile);
	}

	private void injectProjectName(File buildXmlFile) throws IOException
	{
		InputStream is = new FileInputStream("template_build.xml");
		if (is == null)
			throw new RuntimeException("Unable to find \"template_build.xml\" in class path");		
		BufferedWriter writer = new BufferedWriter(new FileWriter(buildXmlFile));
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = reader.readLine()) != null)
		{
			line = replace(line, "@ProjectName@", name);
			writer.write(line);
			writer.newLine();
		}
		reader.close();
		writer.close();
	}

	/**
	 * Compare the new XML we are about to write with the XML in the present file. If unchanged don't writing the file to
	 * awoid unnecessary dirty marking in CVS working copy.
	 * @throws IOException
	 */
	private boolean isAntClassPathFileChangedOrMissing(Document doc, XMLOutputter outputter, File file)
			throws IOException
	{
		if (!file.exists())
			return true;
		ByteArrayOutputStream newXmlContent = new ByteArrayOutputStream();
		outputter.output(doc, newXmlContent);
		BufferedReader newFile = new BufferedReader(new StringReader(newXmlContent.toString()));
		BufferedReader oldFile = new BufferedReader(new FileReader(file));
		String oldLine = oldFile.readLine();
		String newLine = newFile.readLine();
		int lineNo = 1;
		while (true)
		{
			if (oldLine == null && newLine == null) // We reached end att the same time
				return false;
			else if (oldLine == null || newLine == null) // Different number of lines
				return true;
			else if (!oldLine.equals(newLine)) // Difference in one of the lines
			{
				//							System.out.println("\tDifference in line: " + lineNo);
				return true;
			}
			else
			{
				oldLine = oldFile.readLine();
				newLine = newFile.readLine();
				lineNo++;
			}
		}

	}

	/** Replaces a substring in a string with another string. */
	private static String replace(String s, String from, String to)
	{
		StringBuffer buffer = null;
		int fromlength = 0;
		int i = 0;
		while (true)
		{
			int j = s.indexOf(from, i);
			if (j < 0)
				break;
			if (buffer == null)
			{
				buffer = new StringBuffer();
				fromlength = from.length();
			}
			buffer.append(s.substring(i, j));
			buffer.append(to);
			i = j + fromlength;
		}
		if (i == 0)
			return s;
		buffer.append(s.substring(i));
		return buffer.toString();
	}

	public Collection<EclipseProject> getRequiredProjects()
	{
		return requiredProjects;
	}

	/**
	 * Includes everything specified in the libraries tab of the Eclipse project properties.
	 * Not only jar files also class path folders.
	 */
	public Collection<ExternalJarFile> getExternalJars()
	{
		return externalJars;
	}

}