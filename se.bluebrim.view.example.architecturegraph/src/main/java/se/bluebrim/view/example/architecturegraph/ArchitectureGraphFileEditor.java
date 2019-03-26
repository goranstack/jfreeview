package se.bluebrim.view.example.architecturegraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import nu.esox.gui.layout.RowLayout;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.core.io.ClassPathResource;

import se.bluebrim.crud.client.command.DefaultAction;
import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;
import se.bluebrim.desktop.graphical.BufferedImageView;
import se.bluebrim.desktop.graphical.ConnectionView;
import se.bluebrim.desktop.graphical.DefaultPropertyPersistableView;
import se.bluebrim.desktop.graphical.GraphicalFileEditor;
import se.bluebrim.desktop.graphical.PropertyPersistUtil;
import se.bluebrim.desktop.graphical.PropertyPersistableHTMLView;
import se.bluebrim.desktop.graphical.PropertyPersistableView;
import se.bluebrim.desktop.graphical.SvgView;
import se.bluebrim.desktop.graphical.TextView;
import se.bluebrim.view.ConnectableView;
import se.bluebrim.view.HTMLView;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.PopupMenuProvider;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.AbstractView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.impl.ViewVisitor;
import se.bluebrim.view.layout.HorizontalDistributeLayout;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.layout.LayoutAdapter;
import se.bluebrim.view.layout.VerticalDistributeLayout;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.tool.DoubleClickGesture;

/**
 * TODO:
 * <ul>
 * <li>Illustrate Java Service Wrapper</li>
 * <li>How do we upload files from client to server</li>
 * <li>Fix bug: No move cursor on Server block</li>
 * <li>Call out's with link to text</li>
 * <li>Action Lock Colors should be a checked menu item instead of two operations</li>
 * <li>Save as PNG</li>
 * <li>Save as SVG</li>
 * </ul>
 * 
 * @author GStack
 * 
 */
public class ArchitectureGraphFileEditor extends GraphicalFileEditor
{
	private static final Font FONT = new Font("SansSerif", Font.BOLD, 12);
	private static final Color HEADLINE_COLOR = new Color(52, 103, 169);
	private static final Random RANDOM = new Random(System.currentTimeMillis());

	private HeadLineView headlineView;
	private ArchitectureGraphModel architectureGraphModel;
	private Map<String, TextBlockView> synchronizedTextViewMap = new HashMap<String, TextBlockView>();
	private Map<String, ConnectableView> connectableViewMap = new HashMap<String, ConnectableView>();

	public ArchitectureGraphFileEditor(ArchitectureGraphModel model, DesktopApp desktopApp)
	{
		super(model, desktopApp);
		architectureGraphModel = model;
	}

