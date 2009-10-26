package se.bluebrim.view.dependencygraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import nu.esox.gui.aspect.EnablePredicateAdapter;

import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.core.io.ClassPathResource;

import se.bluebrim.crud.client.command.DefaultAction;
import se.bluebrim.dependency.model.eclipse.EclipseProject;
import se.bluebrim.dependency.model.eclipse.EclipseProject.ExternalJarFile;
import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;
import se.bluebrim.desktop.graphical.ConnectionView;
import se.bluebrim.desktop.graphical.GraphicalFileEditor;
import se.bluebrim.desktop.graphical.PropertyPersistUtil;
import se.bluebrim.desktop.graphical.PropertyPersistableHTMLView;
import se.bluebrim.desktop.graphical.PropertyPersistableView;
import se.bluebrim.view.ConnectableView;
import se.bluebrim.view.HTMLView;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.PaperView;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.geom.DoubleDimension;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.AbstractLayout;
import se.bluebrim.view.paint.Paintable;

/**
 * 
 * See: http://www.graphviz.org/
 * http://jgrapht.sourceforge.net/
 * 
 * For design inspiration see:
 * http://tuscany.apache.org/tuscany-databinding-guide.html
 * 
 * TODO:
 * <ul>
 * <li>PDF links to descriptions and open source project home pages</li>
 * <li>Embed fonts in the PDF</li>
 * <li>Drag and drop files to open, also Eclipse Project folders</li>
 * <li>CTRL A select all</li>
 * <li>Reduction of crossing edges when creating new graph.
 * See http://www.infosun.fim.uni-passau.de/~chris/down/MIP-0608.pdf <br>
 * http://jgrapht.sourceforge.net/</li>
 * </ul>
 * 
 * 
 * @author GStack
 * 
 */
public class ProjectGraphFileEditor extends GraphicalFileEditor
{
	private static final Font FONT = new Font("SansSerif", Font.BOLD, 12);
	private static final Color STROKE_COLOR = new Color(153, 0, 51);
	private static final Color PROJECT_FILL_COLOR = new Color(255, 255, 204);
	
	private ProjectGraphModel projectGraphModel;
	private List<EclipseProjectView> eclipseProjectViews;
	private JFileChooser eclipseProjectFileChooser;
	private HeadLineView headlineView;

	public ProjectGraphFileEditor(ProjectGraphModel model, DesktopApp desktopApp)
	{
		super(model, desktopApp);
		this.projectGraphModel = model;
	}

	private ProjectGraphModel createModelFromUserInput()
	{
		List<File> rootProjects = new ArrayList<File>();
		FileListPanel fileListPanel = new FileListPanel("Select one ore more root Eclipse project(s)", rootProjects, getEclipseProjectFileChooser() );
		int result = fileListPanel.openInModalDialog(window, "Project Graph Editor");
		if (result == JOptionPane.OK_OPTION)
		{
			List<File> eclipseProjectDirs = fileListPanel.getFiles();
			if (eclipseProjectDirs.size() > 0)
			{
				File workspaceDir = eclipseProjectDirs.get(0).getParentFile();
				return new ProjectGraphModel(toNameList(eclipseProjectDirs), workspaceDir);
			}
		}
		return null;
	}
	
