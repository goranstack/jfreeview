package se.bluebrim.isac.dbschema;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.carbonfive.db.migration.DriverManagerMigrationManager;
import com.carbonfive.db.migration.MigrationManager;

import se.bluebrim.desktop.graphical.PropertyPersistableHTMLView;
import se.bluebrim.desktop.graphical.PropertyPersistableView;
import se.bluebrim.view.DesktopView;
import se.bluebrim.view.Handle;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.PaperView;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.TestOptionMenuBuilder;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.impl.ActionModifiers;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.SlaveView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.select.StandardSelectionManager;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.tool.DragAndDropTool;
import se.bluebrim.view.tool.SelectionTool;
import se.bluebrim.view.tool.Tool;
import se.bluebrim.view.transaction.MockTransactionManager;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.ZoomController;


/**
 * TODO:
 * <ul>
 * <li>Constrained move when shift is down</li>
 * <li>Hide tables</li>
 * <li>Option menu for orthogonal or diagonal line drawing</li>
 * <li>More information for each column. Data type, size etc</li>
 * <li>Select information that is displayed via GUI</li>
 * <li>PDF and JPEG output.</li>
 * <li>PDF with links to description text boxes on page two</li>
 * <li>Box size slider. Box size is a function of font size</li>
 * <li>Save action</li>
 * <li>Quit menu item</li>
 * <li>Save changes before quit?</li>
 * See: http://www.datamodel.org/DataModelCardinality.html</li>
 * <li>Actions for aligning</li>
 * <li>Layout manager when there is no position file. Use dependency sorter</li>
 * <li>Database URL and login from property file</li>
 * <li>Row count for each table</li>
 * <li>Improve connection redrawing. Multiple move in MoveTool/MoveHandle</li>
 * <li>Fix bug: Enter scale factor manually do not behave correct as selecting in the menu.</li>
 * </ul>
 * Have a look at: http://en.wikipedia.org/wiki/Entity-relationship_model
 * 
 * Layout examples:
 * http://www.jeewiz.com/Assets/hospital/hospital_uml_main.gif
 * http://edocs.bea.com/aldsp/docs20/datasrvc/wwimages/big_model.gif
 * 
 * Database model samples
 * http://www.databaseanswers.org/tutorial4_db_schema/index.htm
 * https://petstore.dev.java.net/
 * 
 * Modeling tools
 * http://www.databaseanswers.com/modelling_tools.htm
 * 
 * @author GStack
 */
public class SchemaGraphBuilder
{
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static final Color LINE_COLOR = new Color(153, 0, 51);
	private static final Color BOX_FILL_COLOR = new Color(255, 255, 204);
	