	@Override
	protected void createViews()
	{
		try
		{
			createTextViews();
			createSvgView("database.svg", "database-symbol");
			createSvgView("database.svg", "filesystem-symbol");
			createBufferedImageView("C31.png", "imaster-image");
			createHeadlineView();
			createLegendView();
			createArchitectureViews();
			createConnections();
			architectureGraphModel.setRootView(paperView);
			paperView.layoutTree();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private void createJavaServiceWrapperBlock(BlockView clientBlock)
	{
		BlockView javaServiceWrapperBlock = new SlaveBlockView(viewContext, new VerticalDistributeLayout(), "java-service-wrapper", clientBlock);
		paperView.addChild(javaServiceWrapperBlock);
		connectableViewMap.put("java-service-wrapper-block", javaServiceWrapperBlock);

		javaServiceWrapperBlock.addChild(new TextBlockView(viewContext, "Java Service Wrapper", FONT));
		javaServiceWrapperBlock.addChild(new TextBlockView(viewContext, "Windows Service", FONT));
	}

	private void createTextViews()
	{
		createTextView(viewContext, "text1", "Database");
		createTextView(viewContext, "text2", "Microsoft SQL Server");
		createTextView(viewContext, "text3", "Concentrator");
		createTextView(viewContext, "text4", "IMaster");
		createTextView(viewContext, "text5", "TriDelta proprietary binary protocol over TCP/IP over GPRS");
		createTextView(viewContext, "text6", "File system");
		createTextView(viewContext, "text7", "<html><ul>" + 
				"<li>Import files</li>" +
				"<li>Export files</li>" +
				"<li>Property files</li>" +
				"<li>Log files</li>" +
				"</ul></html>");
		createTextView(viewContext, "text8", "RMI");
		createTextView(viewContext, "text9", "<html>Serialized Business Objects<br>Remote server calls</html>");
		createTextView(viewContext, "text10", "HTTP");
		createTextView(viewContext, "text11", "<html>Download:<ul>" + "<li>About box</li>"
				+ "<li>Release notes</li>" + "<li>Export files</li>" + "<li>Help files</li>" + "</ul></html>");
		createTextView(viewContext, "text12", "Server", FONT.deriveFont(24f));
		createTextView(viewContext, "text13", "Client", FONT.deriveFont(24f));		
		createTextView(viewContext, "text14", "Launches");
		createTextView(viewContext, "text15", "Launches");
		createTextView(viewContext, "text16", "HTML page");
		createTextView(viewContext, "text17", "jar files");
		createTextView(viewContext, "text18", "Launches");
		createTextView(viewContext, "text19", "<html><ul>" + 
				"<li>Property file</li>" +
				"<li>Log file</li>" +
				"</ul></html>");
		createTextView(viewContext, "text20", "SOAP over HTTP");

	}

	private void createConnections()
	{
		createConnection("NanoHTTPD", "java.net.URL");
		createConnection("NanoHTTPD", "Web Browser");
		createConnection("NanoHTTPD", "Java Web Start");
		createConnection("Web Browser", "Java Web Start", ConnectionView.ArrowHeadOption.END);
		createConnection("Java Web Start", "client-block", ConnectionView.ArrowHeadOption.END);
		createConnection("java-service-wrapper-block", "server-block", ConnectionView.ArrowHeadOption.END);
		createConnection("JTDS driver", "database-symbol");
		createConnection("imaster-image", "Java Socket API");
		createConnection("Spring Remote", "RMI");
		createConnection("server-block", "filesystem-symbol");
		createConnection("java-service-wrapper-block", "filesystem-symbol");
		createConnection("Apache Axis2", "Aptus Web Services");
	}

	private void createConnection(String from, String to)
	{
		createConnection(from, to, ConnectionView.ArrowHeadOption.BOTH);
	}
	
	private void createConnection(String from, String to, ConnectionView.ArrowHeadOption arrowHeadOption)
	{
		new ConnectionView(connectableViewMap.get(from), connectableViewMap.get(to), HEADLINE_COLOR, arrowHeadOption);
	}


	/**
	 * Text views with the same text should have their fill color synchronized
	 */
	private TextBlockView synchronizeFillColor(TextBlockView textView)
	{
		if (synchronizedTextViewMap.containsKey(textView.text))
			new FillColorSynchronizer(synchronizedTextViewMap.get(textView.text), textView);
		synchronizedTextViewMap.put(textView.text, textView);
		return textView;
	}

	private void createArchitectureViews()
	{
		createServerBlock();
		BlockView clientBlock = createClientBlock();
		createSolitaryTextBlock(clientBlock, "web-browser", "Web Browser");
		createSolitaryTextBlock(clientBlock, "java-web-start", "Java Web Start");
		createSolitaryTextBlock(clientBlock, "aptus-web-services", "Aptus Web Services");

		createJavaServiceWrapperBlock(clientBlock);
	}

	private void createSolitaryTextBlock(BlockView clientBlock, String propertyKey, String text)
	{
		BlockView container = new SlaveBlockView(viewContext, new LayoutAdapter(new BorderLayout()), propertyKey, clientBlock);
		TextBlockView textBlockView = new TextBlockView(viewContext, text, FONT);
		container.addChild(textBlockView, BorderLayout.CENTER);
		container.setWidth(textBlockView.getMinWidth());
		container.setHeight(textBlockView.getMinHeight());
		paperView.addChild(container);
	}

	private void createServerBlock()
	{
		BlockView serverBlock = new BlockView(viewContext, new LayoutAdapter(new BorderLayout()), "block.server");
		connectableViewMap.put("server-block", serverBlock);

		// South view
		TextBlockView southView = new TextBlockView(viewContext, "J2SE 1.6", FONT.deriveFont(18f));

		serverBlock.addChild(southView, BorderLayout.SOUTH);

		// West view
		StructureView westView = new StructureView(viewContext, new VerticalDistributeLayout());
		serverBlock.addChild(westView, BorderLayout.WEST);
		StructureView dataAccessStack = new StructureView(viewContext, new LayoutAdapter(new RowLayout(true, true)));
		dataAccessStack.addChild(new TextBlockView(viewContext, "JTDS driver", FONT, true, 0.6f));
		dataAccessStack.addChild(new TextBlockView(viewContext, "Apache Connection Pooling", FONT, true, 0.6f));
		dataAccessStack.addChild(new TextBlockView(viewContext, "Spring JDBC", FONT, true, 0.6f));
		dataAccessStack.addChild(new TextBlockView(viewContext, "Isac DAO (Managers)", FONT, true, 0.6f));
		westView.addChild(dataAccessStack);

		StructureView communicationStack = new StructureView(viewContext, new HorizontalDistributeLayout());
		communicationStack.addChild(new TextBlockView(viewContext, "Java Socket API", FONT, true));
		communicationStack.addChild(new TextBlockView(viewContext, "TriDelta protocol", FONT, true));
		westView.addChild(communicationStack);

		// Center view
		StructureView centerView = new StructureView(viewContext, new VerticalDistributeLayout());
		serverBlock.addChild(centerView, BorderLayout.CENTER);
		centerView.addChild(new TextBlockView(viewContext, "Crud Server", FONT));
		centerView.addChild(new TextBlockView(viewContext, "Crud", FONT));
		StructureView twoColumnRow = new StructureView(viewContext, new HorizontalDistributeLayout());
		twoColumnRow.addChild(new TextBlockView(viewContext, "Quarts", FONT));
		twoColumnRow.addChild(new TextBlockView(viewContext, "JFreeChart", FONT));
		centerView.addChild(twoColumnRow);
		centerView.addChild(new TextBlockView(viewContext, "Spring AOP", FONT));
		centerView.addChild(new TextBlockView(viewContext, "Log4J", FONT));
		centerView.addChild(new TextBlockView(viewContext, "Apache Commons", FONT));

		// East view
		StructureView eastView = new StructureView(viewContext, new VerticalDistributeLayout());
		serverBlock.addChild(eastView, BorderLayout.EAST);
		eastView.addChild(new TextBlockView(viewContext, "Apache Axis2", FONT, true));
		eastView.addChild(new TextBlockView(viewContext, "Spring Remote", FONT, true));
		eastView.addChild(new TextBlockView(viewContext, "NanoHTTPD", FONT, true));

		paperView.addChild(serverBlock);
	}

	private BlockView createClientBlock()
	{
		BlockView clientBlock = new BlockView(viewContext, new LayoutAdapter(new BorderLayout()), "block.client");
		connectableViewMap.put("client-block", clientBlock);

		// South view
		TextBlockView southView = new TextBlockView(viewContext, "J2SE 1.6", FONT.deriveFont(18f));
		clientBlock.addChild(southView, BorderLayout.SOUTH);

		// West view
		StructureView westView = new StructureView(viewContext, new VerticalDistributeLayout());
		clientBlock.addChild(westView, BorderLayout.WEST);
		westView.addChild(new TextBlockView(viewContext, "RMI", FONT, true));
		westView.addChild(new TextBlockView(viewContext, "java.net.URL", FONT, true));

		// Center view
		StructureView centerView = new StructureView(viewContext, new VerticalDistributeLayout());
		clientBlock.addChild(centerView, BorderLayout.CENTER);
		centerView.addChild(new TextBlockView(viewContext, "Crud Client", FONT));
		centerView.addChild(new TextBlockView(viewContext, "Crud", FONT));
		StructureView multiColumnRow = new StructureView(viewContext, new LayoutAdapter(new RowLayout(true, true)));
		multiColumnRow.addChild(new TextBlockView(viewContext, "Esox", FONT), RowLayout.FILL);
		StructureView multiRowColumn = new StructureView(viewContext, new VerticalDistributeLayout());
		multiRowColumn.addChild(new TextBlockView(viewContext, "EventBus", FONT));
		multiRowColumn.addChild(new TextBlockView(viewContext, "Swing Action Manager (SAM)", FONT));
		multiColumnRow.addChild(multiRowColumn);
		centerView.addChild(multiColumnRow);

		StructureView multiColumnRow2 = new StructureView(viewContext, new HorizontalDistributeLayout());
		multiColumnRow2.addChild(new TextBlockView(viewContext, "SwingX", FONT));
		multiColumnRow2.addChild(new TextBlockView(viewContext, "JFreeChart", FONT));
		centerView.addChild(multiColumnRow2);

		centerView.addChild(new TextBlockView(viewContext, "Swing", FONT));
		paperView.addChild(clientBlock);
		return clientBlock;
	}

	private Layoutable createTextView(ViewContext viewContext, String propertyKey, String text)
	{
		return createTextView(viewContext, propertyKey, text, FONT);
	}
	
	private Layoutable createTextView(ViewContext viewContext, String propertyKey, String text, Font font)
	{
		DefaultPropertyPersistableView view = new TextView(viewContext, propertyKey, text, font, HEADLINE_COLOR);
		paperView.addChild(view);
		return view;
	}


	private SvgView createSvgView(String svgFilePath, String propertyKey) throws MalformedURLException, Exception
	{
		URL url = new ClassPathResource(svgFilePath, getClass()).getURL();
		SvgView view = new SvgView(viewContext, url, propertyKey);
		connectableViewMap.put(propertyKey, view);
		paperView.addChild(view);
		return view;
	}

	private BufferedImageView createBufferedImageView(String svgFilePath, String propertyKey)
			throws MalformedURLException, Exception
	{
		URL url = new ClassPathResource(svgFilePath, getClass()).getURL();
		BufferedImageView view = new BufferedImageView(viewContext, url, propertyKey);
		connectableViewMap.put(propertyKey, view);
		paperView.addChild(view);
		return view;
	}

	private void createHeadlineView() throws MalformedURLException, Exception
	{
		URL url = new ClassPathResource("html/headline.html").getURL();
		headlineView = new HeadLineView(viewContext, url, url);
		paperView.addChild(headlineView);
	}

	private void createLegendView() throws MalformedURLException, Exception
	{
		URL url = new ClassPathResource("html/legend.html").getURL();
		HTMLView htmlView = new PropertyPersistableHTMLView(viewContext, url, url)
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
		paperView.addChild(htmlView);
	}

	@Override
	protected String getFileContentName()
	{
		return "Architecture Graph";
	}

	@Override
	protected String getFileExtension()
	{
		return "properties";
	}

	@Override
	protected String getFileFilterDescription()
	{
		return "JFreeView Architecture Graph";
	}

	@Override
	protected void writeToStream(FileOutputStream stream)
	{
		Properties properties = new Properties();
		architectureGraphModel.write(properties);
		try
		{
			properties.store(stream, "Created by " + System.getProperty("user.name"));
		}
		catch (IOException e)
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
	}

	private void createActionMenu(JMenuBar menuBar)
	{
		JMenu actionMenu = new JMenu("Action");
		menuBar.add(actionMenu);
		actionMenu.add(new NewColorsAction(paperView));
		actionMenu.add(new LockColorsAction(paperView));
		actionMenu.add(new UnlockColorsAction(paperView));
	}

	@Override
	protected Image getIcon()
	{
		return new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage();
	}

	@Override
	protected FileEditor openNew()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected FileEditor readModel(File file) throws Exception
	{
		ArchitectureGraphModel model = readModelFromFile(file);
		return open(model, file);
	}

	private ArchitectureGraphModel readModelFromFile(File file) throws FileNotFoundException, IOException
	{
		ArchitectureGraphModel model = new ArchitectureGraphModel();
		model.read(file);
		return model;
	}
	
	private FileEditor open(ArchitectureGraphModel modelToOpen, File file)
	{
		ArchitectureGraphFileEditor editor = new ArchitectureGraphFileEditor(modelToOpen, desktopApp);
		editor.createWindow();
		editor.boundTo(file);
		return editor;
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

		@Override
		protected String getGeometryPropertyKey()
		{
			return "headline";
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

	private static class BlockView extends AbstractParentView implements TransferableView, PropertyPersistableView,
			ConnectableView
	{
		private String propertyKey;

		public BlockView(ViewContext viewContext, Layout layoutManager, String propertyKey)
		{
			super(viewContext, layoutManager);
			this.propertyKey = propertyKey;
			setHeight(100);
			setWidth(100);
			createResizeHandles();
		}

		@Override
		public Object getModel()
		{
			return null;
		}

		public Point2D get3DEffectSize()
		{
			double minDimension = Math.min(getWidth(), getHeight());
			return new Point2D.Double(minDimension * 0.07, minDimension * 0.07);
		}

		@Override
		public Shape getOpticalShape()
		{
			Point2D effectSize = get3DEffectSize();
			return expandOpticalShape(new Rectangle2D.Double(getX(), getY() - effectSize.getY(), getWidth()
					+ effectSize.getX(), getHeight() + effectSize.getY()));
		}

		@Override
		/*
		 * Mark children that is located to the top and/or right edge
		 */
		protected void layout()
		{
			super.layout();
			traverseDepthFirst(new ViewVisitor()
			{
				public void visit(BasicView view)
				{
					if (view instanceof TextBlockView)
					{
						TextBlockView textBlockView = (TextBlockView) view;
						textBlockView.setDrawTopSide3DEffect(Math.abs(textBlockView.getLocationInRoot().getY()
								- getLocationInRoot().getY()) < 2);
						textBlockView.setDrawRightSide3DEffect(Math.abs(textBlockView.getLocationInRoot().getX()
								+ textBlockView.getWidth() - (getLocationInRoot().getX() + getWidth())) < 2);
					}
				}
			});
		}

		@Override
		public void readProperties(Properties properties)
		{
			PropertyPersistUtil.readBounds(properties, this, getPropertyKey());
		}

		@Override
		public void writeProperties(Properties properties)
		{
			PropertyPersistUtil.writeBounds(properties, this, getPropertyKey());
		}

		private String getPropertyKey()
		{
			return propertyKey;
		}

		@Override
		public Shape getConnectionShape()
		{
			return getOpticalShape();
		}
	}
	
	private static class SlaveBlockView extends BlockView
	{
		private BlockView master;
		
		public SlaveBlockView(ViewContext viewContext, Layout layoutManager, String propertyKey, BlockView master)
		{
			super(viewContext, layoutManager, propertyKey);
			this.master = master;
		}
		
		@Override
		public Point2D get3DEffectSize()
		{
			return master.get3DEffectSize();
		}
		
	}

	private class StructureView extends AbstractParentView
	{
		public StructureView(ViewContext viewContext)
		{
			this(viewContext, null);
		}

		public StructureView(ViewContext viewContext, Layout layoutManager)
		{
			super(viewContext, layoutManager);
		}

		@Override
		public float getMinHeight()
		{
			return (float) getLayout().getMinimumLayoutSize(this).getHeight();
		}

		@Override
		public float getMinWidth()
		{
			return (float) getLayout().getMinimumLayoutSize(this).getWidth();
		}

		@Override
		public Shape getOpticalShape()
		{
			BlockView blockView = getBlockView();
			if (blockView != null)
			{
				Point2D effectSize = blockView.get3DEffectSize();
				return expandOpticalShape(new Rectangle2D.Double(getX(), getY(), getWidth() + effectSize.getX(),
						getHeight() + effectSize.getY()));
			}
			else
				return super.getOpticalShape();
		}

		private BlockView getBlockView()
		{
			List<ParentView> parentChain = getParentChain();
			for (ParentView parentView : parentChain)
			{
				if (parentView instanceof BlockView)
					return (BlockView) parentView;
			}
			return null;
		}

		@Override
		protected void drawBeforeChildren(Paintable g)
		{
			super.drawBeforeChildren(g);
			g.setColor(Color.LIGHT_GRAY);
			g.fill(getBounds());
		}

	}

	private class TextBlockView extends BasicView implements Layoutable, PopupMenuProvider, PropertyPersistableView,
			ConnectableView
	{
		protected static final String FILL_COLOR = "fillColor";

		private JPopupMenu popupMenu;
		private RandomColorProvider fillColor = new RandomColorProvider();
		private String text;
		private Font font;
		private boolean vertically;
		private float inset;
		private AbstractAction lockColorAction;
		private boolean drawTopSide3DEffect;
		private boolean drawRightSide3DEffect;

		public TextBlockView(ViewContext viewContext, String text, Font font)
		{
			this(viewContext, text, font, false, 0.8f);
		}

		public TextBlockView(ViewContext viewContext, String text, Font font, float inset)
		{
			this(viewContext, text, font, false, inset);
		}

		public TextBlockView(ViewContext viewContext, String text, Font font, boolean vertically)
		{
			this(viewContext, text, font, vertically, 0.8f);
		}

		public TextBlockView(final ViewContext viewContext, String text, Font font, boolean vertically, float inset)
		{
			super(viewContext);
			this.text = text;
			this.font = font;
			this.vertically = vertically;
			this.inset = inset;
			createPopupMenu();
			addPropertyChangeListener(FILL_COLOR, new PropertyChangeListener()
			{

				@Override
				public void propertyChange(PropertyChangeEvent evt)
				{
					viewContext.repaint(TextBlockView.this);
					synchronizeActionState();

				}
			});

			synchronizeFillColor(this);
			connectableViewMap.put(text, this);
		}

		private void createPopupMenu()
		{
			popupMenu = new JPopupMenu();
			popupMenu.add(new JMenuItem(new AbstractAction("New Color")
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					generateColor();

				}
			}));
			lockColorAction = new AbstractAction("Lock Color")
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					setFillColorLocked(!fillColor.isLocked());
				}

			};
			synchronizeActionState();
			popupMenu.add(new JCheckBoxMenuItem(lockColorAction));
		}

		private void synchronizeActionState()
		{
			lockColorAction.putValue(Action.SELECTED_KEY, fillColor.isLocked());
		}

		public void doubleClickGesture(DoubleClickGesture doubleClickGesture)
		{
			generateColor();
		}

		public void setDrawTopSide3DEffect(boolean drawTopSide3DEffect)
		{
			this.drawTopSide3DEffect = drawTopSide3DEffect;
		}

		public void setDrawRightSide3DEffect(boolean drawRightSide3DEffect)
		{
			this.drawRightSide3DEffect = drawRightSide3DEffect;
		}

		public void showPopupMenu(Component invoker, Point point)
		{
			popupMenu.show(invoker, point.x, point.y);
		}

		/**
		 * Used to assign all views the with of the widest.
		 */
		public float getMinWidth()
		{
			Rectangle2D stringBounds = getStringBounds(font, text);
			return (float) ((vertically ? stringBounds.getHeight() : stringBounds.getWidth()) + (getInset() * 2));
		}

		private float getInset()
		{
			return (float) (font.getSize2D() * inset);
		}

		/**
		 * Used to assign all views the height of the highest.
		 */
		public float getMinHeight()
		{
			Rectangle2D stringBounds = getStringBounds(font, text);
			return (float) ((vertically ? stringBounds.getWidth() : stringBounds.getHeight()) + (getInset() * 2));
		}

		@Override
		protected Font getFont()
		{
			return font;
		}

		@Override
		protected void paintLayer(Paintable g)
		{
			g.setColor(fillColor.getColor().darker());
			g.fill(getBounds());
			g.setColor(whiteIfDarkBackground(fillColor.getColor().darker()));
			g.setFont(font);
			drawCenteredText(g, text, vertically);
			drawTopSide3DEffect(g);
			drawRightSide3DEffect(g);
		}

		protected void drawTopSide3DEffect(Paintable g)
		{
			if (drawTopSide3DEffect)
			{
				g.setColor(fillColor.getColor().darker().darker());
				g.fill(calculateTopSide3DEffect());
			}
		}

		protected void drawRightSide3DEffect(Paintable g)
		{
			if (drawRightSide3DEffect)
			{
				g.setColor(fillColor.getColor());
				g.fill(calculateRightSide3DEffect());
			}
		}

		@Override
		public Shape getOpticalShape()
		{
			BlockView blockView = getBlockView();
			if (blockView != null)
			{
				Point2D effectSize = blockView.get3DEffectSize();
				return expandOpticalShape(new Rectangle2D.Double(getX(), getY() - effectSize.getY(), getWidth()
						+ effectSize.getX(), getHeight() + effectSize.getY()));
			}
			else
				return super.getOpticalShape();
		}

		private Shape calculateTopSide3DEffect()
		{
			Rectangle2D bounds = getBounds();
			Point2D pt = getBlockView().get3DEffectSize();
			Path2D path = new Path2D.Float();
			double x = bounds.getX();
			double y = bounds.getY();
			path.moveTo(x, y);
			path.lineTo(x + pt.getX(), y - pt.getY());
			path.lineTo(x + pt.getX() + bounds.getWidth(), y - pt.getY());
			path.lineTo(x + bounds.getWidth(), y);
			path.closePath();
			return path;
		}

		private Shape calculateRightSide3DEffect()
		{
			Rectangle2D bounds = getBounds();
			Point2D pt = getBlockView().get3DEffectSize();
			Path2D path = new Path2D.Float();
			double x = bounds.getX() + bounds.getWidth();
			double y = bounds.getY();
			path.moveTo(x, y);
			path.lineTo(x + pt.getX(), y - pt.getY());
			path.lineTo(x + pt.getX(), y - pt.getY() + bounds.getHeight());
			path.lineTo(x, y + bounds.getHeight());
			path.closePath();
			return path;
		}

		protected TextBlockView setFillColor(RandomColorProvider fillColor)
		{
			if (!fillColor.equals(this.fillColor))
			{
				this.fillColor = fillColor;
				firePropertyChange(FILL_COLOR);
			}
			return this;
		}

		@Override
		public void readProperties(Properties properties)
		{
			String colorProperty = properties.getProperty(getColorPropertyKey());
			if (colorProperty != null)
			{
				setFillColor(new RandomColorProvider(Color.decode(colorProperty)));
			}
		}

		@Override
		public void writeProperties(Properties properties)
		{
			fillColor.writeProperties(getColorPropertyKey(), properties);
		}

		private String getColorPropertyKey()
		{
			return getPropertyKey() + ".color";
		}

		private String getPropertyKey()
		{
			return text.replaceAll(" ", "_").toLowerCase();
		}

		public RandomColorProvider getFillColor()
		{
			return fillColor;
		}

		public void generateColorIfUnlocked()
		{
			if (!fillColor.isLocked())
				generateColor();
		}

		public void generateColor()
		{
			fillColor.generateColor();
			firePropertyChange(FILL_COLOR);
		}

		public void setFillColorLocked(boolean locked)
		{
			if (fillColor.isLocked() != locked)
			{
				fillColor.setLocked(locked);
				firePropertyChange(FILL_COLOR);
			}
		}

		@Override
		public Shape getConnectionShape()
		{
			Shape connectionShape = getBounds();
			if (drawRightSide3DEffect)
			{
				Area area = new Area(connectionShape);
				area.add(new Area(calculateRightSide3DEffect()));
				if (drawTopSide3DEffect)
					area.add(new Area(calculateTopSide3DEffect()));
				return area;
			}
			return connectionShape;
		}

		private BlockView getBlockView()
		{
			List<ParentView> parentChain = getParentChain();
			for (ParentView parentView : parentChain)
			{
				if (parentView instanceof BlockView)
					return (BlockView) parentView;
			}
			return null;
		}

	}