	private JFileChooser getEclipseProjectFileChooser()
	{
		if (eclipseProjectFileChooser == null)
			eclipseProjectFileChooser = createEclipseProjectFileChooser();
		return eclipseProjectFileChooser;
	}

	
	private JFileChooser createEclipseProjectFileChooser()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setCurrentDirectory(getWorkingDirectory());
		fileChooser.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File file)
			{
				if (file.isDirectory())
				{
					String[] list = file.list();
					if (list != null)
						return Arrays.asList(list).contains(".classpath");
				}
				return false;
			}

			@Override
			public String getDescription()
			{
				return "Eclipse Project Folder";
			}});
		return fileChooser;
	}
	
	private List<String> toNameList(List<File> files)
	{
		List<String> names = new ArrayList<String>();
		for (File file : files)
		{
			names.add(file.getName());
		}
		return names;
	}


	@Override
	/**
	 * Show a blank page if there is no model
	 */
	protected void createViews()
	{
		if (projectGraphModel != null)
			try
			{
				createHeadlineView(viewContext);
				createLegendView(viewContext);
				createEclipseProjectViews(paperView, viewContext, new DoubleDimension(java.lang.Double.MIN_VALUE, java.lang.Double.MIN_VALUE));
				createJarFileViews(paperView, viewContext);
				projectGraphModel.setRootView(paperView);
				paperView.layoutTree();
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}
	}



	private void createEclipseProjectViews(PaperView paperView, ViewContext viewContext, DoubleDimension dimension)
	{
		eclipseProjectViews = new ArrayList<EclipseProjectView>();
		for (EclipseProject eclipseProject : projectGraphModel.getProjects())
		{
			EclipseProjectView view = new EclipseProjectView(viewContext, eclipseProject);
			paperView.addChild(view);
			eclipseProjectViews.add(view);
			dimension.width = Math.max(dimension.width, view.getMinWidth());
			dimension.height = Math.max(dimension.height, view.getMinWidth());
		}
		
		for (EclipseProjectView view : eclipseProjectViews)
		{
			view.connectToDependentOn(this);	
		}

		layoutViews(new DoubleDimension(dimension.width, dimension.width * 0.5));
	}
	
	private void layoutViews(DoubleDimension dimension)
	{
		for (EclipseProjectView view : eclipseProjectViews)
		{
			view.setWidth((float) dimension.width);
			view.setHeight((float) dimension.height);
//			System.out.println(view.eclipseProject.getName() + " Dependency Level: " + view.getDependencyLevel());
			view.setLocation(new Point2D.Float(0, (float) (view.getDependencyLevel() * (dimension.height + 20))));
		}
	}

	
	/**
	 *
	 */
	private void createJarFileViews(PaperView paperView, ViewContext viewContext)
	{
		for (EclipseProject eclipseProject : projectGraphModel.getProjects())
		{
			JarLibraryView jarLibraryView = new JarLibraryView(viewContext, eclipseProject);
			paperView.addChild(jarLibraryView);
			
			Map<File, JarSubLibraryView> map = new HashMap<File, JarSubLibraryView>();
			for (ExternalJarFile externalJarFile : eclipseProject.getExternalJars())
			{
				File jarDirectory = externalJarFile.getJarFile().getParentFile();
				map.put(jarDirectory, new JarSubLibraryView(viewContext, jarDirectory.getName()));
			}
	
			for (ExternalJarFile externalJarFile : eclipseProject.getExternalJars())
			{
				if (externalJarFile.getName().toLowerCase().endsWith(".jar"))
				{
					JarFileView jarFileView = new JarFileView(viewContext, externalJarFile);
					map.get(externalJarFile.getJarFile().getParentFile()).addChild(jarFileView);
				}
			}
			
			for (JarSubLibraryView view : map.values())
			{
				if (view.getChildren().size() > 1)			// A label view was added in the constructor
				{
					jarLibraryView.addChild(view);
					new ConnectionView(findViewFor(eclipseProject), jarLibraryView, STROKE_COLOR);
				}
			}			
		}
	}


	private void createHeadlineView(ViewContext viewContext) throws MalformedURLException, Exception
	{
		URL headLineView = new ClassPathResource("html/headline.html").getURL();
		headlineView = new HeadLineView(viewContext, headLineView, headLineView);
		paperView.addChild(headlineView);
	}
	
	private void createLegendView(ViewContext viewContext) throws MalformedURLException, Exception
	{
		URL legendView = new ClassPathResource("html/legend.html").getURL();
		HTMLView headlineView = new PropertyPersistableHTMLView(viewContext, legendView, legendView)
		{
			@Override
			protected String getGeometryPropertyKey()
			{
				return "legend";
			}
			
			@Override
			protected String filterHTML(String html)
			{
				String result = html.replace("@printing-date@", dateTimeFormat.format(new Date()));
				return result;
			}
			
			@Override
			/**
			 * Suppress legend for bitmap image since legend fits bad when the graph
			 * is embedded in a text document.
			 */
			protected void paintLayer(Paintable g)
			{
				if (g.getTarget() == Paintable.Target.BitmapImage || g.getTarget() == Paintable.Target.SVG)
					return;
				else				
					super.paintLayer(g);
			}
			
		};
		paperView.addChild(headlineView);
	}



	private EclipseProjectView findViewFor(EclipseProject eclipseProject)
	{
		for (EclipseProjectView eclipseProjectView : eclipseProjectViews)
		{
			if (eclipseProjectView.eclipseProject.getName().equals(eclipseProject.getName()))
				return eclipseProjectView;
		}
		throw new IllegalArgumentException("No view is containing the specified Eclipse project: " + eclipseProject.getName());
	}
	
	@Override
	protected JFileChooser createFileChooser()
	{
		JFileChooser fileChooser = super.createFileChooser();
		fileChooser.setCurrentDirectory(getWorkingDirectory());
		return fileChooser;
	}
	
	private File getWorkingDirectory()
	{
		return (projectGraphModel != null) ? projectGraphModel.getWorkspaceDir() : new File("../");
	}

	@Override
	protected String getFileContentName()
	{
		return "project-graph";
	}

	@Override
	protected String getFileExtension()
	{
		return "properties";
	}

	@Override
	protected String getFileFilterDescription()
	{
		return "ISAC project graph";
	}

	@Override
	protected void writeToStream(FileOutputStream stream)
	{
		Properties properties = new Properties();
		projectGraphModel.write(properties);
		try
		{
			properties.store(stream, "Created by " + System.getProperty("user.name"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void createFileMenu(JMenuBar menuBar)
	{
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		createMenuItemForNew(fileMenu);
		createMenuItemForOpen(fileMenu);
		createMenuItemForSave(fileMenu);
		createMenuItemForSaveAs(fileMenu);
		createMenuItemForRecentFiles(this, fileMenu);
		fileMenu.addSeparator();
		fileMenu.add(new PageSetupAction());
		fileMenu.add(new PrintAction());
		fileMenu.add(new SaveAsPdfAction());		
		fileMenu.add(new SaveAsSvgAction());
		fileMenu.add(new SaveAsPngAction());
		fileMenu.addSeparator();
		createMenuItemForQuitWithoutSaving(fileMenu);
		createEditMenu(menuBar);
		createActionMenu(menuBar);
	}

	private void createEditMenu(JMenuBar menuBar)
	{
		JMenu editMenu = new JMenu("Edit");
		menuBar.add(editMenu);
		editMenu.add(new EditHeadLineAction());		
	}
	
	private void createActionMenu(JMenuBar menuBar)
	{
		JMenu actionMenu = new JMenu("Action");
		menuBar.add(actionMenu);
		actionMenu.add(new RebuildAction());		
	}


	@Override
	protected Image getIcon()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected FileEditor openNew()
	{
		ProjectGraphModel newModel = createModelFromUserInput();
		if (newModel != null)
			return open(newModel, null);
		else
			return null;
	}

	/**
	 * Use the initial model-less instance of the editor instead of creating a new editor
	 * and window.
	 */
	private FileEditor open(ProjectGraphModel modelToOpen, File file)
	{
		if (projectGraphModel == null)
		{
			setProjectGraphModel(modelToOpen);
			boundTo(file);
			createViews();
			viewPanel.updateCashedViewValues();
			viewPanel.repaint();
			return this;
		} else
		{
			ProjectGraphFileEditor editor = new ProjectGraphFileEditor(modelToOpen, desktopApp);
			editor.createWindow();
			editor.boundTo(file);
			return editor;
		}
	}
	
	private void setProjectGraphModel(ProjectGraphModel projectGraphModel)
	{
		setModel(projectGraphModel);
		this.projectGraphModel = projectGraphModel;
	}

	@Override
	protected FileEditor readModel(File file) throws Exception
	{
		ProjectGraphModel model = readModelFromFile(file);
		FileEditor fileEditor = open(model, file);
		model.applyStoredGeometry();
		viewPanel.updateCashedViewValues();
		return fileEditor;
	}
	
	private ProjectGraphModel readModelFromFile(File file) throws FileNotFoundException, IOException
	{
		ProjectGraphModel model = new ProjectGraphModel();
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		model.readRootProjects(properties);
		return model;
	}

	
	/**
	 * View that illustrates an Eclipse project with an ellipse containing
	 * the project name in the center.
	 */
	private static class EclipseProjectView extends BasicView implements ConnectableView, TransferableView, PropertyPersistableView
	{
		private Shape shape;
		private EclipseProject eclipseProject;
		
		public EclipseProjectView(ViewContext viewContext, EclipseProject eclipseProject)
		{
			super(viewContext);
			this.eclipseProject = eclipseProject;
			eclipseProject.setName(stripOpenCRXname(eclipseProject.getName()));
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent evt)
				{
					calculateShape();
				}
			};
			addPropertyChangeListener(BasicView.HEIGHT, propertyChangeListener);
			addPropertyChangeListener(BasicView.WIDTH, propertyChangeListener);
			addPropertyChangeListener(BasicView.LOCATION, propertyChangeListener);
			calculateShape();
		}
		
		private String stripOpenCRXname(String name)
		{
			if (name.contains("~"))
			{
				String[] parts = name.split("~");
				if (name.startsWith("openCRX"))
					return "openCRX" + parts[1];
				else
					if (name.startsWith("openMDX"))
						return "openMDX" + parts[1];
					else
						return parts[1];					
			} else
				return name;
		}

		
		public int getDependencyLevel()
		{
			MutableInt maxDepth= new MutableInt();
			getDependencyLevel(eclipseProject, new MutableInt(), maxDepth);
			return maxDepth.intValue();
		}
		
		private void getDependencyLevel(EclipseProject project, MutableInt level, MutableInt maxDepth)
		{
			if (project.getRequiredProjects().size() > 0)
			{
				level.increment();
				maxDepth.setValue(Math.max(level.intValue(), maxDepth.intValue()));
				for (EclipseProject requiredProject : project.getRequiredProjects())
				{
					getDependencyLevel(requiredProject, level, maxDepth);
				}
			}
			level.decrement();
		}

		@Override
		public Object getModel()
		{
			return eclipseProject;
		}
		
		@Override
		public Shape getConnectionShape()
		{
			if (shape == null)
				calculateShape();
			return shape;
		}
		
		/**
		 * Used to assign all views the with of the widest.
		 */
		public float getMinWidth()
		{
			return (float) (getStringBounds(FONT, eclipseProject.getName()).getWidth() + (FONT.getSize2D() * 0.8));
		}
		
		protected void calculateShape()
		{
			Rectangle2D bounds = getBounds();
			shape = new Ellipse2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
		}
				
		@Override
		protected void paintLayer(Paintable g)
		{
			g.setStroke(new BasicStroke(getFrameThickness()));
			g.setColor(PROJECT_FILL_COLOR);
			g.fill(shape);
			g.setColor(STROKE_COLOR);
			g.draw(shape);
			g.setColor(Color.BLACK);
			g.setFont(FONT);
			drawCenteredText(g, eclipseProject.getName());
		}
				
		public void connectToDependentOn(ProjectGraphFileEditor editor)
		{
			for (EclipseProject depententOnProject : eclipseProject.getRequiredProjects())
			{
				EclipseProjectView dependentOnProjectView = editor.findViewFor(depententOnProject);
				
				Color connectionColor = eclipseProject.isExportedRequiredProject(depententOnProject) ? STROKE_COLOR : Color.GRAY;
				new ConnectionView(this, dependentOnProjectView, connectionColor);
			}			
		}
		

		@Override
		public void readProperties(Properties properties)
		{
			PropertyPersistUtil.readLocation(properties, this, eclipseProject.getName());			
		}

		@Override
		public void writeProperties(Properties properties)
		{
			PropertyPersistUtil.writeLocation(properties, this, eclipseProject.getName());			
		}
				
	}
	
	/**
	 * Container that groups the jars together that belongs to a particular project.
	 *
	 */
	private static class JarLibraryView extends AbstractParentView implements ConnectableView, Layoutable , TransferableView, PropertyPersistableView
	{				
		private EclipseProject eclipseProject;

		public JarLibraryView(ViewContext viewContext, EclipseProject eclipseProject)
		{
			super(viewContext, new JarLibraryLayout());
			setSizeControlledByChildren(true);
			this.eclipseProject = eclipseProject;
		}

		@Override
		public Object getModel()
		{
			return eclipseProject;
		}
		
		@Override
		protected void drawBeforeChildren(Paintable g)
		{
			Shape shape = getConnectionShape();
			g.setColor(new Color(243, 243, 243));
			g.fill(shape);
			g.setColor(STROKE_COLOR);
			g.setStroke(new BasicStroke(getFrameThickness()));
			g.draw(shape);
		}


		@Override
		public Shape getConnectionShape()
		{
			Rectangle2D bounds = getBounds();
			return new RoundRectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 12.0, 12.0);
		}

		@Override
		public void readProperties(Properties properties)
		{
			PropertyPersistUtil.readLocation(properties, this, getPropertyKey());			
			
		}

		@Override
		public void writeProperties(Properties properties)
		{
			PropertyPersistUtil.writeLocation(properties, this, getPropertyKey());						
		}

		private String getPropertyKey()
		{
			return eclipseProject.getName() + ".lib";
		}

	}
	
	/**
	 * Container that groups the jars together that belongs to same component.
	 *
	 */
	private static class JarSubLibraryView extends AbstractParentView implements Layoutable
	{				
		public JarSubLibraryView(ViewContext viewContext, String componentName)
		{
			super(viewContext, new JarSubLibraryLayout());
			setSizeControlledByChildren(true);
			addChild(new JarSubLibraryLabelView(viewContext, componentName));
		}
		
		@Override
		public void setWidth(float width)
		{
			super.setWidth(width);
			for (Object child : getChildren())
			{
				if (child instanceof Layoutable)
					((Layoutable)child).setWidth(width);
			}
		}

	}

		
	/**
	 * Container that groups the jars together that belongs to same component.
	 *
	 */
	private static class JarSubLibraryLabelView extends IconTextView
	{
		private static BufferedImage icon = loadImage("folder.gif");

		public JarSubLibraryLabelView(ViewContext viewContext, String label)
		{
			super(viewContext, label, icon, JarFileView.JAR_FILE_FONT.deriveFont(Font.BOLD), Color.BLACK);
		}		
	}

	
	/**
	 * Stack the children vertically. Set the width equally to widest.
	 *
	 */
	private static class JarSubLibraryLayout extends AbstractLayout
	{
		private static float gap = 0;

		public void layoutViews(ParentView container)
		{
			float width = Float.MIN_VALUE;
			float height = Float.MIN_VALUE;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				width = Math.max(width, view.getMinWidth());
				height = Math.max(height, view.getMinHeight());
			}
						
			float y = 0;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				view.setWidth(width);
				view.setHeight(height);
				view.setLocation(new Point2D.Float(0, y));
				y = y + view.getHeight() + gap;
			}
		}
		
		@Override
		public Dimension2D getMinimumLayoutSize(ParentView container)
		{
			float width = Float.MIN_VALUE;
			float height = Float.MIN_VALUE;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				width = Math.max(width, view.getMinWidth());
				height = Math.max(height, view.getMinHeight());
			}
						
			float y = 0;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				y = y + height + gap;
			}

			Dimension2D dim = new Dimension();
			dim.setSize(width, y);
			return dim;
		}
	}
	
	/**
	 * Stack the children vertically. Set the width equally to widest.
	 *
	 */
	private static class JarLibraryLayout extends AbstractLayout
	{
		public void layoutViews(ParentView container)
		{
			float width = Float.MIN_VALUE;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				width = Math.max(width, view.getWidth());
			}
						
			float y = 0;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				view.setWidth(width);
				view.setLocation(new Point2D.Float(0, y));
				y = y + view.getHeight();
			}
		}

		@Override
		public Dimension2D getMinimumLayoutSize(ParentView container)
		{
			float width = Float.MIN_VALUE;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				width = Math.max(width, view.getWidth());
			}
						
			float y = 0;
			for (Iterator iter = container.getChildren().iterator(); iter.hasNext();) 
			{
				Layoutable view = (Layoutable) iter.next();
				y = y + view.getHeight();
			}
			Dimension2D dim = new Dimension();
			dim.setSize(width, y);
			return dim;
		}
	}

	private class EditHeadLineAction extends DefaultAction
	{

		public EditHeadLineAction()
		{
			super("Headline...");
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			String input = JOptionPane.showInputDialog(window, "Edit headline", headlineView.text);
			if (input != null)
			{
				headlineView.setText(input);			
				viewPanel.repaint();
				setDirty();
			}
		}		
	}
	
	/**
	 * Run this after changing the class path properties in your Eclipse workspace
	 * to see immediate effect of your changes.
	 */
	private class RebuildAction extends DefaultAction
	{

		public RebuildAction()
		{
			super("Rebuild");
			new EnablePredicateAdapter(null, null, this, null, hasModelPredicate);
		}
		
		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			projectGraphModel.updateProperties();
			paperView.removeAllChildren();
			projectGraphModel.refresh();
			createViews();
			projectGraphModel.applyStoredGeometry();
			viewPanel.updateCashedViewValues();
			viewPanel.repaint();			
		}
		
	}

	private abstract static class IconTextView extends BasicView implements Layoutable
	{
			private String text;
			private BufferedImage icon;
			private Font font;
			private Color textColor;

			public IconTextView(ViewContext viewContext, String text, BufferedImage icon, Font font, Color textColor)
			{
				super(viewContext);
				this.text = text;
				this.icon = icon;
				this.font = font;
				this.textColor = textColor;
			}

			protected static BufferedImage loadImage(String name)
			{
				try
				{
					return ImageIO.read(new ClassPathResource(name, JarFileView.class).getInputStream());
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}

			
			/**
			 * Used to assign all views the with of the widest.
			 */
			public float getMinWidth()
			{
				return (float) (getStringBounds(font, text).getWidth() + (getInset() * 2) + icon.getWidth() + getIconTextGap());
			}

			private float getInset()
			{
				return (float) (font.getSize2D() * 0.4);
			}
			
			/**
			 * Used to assign all views the height of the highest.
			 */
			public float getMinHeight()
			{
				return (float) (getStringBounds(font, text).getHeight() + (getInset() * 2));
			}
			
			@Override
			protected Font getFont()
			{
				return font;
			}
			
			@Override
			protected void paintLayer(Paintable g)
			{
				g.setColor(textColor);
				g.setFont(font);			
				AffineTransform at = new AffineTransform();
				at.translate(getX() + getInset(), getY() + (getHeight() - icon.getHeight())/2);
				g.drawRenderedImage(icon, at);
				drawLeftJustifiedText(g, text, getInset() + icon.getWidth() + getIconTextGap(), 0f);				
			}

			private float getIconTextGap()
			{
				return icon.getWidth() * 0.2f;
			}
			
	}

	/**
	 * View that illustrates a jar file with a rounded rectangle containing
	 * the jar name in the center.
	 */
	private static class JarFileView extends IconTextView
	{
		protected static final Font JAR_FILE_FONT = FONT.deriveFont(Font.PLAIN, 9f);
		private static BufferedImage privateIcon = loadImage("private.gif");
		private static BufferedImage publicIcon = loadImage("public.gif");
		
		public JarFileView(ViewContext viewContext, EclipseProject.ExternalJarFile jarFile)
		{
			super(viewContext, jarFile.getName(), jarFile.isExported() ? publicIcon : privateIcon, JAR_FILE_FONT, Color.BLACK);
		}				
	}

	@Override
	protected String getPrinterJobName()
	{
		return getFileFilterDescription();
	}
	
	private static class HeadLineView extends PropertyPersistableHTMLView
	{
		public HeadLineView(ViewContext viewContext, URL documentBase, URL url) throws Exception
		{
			super(viewContext, documentBase, url);
		}

		private String text = "Edit this text by selecting Edit -> Headline";
		
		@Override
		protected String getGeometryPropertyKey()
		{
			return "headline";
		}
		
		@Override
		protected String filterHTML(String html)
		{
			String result = html.replace("@text@", text);
			return result;
		}
		
		@Override
		public void writeProperties(Properties properties)
		{
			super.writeProperties(properties);
			properties.put("headline-text", text);
		}
		
		@Override
		public void readProperties(Properties properties)
		{
			super.readProperties(properties);
			text = properties.getProperty("headline-text", text);
		}

		public void setText(String text)
		{
			this.text = text;
			invalidateHtml();
		}

		@Override
		/**
		 * Suppress legend for bitmap image since legend fits bad when the graph
		 * is embedded in a text document.
		 */
		protected void paintLayer(Paintable g)
		{
			if (g.getTarget() == Paintable.Target.BitmapImage || g.getTarget() == Paintable.Target.SVG)
				return;
			else				
				super.paintLayer(g);
		}

	
	}

}