	public static void main(String[] args) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		String dbName = "example-db";
		new SchemaGraphBuilder(dbName).run();
	}
	
	private Connection connection;
	private String databaseName;
	private List<Table> tables = new ArrayList<Table>();
	private DatabaseMetaData metaData;
	private static Font tableNameFont = new Font("Arial", Font.BOLD, 10);
	private static Font columnNameFont = new Font("Arial", Font.PLAIN, 10);
	private static BufferedImage keyIcon;
	private List<PropertyPersistableView> propertyBasedGeometryViews = new ArrayList<PropertyPersistableView>();
	private JFrame frame;
	private PrinterJob printerJob;
	private PageFormat pageFormat;
	private SchemaGraphPaperView paperView;
	private List<String> sqlFileNames;
	private JFileChooser pdfFileChooser;

	public SchemaGraphBuilder(String databaseName) throws Exception
	{
		super();
		this.databaseName = databaseName;
		this.connection = createConnection(databaseName);
		metaData = connection.getMetaData();
		getClass().getClassLoader();
		keyIcon = ImageIO.read(getClass().getResourceAsStream("key-icon.png"));
		initPageFormat();
		pdfFileChooser = new JFileChooser();
		pdfFileChooser.setSelectedFile(createPdfFile());
	}

	private File createPdfFile()
	{
		return new File(databaseName + " ER diagram.pdf");
	}
		
	protected void run() throws Exception
	{
		Properties schemaProperties = new Properties();
		File file = new File(getSchemaPropertiesFilePath());
		if (file.exists())
			schemaProperties.load(new FileReader(file));				

		buildModel();
		applyModelProperties(schemaProperties);
		buildWindow();
		ViewPanel viewPanel = buildViewPanel(createMenuBar(frame));
		applyViewProperties(schemaProperties);
		
		frame.setSize(viewPanel.getUnscaledSize());	
		centerOnScreen(frame);
		frame.setVisible(true);
	}

	private void buildWindow()
	{
		frame = new JFrame(databaseName);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);				
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e)
			{
				saveSchemaProperties();
				super.windowClosing(e);
			}
		});
	}

	private ViewPanel buildViewPanel(JMenu scaleMenu) throws Exception
	{
		ViewContext viewContext = new ViewContext(new Scale(), new Scale(), frame);
		
		DesktopView desktopView = new DesktopView(viewContext);
		paperView = new SchemaGraphPaperView(viewContext, this);
		desktopView.setPaperView(paperView);
		
		ViewPanel viewPanel = new ViewPanel(desktopView, viewContext);
		viewPanel.setBackground(DesktopView.DESKTOP_COLOR);
		viewContext.setComponent(viewPanel);
		
		SelectionManager selectionManager = new StandardSelectionManager(viewPanel);
		selectionManager.setRootView(desktopView);
		viewContext.setSelectionManager(selectionManager);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportView(viewPanel);
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		JToolBar toolBar = new JToolBar();		
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		ZoomController zoomController = new ZoomController(viewPanel, scrollPane, toolBar, scaleMenu);
		Tool selectionTool = new SelectionTool(zoomController, true);
		selectionTool.setTransactionManager(new MockTransactionManager());
		selectionTool.select();

		MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher(contentPane);
		viewPanel.addMouseListener(mouseEventDispatcher);
		viewPanel.addMouseMotionListener(mouseEventDispatcher);
		mouseEventDispatcher.addToolDispatcher(zoomController);
		viewPanel.updateCashedViewValues();

		return viewPanel;
	}
		
	/**
	 * TODO: Returns the view menu since that is needed in the constructor to the ZoomController
	 * and we like avoid an extra instance variable just for that purpose. Not very pretty should be fixed.
	 */
	private JMenu createMenuBar(JFrame frame)
	{
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		createFileMenu(menuBar);
		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);

		JMenu menu = new JMenu("Test");
		menuBar.add(menu);
		createTestOptionMenuItems(menu);

		return viewMenu;
	}
	
	protected void createFileMenu(JMenuBar menuBar)
	{	    
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
	    // Page Format menu item
		JMenuItem item = new JMenuItem("Page Setup...");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				pageFormatDialog();
			}
		});
		fileMenu.add(item);
	
	    // Print menu item
		item = new JMenuItem("Print...");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printWindow(paperView);
			}
		});
		fileMenu.add(item);
		
    // Save as PDF item
		item = new JMenuItem("Save as PDF...");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				paperView.saveAsPdf(pdfFileChooser, frame);
			}
		});
		fileMenu.add(item);	

	}
	

	private void createTestOptionMenuItems(JMenu menu)
	{
		TestOptionMenuBuilder testOptionMenuBuilder = new TestOptionMenuBuilder();
		testOptionMenuBuilder.addOption("Mark dirty regions", Paintable.RANDOM_COLORS);		
		testOptionMenuBuilder.addOption("Use MoveHandle", DragAndDropTool.USE_MOVE_HANDLE);
		testOptionMenuBuilder.addMenuItems(menu);
	}

	private URI getSchemaPropertiesFilePath()
	{
		try {
			return getClass().getResource("/config/schema.properties").toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void printWindow(Printable erDiagram)
	{
		PrinterJob printerJob = getPrinterJob();
		printerJob.setJobName(getPrinterJobName());	
	    printerJob.setPrintable(erDiagram, getPageFormat());
	    
	     try {
	    	 if (printerJob.printDialog())
	    	 	printerJob.print();
	    } catch (PrinterException e) {
	    	JOptionPane.showMessageDialog(frame, "Kunde inte skriva ut beroende på\n" + e, "Fel vid utskrift", JOptionPane.ERROR_MESSAGE);
	    }
	}

	protected String getPrinterJobName()
	{
		return "ER Diagram";
	}
	
	
	private PageFormat getPageFormat()
	{
		if (pageFormat == null)
			getPrinterJob();
		return pageFormat;
	}

	private PrinterJob getPrinterJob()
	{
		if (printerJob == null)
		{
			printerJob = PrinterJob.getPrinterJob();
			pageFormat = printerJob.defaultPage();
		}
		return printerJob;
	}

	protected void pageFormatDialog()
	{
		pageFormat = getPrinterJob().pageDialog(pageFormat);
//		paperOnDesktopView.setPageFormat(pageFormat);
	}

	private void initPageFormat()
	{
		PageFormat pageFormat = getPageFormat();
		pageFormat.setOrientation(PageFormat.LANDSCAPE);
		setDefaultPageMargins(pageFormat);
	}
	
	/**
	 * Set default margins for the printable view on printed page
	 */
	private void setDefaultPageMargins(PageFormat pageFormat)
	{
		Paper paper = pageFormat.getPaper();
		paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
		pageFormat.setPaper(paper);
	}
	
	private static void centerOnScreen(Window window) 
	{
		window.setBounds(centerOnScreen(window.getSize()));
	}

	private static Rectangle centerOnScreen(Dimension size) {
		return centerOnScreen(size.width, size.height);
	}
	
	/**
	 * Return a rectangle of the given width and height so that it is centered
	 * on and cropped within the <em>usable</em> part of the screen.
	 * 
	 */
	private static Rectangle centerOnScreen(int width, int height) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = ge.getCenterPoint();
		Rectangle rect = new Rectangle(center.x - (width / 2), center.y - (height / 2), width, height);
		return rect.intersection(ge.getMaximumWindowBounds());
	}
	
	/**
	 * Saves the positions of the table views in a properties file
	 */
	private void saveSchemaProperties()
	{
		Properties schemaProperties = new Properties();
		for (PropertyPersistableView view : propertyBasedGeometryViews)
		{
			view.writeProperties(schemaProperties);
		}
		for (Table table : tables)
		{
			table.writeProperties(schemaProperties);
		}
		
		File file = new File(getSchemaPropertiesFilePath());
		try
		{
			file.createNewFile();
			schemaProperties.store(new FileWriter(file), null);
		} catch (IOException e)
		{
			throw new RuntimeException("Unable to store properties");
		}		
	}
	
	/**
	 * Apply properties to the views that has been saved in the schema properties file.
	 * The schema properties file contains improvements to the graph made by positioning 
	 * the objects with the mouse.
	 */
	private void applyViewProperties(Properties schemaProperties) throws FileNotFoundException, IOException
	{
		for (PropertyPersistableView view : propertyBasedGeometryViews)
		{
			view.readProperties(schemaProperties);
		}
	}

	private void applyModelProperties(Properties schemaProperties) throws FileNotFoundException, IOException
	{
		for (Table table : tables)
		{
			table.readProperties(schemaProperties);
		}
	}
	
	

	private void buildModel() throws SQLException
	{
		ResultSet rs = metaData.getTables(null, null, null, new String[]{ "TABLE" });
		while (rs.next())
		{
			System.out.println(rs.getString("TABLE_NAME"));
			tables.add(new Table(rs.getString("TABLE_NAME")));
		}
		for (Table table : tables)
		{
			table.updateForeignKeyList();
		}
		connection.close();
	}
	
	private Table getTable(String tableName)
	{
		for (Table table : tables)
		{
			if (table.name.equals(tableName))
				return table;
		}
		throw new IllegalArgumentException("No table called: \"" + tableName + "\"");
	}

	/**
	 * http://code.google.com/p/c5-db-migration/wiki/ApplicationEmbedding
	 */
	protected Connection createConnection(String databaseName) throws Exception {
		String databaseURL = "jdbc:hsqldb:mem:" + databaseName;
		String databaseUser = "sa";
		String password = "";
		
		MigrationManager migrationManager = new DriverManagerMigrationManager(
				"org.hsqldb.jdbcDriver", databaseURL, databaseUser, password);
		migrationManager.migrate();

		Connection connection = DriverManager.getConnection(databaseURL, databaseUser, password);
		return connection;
	}
	
	private class Table
	{
		private String name;
		private String improvedName;
		private List<Column> columns = new ArrayList<Column>();
		private List<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();

		public Table(String name) throws SQLException
		{
			this.name = name;
			ResultSet rs = metaData.getColumns(null, null, name, null);
			while (rs.next())
			{
				String columnName = rs.getString("COLUMN_NAME");
				String isNullable = rs.getString("IS_NULLABLE");
				System.out.println("     " + columnName + " isNullable: " + isNullable);
				columns.add(new Column(this, columnName, isNullable));
			}
			rs = metaData.getPrimaryKeys(null, null, name);
			while (rs.next())
			{
				String columnName = rs.getString("COLUMN_NAME");
				System.out.println("     PK:" + columnName);
				getColumn(columnName).isPrimaryKey = true;
			}
			sortColumns();
		}
		
		public void readProperties(Properties properties)
		{
			improvedName = properties.getProperty(getImprovedNamePropertyKey());			
		}

		public void writeProperties(Properties properties)
		{
			if (improvedName != null)
				properties.setProperty(getImprovedNamePropertyKey(), improvedName);
		}
		
		private String getImprovedNamePropertyKey()
		{
			return name + ".improved.name";
		}

		public String getDisplayName()
		{
			return improvedName != null ? improvedName : name;
		}
		
		private void sortColumns()
		{
			Collections.sort(columns, new Comparator<Column>(){

				@Override
				public int compare(Column column1, Column column2)
				{
					int column1SortValue = column1.isPrimaryKey ? 0 : 1;
					int column2SortValue = column2.isPrimaryKey ? 0 : 1;
					return column1SortValue - column2SortValue;
				}});
		}
		
		/**
		 * The JDBC meta data API gives you the foreign keys of the referenced table and
		 * not in the table they where defined and thats exactly how we want it but can be
		 * a little confusing when it comes to the semantic.
		 */
		public void updateForeignKeyList() throws SQLException
		{
			ResultSet rs = metaData.getExportedKeys(null, null, name);
			while (rs.next())
			{
				String foreignKeyTableName = rs.getString("FKTABLE_NAME"); 	// The table that defines the foreign key
				String foreignKeyColumnName = rs.getString("FKCOLUMN_NAME");	// The foreign key column in the defining table
				Table definingTable = getTable(foreignKeyTableName);
				foreignKeys.add(new ForeignKey(definingTable, this, definingTable.getColumn(foreignKeyColumnName)));
				System.out.println("Added foreign key " + foreignKeyTableName + " to " + name);
			}
		}

		public Column getColumn(String columnName)
		{
			for (Column column : columns)
			{
				if (column.name.equals(columnName))
					return column;
			}
			throw new IllegalArgumentException("No column called: \"" + columnName + "\" in: " + name);
		}
		
	}
	
	/**
	 * Represents a foreign key definition. Stored in a list at the foreign table since
	 * the JDBC meta data data API present them in that way and that also suit our needs.
	 */
	private class ForeignKey
	{
		private Table definingTable;
		private Table foreignTable;			// The referenced table
		private Column foreignKeyColumn;	// The foreign key column in the defining table

		public ForeignKey(Table definingTable, Table foreignTable, Column foreignKeyColumn)
		{
			this.definingTable = definingTable;
			this.foreignTable = foreignTable;
			this.foreignKeyColumn = foreignKeyColumn;
		}

		public Table getForeignTable()
		{
			return foreignTable;
		}

		public Column getForeignKeyColumn()
		{
			return foreignKeyColumn;
		}

		public Table getDefiningTable()
		{
			return definingTable;
		}

		
	}
	
	private class Column
	{
		private Table table;
		private String name;
		private String isNullable;
		private boolean isPrimaryKey = false;
		
		/**
		 * IS_NULLABLE String => ISO rules are used to determine the nullability for a column. 
		 * YES --- if the parameter can include NULLs 
		 * NO --- if the parameter cannot include NULLs 
		 * empty string --- if the nullability for the parameter is unknown
		 * 
		 */
		public Column(Table table, String name, String isNullable)
		{
			this.table = table;
			this.name = name;
			this.isNullable = isNullable;
		}
		
		@Override
		public String toString()
		{
			return "Column: " + name + " in table: " + table.name;
		}
		
		public boolean isNullable()
		{
			return isNullable != null && isNullable.equalsIgnoreCase("YES");
		}
	}
		
	public static class SchemaGraphPaperView extends PaperView
	{
		public SchemaGraphPaperView(ViewContext viewContext, SchemaGraphBuilder schemaGraphBuilder) throws Exception
		{
			super(viewContext);
			buildTableViews(viewContext, schemaGraphBuilder);
			buildHTMLViews(viewContext, schemaGraphBuilder);
		}
		
		private void buildTableViews(ViewContext viewContext, SchemaGraphBuilder schemaGraphBuilder)
		{
			List<TableView> tableViews = new ArrayList<TableView>();
			for (Table table : schemaGraphBuilder.tables)
			{
				TableView tableView = new TableView(viewContext, table);
				addChild(tableView);	
				tableViews.add(tableView);
				schemaGraphBuilder.propertyBasedGeometryViews.add(tableView);
			}
			for (TableView tableView : tableViews)
			{
				tableView.createConnectionsToChildren(this);
			}		
		}

		private void buildHTMLViews(ViewContext viewContext, SchemaGraphBuilder schemaGraphBuilder) throws Exception
		{
			URL legendURL = getClass().getResource("/config/legend.html").toURI().toURL();
			URL documentBase = legendURL;
			LegendView legendView = new LegendView(viewContext, documentBase, legendURL, schemaGraphBuilder);
			addChild(legendView);
			schemaGraphBuilder.propertyBasedGeometryViews.add(legendView);
			HeadlineView headlineView = new HeadlineView(viewContext, documentBase, getClass().getResource("/config/headline.html").toURI().toURL());
			addChild(headlineView);
			schemaGraphBuilder.propertyBasedGeometryViews.add(headlineView);
		}

		
		public TableView getViewFor(Table table)
		{
			for (Object object : getChildren())
			{
				if (object instanceof TableView)
				{
					TableView tableView = (TableView)object;
					if (tableView.table.equals(table))
						return tableView;
				}				
			}
			throw new RuntimeException("Table not found: " + table.name);
		}

	}

	private static class LegendView extends PropertyPersistableHTMLView
	{
		private SchemaGraphBuilder schemaGraphBuilder;
		
		public LegendView(ViewContext viewContext, URL documentBase, URL url, SchemaGraphBuilder schemaGraphBuilder) throws Exception
		{
			super(viewContext, documentBase, url);
			this.schemaGraphBuilder = schemaGraphBuilder;
		}
		
		@Override
		protected String getGeometryPropertyKey()
		{
			return "legend.x.y.width.height";
		}
		
		protected String filterHTML(String html)
		{
			String result = html.replace("@printing-date@", dateTimeFormat.format(new Date()));
			result = result.replace("@sql-files@", getSqlFileNameAsHtml());
			return result;
		}
		
		private String getSqlFileNameAsHtml()
		{
			if (schemaGraphBuilder.sqlFileNames == null)
				return "";
			StringBuffer buffer = new StringBuffer();
			buffer.append("<ol>");
			for (String name : schemaGraphBuilder.sqlFileNames)
			{
				buffer.append("<li>" + name + "</li>");
			}
			buffer.append("</ol>");

			return buffer.toString();
		}

	}
	
	private static class HeadlineView extends PropertyPersistableHTMLView
	{
		
		public HeadlineView(ViewContext viewContext, URL documentBase, URL url) throws Exception
		{
			super(viewContext, documentBase, url);
		}
		
		@Override
		protected String getGeometryPropertyKey()
		{
			return "headline.x.y.width.height";
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

	
	private static class TableView extends BasicView implements Layoutable, TransferableView, PropertyPersistableView
	{		
		private static final float RULER_HEIGHT = 4;
		private float insets = 3;
		private float columnInset = 12;
		private Table table;
		private List<ConnectionView> connectionViews = new ArrayList<ConnectionView>();
		private boolean isMoving = false;
		private List<ConnectionEdge> connectionEdges;

		public TableView(ViewContext viewContext, Table table)
		{
			super(viewContext);
			this.table = table;
			calculateSize();
			connectionEdges = new ArrayList<ConnectionEdge>();
			connectionEdges.add(new NorthConnectionEdge(this));
			connectionEdges.add(new SouthConnectionEdge(this));
			connectionEdges.add(new WestConnectionEdge(this));
			connectionEdges.add(new EastConnectionEdge(this));
		}
					
		public void writeProperties(Properties properties)
		{
			properties.setProperty(getGeometryPropertyKey(), Float.toString(getX()) + ";" + Float.toString(getY()));
		}

		
		public void readProperties(Properties properties)
		{
			String string = properties.getProperty(getGeometryPropertyKey());
			if (string != null)
			{
				String[] parts = string.split(";");
				setLocation(new Point2D.Float(Float.valueOf(parts[0]), Float.valueOf(parts[1])));	
			}
		}
	
		private String getGeometryPropertyKey()
		{
			return table.name + ".x.y";
		}

		public void createConnectionsToChildren(SchemaGraphPaperView rootView)
		{			
			for (ForeignKey foreignKey : table.foreignKeys)
			{
				rootView.addChild(new OneToManyConnectionView(rootView, foreignKey));
			}
		}

		@Override
		protected void paintLayer(Paintable g)
		{
			super.paintLayer(g);
			AffineTransform oldAt = g.getGraphics().getTransform();		
			if (Paintable.RANDOM_COLORS.value) // For debugging of repaint areas
				g.setRandomColor();
			else
				g.setColor(BOX_FILL_COLOR);
			g.fill(getBounds());
			g.setColor(LINE_COLOR);
			g.setStroke(new BasicStroke(getFrameThickness()));
			g.draw(getBounds());
			g.setColor(Color.BLACK);
			g.setFont(tableNameFont);
			String tableName = table.getDisplayName();
			float tableNameFontAscent = tableNameFont.getLineMetrics(tableName, g.getFontRenderContext()).getAscent();			
			float tableNameFontDescent = tableNameFont.getLineMetrics(tableName, g.getFontRenderContext()).getDescent();			
			Rectangle2D textBounds = tableNameFont.getStringBounds(tableName, g.getFontRenderContext());
			g.translate(getX(), getY() + insets + tableNameFontAscent);
			g.drawString(tableName, (float)(getWidth()/2 - textBounds.getWidth()/2.0), 0);
			g.setFont(columnNameFont);
//			if (table.improvedName != null)
//			{
//				tableName = table.improvedName;
//				textBounds = tableNameFont.getStringBounds(tableName, g.getFontRenderContext());
//			}
			
			g.setColor(LINE_COLOR);
			g.draw(new Line2D.Double(0, tableNameFontDescent + RULER_HEIGHT/2.0, getWidth(), tableNameFontDescent + RULER_HEIGHT/2.0));
			g.translate(0, RULER_HEIGHT);
			g.setColor(Color.BLACK);
			g.translate(0, textBounds.getHeight());
			for (Column column : table.columns)
			{				
				String columnName = column.name;
				float columnNameFontAscent = columnNameFont.getLineMetrics(columnName, g.getFontRenderContext()).getAscent();
				textBounds = columnNameFont.getStringBounds(columnName, g.getFontRenderContext());
				g.drawString(columnName, columnInset, 0);
				if (column.isPrimaryKey)
				{
					AffineTransform at = new AffineTransform();
					double scale = keyIcon.getHeight() / textBounds.getHeight();
					at.scale(scale, scale);
					at.translate(1, -columnNameFontAscent);		// Text has base line as origin, icons has upper left corner
					g.drawRenderedImage(keyIcon, at);
				}
				g.translate(0, textBounds.getHeight());
			}
			g.getGraphics().setTransform(oldAt);
		}
		
		private void paintConnectionViews(Paintable g)
		{
			if (isMoving)
			{
				for (ConnectionView connectionView : connectionViews)
				{
					connectionView.paint(g);
				}
			}		
		}
		
		@Override
		public void startHandleManipulation(Handle handle)
		{
			super.startHandleManipulation(handle);
			isMoving = true;
		}
		
		@Override
		public void stopHandleManipulation(ActionModifiers modifiers)
		{
			super.stopHandleManipulation(modifiers);
			isMoving = false;
		}

		private void calculateSize()
		{			
			Rectangle2D tableNameBounds = tableNameFont.getStringBounds(table.getDisplayName(), ViewContext.DEFAULT_FONT_RENDER_CONTEXT);
			double height = tableNameBounds.getHeight();
			double width = tableNameBounds.getWidth();
			for (Column column : table.columns)
			{
				Rectangle2D columnNameBounds = columnNameFont.getStringBounds(column.name, ViewContext.DEFAULT_FONT_RENDER_CONTEXT);

				width = Math.max(columnNameBounds.getWidth(), width);
				height = height + columnNameBounds.getHeight();
			}					
			setHeight((float) height + (2 * insets) + RULER_HEIGHT);
			setWidth((float) width + columnInset + (2 * insets));			
		}
		
		@Override
		public Shape getOpticalShape()
		{
			Rectangle2D opticalShape = super.getOpticalShape().getBounds2D();
			if (isMoving)
			{
				for (ConnectionView connectionView : connectionViews)
				{
					opticalShape = opticalShape.createUnion(connectionView.getOpticalShape().getBounds2D());
				}
			}
			return opticalShape;
		}
		
		@Override
		public Object getModel()
		{
			return table;
		}
		
		public List<ConnectionEdge> getConnectionEdges()
		{
			return connectionEdges;
		}

		/**
		 * A line from the center from one table view to the center of another
		 * table view will intersect one edge unless the table views are overlapping.
		 * In that case null is returned.
		 */
		public ConnectionEdge getIntersectingEdge(Line2D testLine)
		{
			for (ConnectionEdge connectionEdge : connectionEdges)
			{
				if (connectionEdge.getEdgeLine().intersectsLine(testLine))
					return connectionEdge;
			}
			return null; // The table views are overlapping
		}
							
	}
	
	/**
	 * The notation of multiplicity is from Martin Fowler's book: UML Distilled Third Edition
	 *
	 */
	private static abstract class MultiplicityView extends SlaveView
	{
		private float textInset = 4;
		private boolean isStartPoint;
		private ConnectionView connectionView;
		
		public MultiplicityView(ConnectionView connectionView, boolean isStartPoint)
		{
			super(connectionView);
			this.connectionView = connectionView;
			this.isStartPoint = isStartPoint;
		}

		public void paint(Paintable g)
		{
			g.setPaint(Color.BLACK);
			g.setFont(columnNameFont);
			drawCenteredText(g, getText());
		}
		
		protected abstract String getText();
		
		@Override
		public float getWidth()
		{
			return (float) getStringWidth(columnNameFont, getText()) + 2 * textInset;
		}
		
		@Override
		public float getHeight()
		{
			return (float) getStringHeight(columnNameFont, getText() + 2 * textInset);
		}
		
		@Override
		public float getX()
		{
			return (float) getAnchorPoint().getX();		
			}
			
		@Override
		public float getY()
		{
			return (float) getAnchorPoint().getY();
		}
		
		private Point2D getAnchorPoint()
		{
			DecorationLayout decorationQuadrant;
			if (isStartPoint)
				decorationQuadrant = connectionView.getStartDecorationLayout();
			else
				decorationQuadrant = connectionView.getEndDecorationLayout();

			Rectangle2D bounds = getStringBounds(columnNameFont, getText());
			bounds.setFrame(bounds.getX(), bounds.getY(), bounds.getWidth() + textInset * 2, bounds.getHeight() + textInset * 2);
			decorationQuadrant.locate(bounds);			
			return new Point2D.Double(bounds.getX(), bounds.getY());
		}

		
		public Object getModel()
		{
			return getText();
		}		
	}
	
	private static class ExactlyOneMultiplicityView extends MultiplicityView
	{
		public ExactlyOneMultiplicityView(ConnectionView connectionView, boolean isStartPoint)
		{
			super(connectionView, isStartPoint);
		}

		@Override
		protected String getText()
		{
			return "1";
		}
	}

	
	private static class ZeroOrOneMultiplicityView extends MultiplicityView
	{
		public ZeroOrOneMultiplicityView(ConnectionView connectionView, boolean isStartPoint)
		{
			super(connectionView, isStartPoint);
		}

		@Override
		protected String getText()
		{
			return "0..1";
		}
	}
	
	private static class ZeroOrMoreMultiplicityView extends MultiplicityView
	{
		public ZeroOrMoreMultiplicityView(ConnectionView connectionView, boolean isStartPoint)
		{
			super(connectionView, isStartPoint);
		}

		@Override
		protected String getText()
		{
			return "*";
		}
	}

	private static class DecorationLayout
	{
		public ConnectionEdge connectionEdge;	
		public Point2D origin;
		
		public DecorationLayout(ConnectionEdge connectionEdge, Point2D origin)
		{
			this.connectionEdge = connectionEdge;
			this.origin = origin;
		}

		public void locate(Rectangle2D rectangle)
		{
			connectionEdge.locate(origin, rectangle);
		}			
	}
	
	private abstract static class ConnectionView extends SlaveView
	{
		protected TableView fromTable;
		protected TableView toTable;
		protected ConnectionEdge fromEdge;
		protected ConnectionEdge toEdge;
		private Path2D path;
		private DecorationLayout startDecorationLayout;
		private DecorationLayout endDecorationLayout;
			
		public ConnectionView(TableView fromTable, TableView toTable)
		{
			super(fromTable);
			this.fromTable = fromTable;
			this.toTable = toTable;
			selectConnectionEdges();
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener(){

				@Override
				public void propertyChange(PropertyChangeEvent evt)
				{
					selectConnectionEdges();
					
				}};
			fromTable.addPropertyChangeListener(BasicView.LOCATION, propertyChangeListener);
			toTable.addPropertyChangeListener(BasicView.LOCATION, propertyChangeListener);
			fromTable.connectionViews.add(this);
			toTable.connectionViews.add(this);
		}
		
		public TableView getOppositeTableView(ConnectionEdge connectionEdge)
		{
			if (connectionEdge == toEdge)
				return fromTable;
			else
				if (connectionEdge == fromEdge)
					return toTable;
			throw new IllegalArgumentException("The specified connection edge must be one of the connection view's connection edges");
		}
		
		/**
		 * Called whenever the connected table views are moved. The selection is done
		 * by connection the center of the two table views with a line and select
		 * the edge that is intersected by the line.
		 */
		private void selectConnectionEdges()
		{
			if (fromEdge != null)
				fromEdge.disconnect(this);
			if (toEdge != null)
				toEdge.disconnect(this);
								
			Line2D testLine = new Line2D.Double(fromTable.getCenter(), toTable.getCenter());
			fromEdge = fromTable.getIntersectingEdge(testLine);
			toEdge = toTable.getIntersectingEdge(testLine);
			if (fromEdge != null)
				fromEdge.connect(this);
			if (toEdge != null)
				toEdge.connect(this);
			createPath();
		}
			
		@Override
		public Shape getOpticalShape()
		{
			if (path == null)
				createPath();
			return path;
		}
		
		/**
		 *	fromEdge and toEdge can be null when connected table view are overlapping.
		 */
		private void createPath()
		{
			path = new Path2D.Float();
	
			if (fromEdge == null || toEdge == null)
				return;

			Line2D line1 = fromEdge.getVeryLongPerpendicularLine(this);
			Line2D line2 = toEdge.getVeryLongPerpendicularLine(this);
			
			if (line1.intersectsLine(line2))
			{
				// The lines are orthogonal
				Point2D.Double intersectionPoint = isHorizontal(line1) ? new Point2D.Double(line2.getX1(), line1.getY1()) : new Point2D.Double(line1.getX1(), line2.getY1());
				line1 = new Line2D.Double(line1.getP1(), intersectionPoint);
				line2 = new Line2D.Double(line2.getP1(), intersectionPoint);
			} else
				// The lines are parallel
			{
				line1 = fromEdge.getHalfwayPerpendicularLine(this, toEdge.getConnectionPointFor(this));
				line2 = toEdge.getHalfwayPerpendicularLine(this, fromEdge.getConnectionPointFor(this));
				elimateTinyParallelity(line1, line2);
//				Let the path do the connection instead				
//				Line2D line3 = new Line2D.Double(line1.getP2(), line2.getP2());
//				path.append(line3, false);
			}
			startDecorationLayout = new DecorationLayout(fromEdge, line1.getP1());
			endDecorationLayout = new DecorationLayout(toEdge, line2.getP1());

			line2.setLine(line2.getP2(), line2.getP1());  // Reverse direction		
			path.append(line1, false);
			path.append(line2, true);
		}
		
		@Override
		public void paint(Paintable g)
		{
			g.setColor(LINE_COLOR);
			g.setStroke(new BasicStroke(1.0f));
			g.draw(path);
		}

		
		private void elimateTinyParallelity(Line2D line1, Line2D line2)
		{
			double threshold = 15;
			double yd = line1.getY1() - line2.getY1();
			if (Math.abs(yd) < threshold)
			{
				double halfway = yd/2;
				line1.setLine(line1.getX1(), line1.getY1() - halfway, line1.getX2(), line1.getY2() - halfway);
				line2.setLine(line2.getX1(), line2.getY1() + halfway, line2.getX2(), line2.getY2() + halfway);
			}
			else
			{
				double xd = line1.getX1() - line2.getX1();
				if (Math.abs(xd) < threshold)
				{
					double halfway = xd/2;
					line1.setLine(line1.getX1() - halfway, line1.getY1(), line1.getX2() - halfway, line1.getY2());
					line2.setLine(line2.getX1() + halfway, line2.getY1(), line2.getX2() + halfway, line2.getY2());
				}
			}
		}
		
		private boolean isHorizontal(Line2D line)
		{
			return line.getY1() == line.getY2();
		}
				
		public DecorationLayout getStartDecorationLayout()
		{
			return startDecorationLayout;
		}

		public DecorationLayout getEndDecorationLayout()
		{
			return endDecorationLayout;
		}

	}
	
	private static class OneToManyConnectionView extends ConnectionView
	{
		
		public OneToManyConnectionView(SchemaGraphPaperView parentView, ForeignKey foreignKey)
		{
			super(parentView.getViewFor(foreignKey.getForeignTable()), parentView.getViewFor(foreignKey.getDefiningTable()));
			parentView.addChild(new ZeroOrMoreMultiplicityView(this, false));
			parentView.addChild(foreignKey.getForeignKeyColumn().isNullable() ? new ZeroOrOneMultiplicityView(this, true) : new ExactlyOneMultiplicityView(this, true));
		}
		
	}
	
	/**
	 * Represents an edge of a TableView where it is possible to
	 * connect a connection line. Several connection to same edge
	 * is distributed along the edge with equal distances.
	 *
	 */
	private static abstract class ConnectionEdge
	{
		/**
		 * Could not find a map that was able to sort according to vertical or horizontal
		 * position and the at the same time use object identity for the key object.
		 * Got strange result with a TreeMap that I gave a Comparator in the constructor.
		 * Forced to use a list instead.
		 */
		private class ConnectionMapEntry
		{
			ConnectionView connectionView;
			Point2D point;
			
			public ConnectionMapEntry(ConnectionView connectionView, Point2D point)
			{
				this.connectionView = connectionView;
				this.point = point;
			}
			
			@Override
			public boolean equals(Object obj)
			{
				if (obj == null)
					return false;
				else
				{
					ConnectionMapEntry connectionMapEntry = (ConnectionMapEntry)obj;
					return connectionMapEntry.connectionView.equals(connectionView);
				}
			}
			
		}
		
		protected TableView tableView;
		protected List<ConnectionMapEntry> connectionMap = new ArrayList<ConnectionMapEntry>();
				
//		protected Map<ConnectionView, Point2D> connectionMap = new HashMap<ConnectionView, Point2D>();
						
		public ConnectionEdge(TableView tableView)
		{
			super();
			this.tableView = tableView;
		}

		public Point2D getConnectionPointFor(ConnectionView connectionView)
		{
			for (ConnectionMapEntry connectionViewPoint : connectionMap)
			{
				if (connectionViewPoint.connectionView == connectionView)
					return connectionViewPoint.point;
			}
			return null;
		}
		
		public void connect(ConnectionView connectionView)
		{
			connectionMap.add(new ConnectionMapEntry(connectionView, null));
			distributeConnectionPoints();
		}
		
		public void disconnect(ConnectionView connectionView)
		{
			connectionMap.remove(new ConnectionMapEntry(connectionView, null));
			distributeConnectionPoints();
		}
		
		public abstract boolean isVertically();
		
		protected abstract Point2D getFirstPoint();
		
		protected abstract Point2D getSecondPoint();
		
		/**
		 * Locate the specified rectangle relative to specified origin
		 * in a way that is appropriate for decoration of connection lines
		 */
		public abstract void locate(Point2D origin, Rectangle2D rectangle);
			
		public Line2D getEdgeLine()
		{
			return new Line2D.Float(getFirstPoint(), getSecondPoint());
		}
		
		/**
		 * 
		 * @return a very long line that is 90 degrees to the edge and starting
		 * at the connection point for the specified connection view
		 */
		public Line2D getVeryLongPerpendicularLine(ConnectionView connectionView)
		{
			Point perpendicularDirection = getPerpendicularDirection();
			float xOffset = 10000 * perpendicularDirection.x;
			float yOffset = 10000 * perpendicularDirection.y;
			Point2D pt1 = getConnectionPointFor(connectionView);
			Point2D pt2 = new Point2D.Double(pt1.getX() + xOffset, pt1.getY() + yOffset);
			return new Line2D.Float(pt1, pt2);
		}
		
		/**
		 * We are only dealing with horizontal or vertical lines
		 */
		public Line2D getHalfwayPerpendicularLine(ConnectionView connectionView, Point2D destination)
		{
			double xOffset = isVertically() ? (destination.getX() - getFirstPoint().getX()) / 2.0 : 0;
			double yOffset = isVertically() ? 0 : (destination.getY() - getFirstPoint().getY()) / 2.0;
			
			Point2D pt1 = getConnectionPointFor(connectionView);
			Point2D pt2 = new Point2D.Double(pt1.getX() + xOffset, pt1.getY() + yOffset);
			return new Line2D.Float(pt1, pt2);
		}

		/**
		 * Returns a point that can be multiplied by a point
		 * on the edge to point out the direction for a perpendicular to
		 * the edge.<br>
		 * <br>
		 * <table border=1>
		 * <tr><th></th><th>X</th><th>Y</th></tr>
		 * <tr><td>North</td><td>0</td><td>-1</td></tr>
		 * <tr><td>South</td><td>0</td><td>1</td></tr>
		 * <tr><td>East</td><td>1</td><td>0</td></tr>
		 * <tr><td>West</td><td>-1</td><td>0</td></tr>
		 * </table>
		 */
		protected abstract Point getPerpendicularDirection();
				
		protected Point2D getCenter()
		{
			return new Point2D.Double((getFirstPoint().getX() + getSecondPoint().getX()) / 2.0, (getFirstPoint().getY() + getSecondPoint().getY()) / 2.0);
		}
		
		/**
		 * 
		 * @return the distance between the connection point on the edge
		 */
		protected double getConnectionPointInterval()
		{
			return getSecondPoint().distance(getFirstPoint()) / (connectionMap.size() + 1);
		}
		
		/**
		 * x interval or y interval is zero to control horizontal or
		 * vertical distribution of the connection points
		 */
		private void distributeConnectionPoints()
		{
			sortConnectionMap();
			Point2D pt = getFirstPoint();
			for (ConnectionMapEntry entry : connectionMap)
			{
					entry.point = new Point2D.Double(pt.getX() + getHorizontalInterval(), pt.getY() + getVerticalInterval());
					pt = entry.point;
			}
		}
		
		/**
		 * Sort the table view this edge is connecting to by location to avoid
		 * crossing connection lines.
		 */
		private void sortConnectionMap()
		{
			Collections.sort(connectionMap, new Comparator<ConnectionMapEntry>(){

				@Override
				public int compare(ConnectionMapEntry o1, ConnectionMapEntry o2)
				{
					return compareLocation(o1.connectionView.getOppositeTableView(ConnectionEdge.this), o2.connectionView.getOppositeTableView(ConnectionEdge.this));
				}
						
			});
		}
		
		/**
		 * Overridden to implement vertical or horizontal location comparison
		 */
		protected abstract int compareLocation(TableView tableView1, TableView tableView2);
		
		protected abstract double getHorizontalInterval();

		protected abstract double getVerticalInterval();
		
		protected Point2D getUpperLeftCorner()
		{
			return new Point2D.Float(tableView.getX(), tableView.getY());
		}
		
		protected Point2D getUpperRightCorner()
		{
			return new Point2D.Float(tableView.getRightBound(), tableView.getY());
		}

		protected Point2D getLowerLeftCorner()
		{
			return new Point2D.Float(tableView.getX(), tableView.getBottomBound());
		}
		
		protected Point2D getLowerRightCorner()
		{
			return new Point2D.Float(tableView.getRightBound(), tableView.getBottomBound());
		}
	}
	
	private static abstract class HorizontalConnectionEdge extends ConnectionEdge
	{
		public HorizontalConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected double getHorizontalInterval()
		{
			return getConnectionPointInterval();
		}
		
		@Override
		protected double getVerticalInterval()
		{
			return 0;
		}
		
		@Override
		public boolean isVertically()
		{
			return false;
		}
		
		@Override
		protected int compareLocation(TableView tableView1, TableView tableView2)
		{
			return (int) (tableView1.getX() - tableView2.getX());			
		}
	}
	
	private static class SouthConnectionEdge extends HorizontalConnectionEdge
	{
		public SouthConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected Point2D getFirstPoint()
		{
			return getLowerLeftCorner(); 
		}
		
		@Override
		protected Point2D getSecondPoint()
		{
			return getLowerRightCorner();
		}
		
		@Override
		protected Point getPerpendicularDirection()
		{
			return new Point(0, 1);
		}
		
		@Override
		public void locate(Point2D origin, Rectangle2D rectangle)
		{
			rectangle.setFrame(origin.getX(), origin.getY(), rectangle.getWidth(), rectangle.getHeight());
		}


	}

	private static class NorthConnectionEdge extends HorizontalConnectionEdge
	{
		public NorthConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected Point2D getFirstPoint()
		{
			return getUpperLeftCorner();
		}
		
		@Override
		protected Point2D getSecondPoint()
		{
			return getUpperRightCorner();
		}
		
		@Override
		protected Point getPerpendicularDirection()
		{
			return new Point(0, -1);
		}
		
		@Override
		public void locate(Point2D origin, Rectangle2D rectangle)
		{
			rectangle.setFrame(origin.getX(), origin.getY() -  rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
		}
	}
	
	private static abstract class VerticalConnectionEdge extends ConnectionEdge
	{
		public VerticalConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected double getHorizontalInterval()
		{
			return 0;
		}
		
		@Override
		protected double getVerticalInterval()
		{
			return getConnectionPointInterval();
		}
		
		@Override
		public boolean isVertically()
		{
			return true;
		}
		
		@Override
		protected int compareLocation(TableView tableView1, TableView tableView2)
		{
			return (int) (tableView1.getY() - tableView2.getY());			
		}
	}
	
	private static class WestConnectionEdge extends VerticalConnectionEdge
	{
		public WestConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected Point2D getFirstPoint()
		{
			return getUpperLeftCorner();
		}
		
		@Override
		protected Point2D getSecondPoint()
		{
			return getLowerLeftCorner();
		}
		
		@Override
		protected Point getPerpendicularDirection()
		{
			return new Point(-1, 0);
		}
		
		@Override
		public void locate(Point2D origin, Rectangle2D rectangle)
		{
			rectangle.setFrame(origin.getX() - rectangle.getWidth(), origin.getY()- rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
		}

	}

	private static class EastConnectionEdge extends VerticalConnectionEdge
	{
		public EastConnectionEdge(TableView tableView)
		{
			super(tableView);
		}

		@Override
		protected Point2D getFirstPoint()
		{
			return getUpperRightCorner();
		}
		
		@Override
		protected Point2D getSecondPoint()
		{
			return getLowerRightCorner();
		}
			
		@Override
		protected Point getPerpendicularDirection()
		{
			return new Point(1, 0);
		}
		
		@Override
		public void locate(Point2D origin, Rectangle2D rectangle)
		{
			rectangle.setFrame(origin.getX(), origin.getY() -  rectangle.getHeight(), rectangle.getWidth(), rectangle.getHeight());
		}

	}


}