	/**
	 * Used to synchronize the fill color property in text views.
	 */
	private static class FillColorSynchronizer
	{
		public FillColorSynchronizer(final TextBlockView view1, final TextBlockView view2)
		{
			super();
			if (view1 == view2)
				throw new IllegalArgumentException("The arguments must be two different objects");
			addFillColorListener(view1, view2);
			addFillColorListener(view2, view1);
			synchronize(view1, view2);
		}

		private void addFillColorListener(final TextBlockView view1, final TextBlockView view2)
		{
			view1.addPropertyChangeListener(TextBlockView.FILL_COLOR, new PropertyChangeListener()
			{

				@Override
				public void propertyChange(PropertyChangeEvent evt)
				{
					synchronize(view1, view2);

				}
			});
		}

		/**
		 * Avoid sharing the same instance since a color change will not trigger any repaint.
		 */
		private void synchronize(TextBlockView view1, TextBlockView view2)
		{
			view2.setFillColor(new RandomColorProvider(view1.getFillColor()));
		}
	}

	/**
	 * One instance of RandomColorProvider can be shared among several View's that gets a repaint
	 * call when the color changes.
	 */
	private static class RandomColorProvider
	{
		private Color color;
		private boolean locked;

		public RandomColorProvider()
		{
			locked = false;
			generateColor();
		}

		public RandomColorProvider(Color color)
		{
			this.color = color;
			locked = true;
		}

		public RandomColorProvider(Color color, boolean locked)
		{
			super();
			this.color = color;
			this.locked = locked;
		}

		public RandomColorProvider(RandomColorProvider other)
		{
			color = other.color;
			locked = other.locked;
		}

		public void generateColor()
		{
			color = new Color(RANDOM.nextFloat(), RANDOM.nextFloat(), RANDOM.nextFloat());
		}

		public Color getColor()
		{
			return color;
		}

		public boolean isLocked()
		{
			return locked;
		}

		public void setLocked(boolean locked)
		{
			this.locked = locked;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
				return false;
			if (obj == this)
				return true;
			if (obj.getClass() != getClass())
				return false;
			RandomColorProvider another = (RandomColorProvider) obj;
			return new EqualsBuilder().append(color, another.color).append(locked, another.locked).isEquals();
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(9, 19).append(color).append(locked).toHashCode();
		}

		public void writeProperties(String key, Properties properties)
		{
			if (locked)
				properties.put(key, encode(color));
		}

		private static String encode(Color c)
		{
			char[] buf = new char[7];
			buf[0] = '#';
			String s = Integer.toHexString(c.getRed());
			if (s.length() == 1)
			{
				buf[1] = '0';
				buf[2] = s.charAt(0);
			}
			else
			{
				buf[1] = s.charAt(0);
				buf[2] = s.charAt(1);
			}
			s = Integer.toHexString(c.getGreen());
			if (s.length() == 1)
			{
				buf[3] = '0';
				buf[4] = s.charAt(0);
			}
			else
			{
				buf[3] = s.charAt(0);
				buf[4] = s.charAt(1);
			}
			s = Integer.toHexString(c.getBlue());
			if (s.length() == 1)
			{
				buf[5] = '0';
				buf[6] = s.charAt(0);
			}
			else
			{
				buf[5] = s.charAt(0);
				buf[6] = s.charAt(1);
			}
			return String.valueOf(buf);
		}
	}

	private static class NewColorsAction extends DefaultAction
	{
		private AbstractParentView rootView;

		public NewColorsAction(AbstractParentView rootView)
		{
			super("New Colors");
			this.rootView = rootView;
			setAccelerator(KeyStroke.getKeyStroke("control 0"));
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			rootView.traverseDepthFirst(new ViewVisitor()
			{
				@Override
				public void visit(AbstractView view)
				{
					if (view instanceof TextBlockView)
						((TextBlockView) view).generateColorIfUnlocked();
				}
			});
			ViewContext viewContext = rootView.getViewContext();
			viewContext.repaint(rootView);
			viewContext.setHasUnconfirmedChanges(true);
		}
	}

	private static class LockColorsAction extends DefaultAction
	{
		private AbstractParentView rootView;

		public LockColorsAction(AbstractParentView rootView)
		{
			super("Lock All Colors");
			this.rootView = rootView;
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			rootView.traverseDepthFirst(new ViewVisitor()
			{
				@Override
				public void visit(AbstractView view)
				{
					if (view instanceof TextBlockView)
					{
						((TextBlockView) view).setFillColorLocked(true);
					}
				}
			});
			ViewContext viewContext = rootView.getViewContext();
			viewContext.repaint(rootView);
			viewContext.setHasUnconfirmedChanges(true);
		}

	}

	private static class UnlockColorsAction extends DefaultAction
	{
		private AbstractParentView rootView;

		public UnlockColorsAction(AbstractParentView rootView)
		{
			super("Unlock All Colors");
			this.rootView = rootView;
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			rootView.traverseDepthFirst(new ViewVisitor()
			{
				@Override
				public void visit(AbstractView view)
				{
					if (view instanceof TextBlockView)
						((TextBlockView) view).setFillColorLocked(false);
				}
			});
			ViewContext viewContext = rootView.getViewContext();
			viewContext.repaint(rootView);
			viewContext.setHasUnconfirmedChanges(true);
		}

	}

}
