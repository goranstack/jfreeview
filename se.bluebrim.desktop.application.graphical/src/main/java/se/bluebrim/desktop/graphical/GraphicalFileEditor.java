package se.bluebrim.desktop.graphical;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import nu.esox.gui.aspect.EnablePredicateAdapter;
import se.bluebrim.crud.client.DirtyPredicateProvider;
import se.bluebrim.crud.client.command.DefaultAction;
import se.bluebrim.crud.client.command.DefaultForegroundCommand;
import se.bluebrim.crud.esox.DirtyPredicateModel;
import se.bluebrim.desktop.DesktopApp;
import se.bluebrim.desktop.FileEditor;
import se.bluebrim.view.DesktopView;
import se.bluebrim.view.PaperView;
import se.bluebrim.view.TestOptionMenuBuilder;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.select.StandardSelectionManager;
import se.bluebrim.view.tool.DragAndDropTool;
import se.bluebrim.view.tool.SelectionTool;
import se.bluebrim.view.tool.Tool;
import se.bluebrim.view.transaction.MockTransactionManager;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.ZoomController;

/**
 * Abstract super class to file editors that is editing a content that is
 * printable and meaningful to save as PDF. For now only landscape A3 is
 * handled. <br>
 * Have a look at JFDraw: http://www.jfimagine.com/products.html
 * 
 * @author GStack
 */
public abstract class GraphicalFileEditor extends FileEditor
{
	protected static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private JMenu scaleMenu;
	protected DirtyPredicateViewPanel viewPanel;
	private PrinterJob printerJob;
	private PageFormat pageFormat;
	private JFileChooser pdfFileChooser;
	private JFileChooser svgFileChooser;
	protected PaperView paperView;
	protected ViewContext viewContext;

	public GraphicalFileEditor(DirtyPredicateModel model, DesktopApp desktopApp)
	{
		super(model, desktopApp);
		scaleMenu = new JMenu("View");
		initPageFormat();
	}

	private File createFile(String fileExtension)
	{
		if (file != null)
		{
			String[] parts = file.getName().split("[.]");
			String name = parts.length > 0 ? parts[0] : file.getName();
			return new File(file.getParentFile(), name + "." + fileExtension);
		} else
			return new File(getFileContentName() + "." + fileExtension);
	}


	@Override
	protected void layoutWindow()
	{
		window.setSize(viewPanel.getUnscaledSize());
		centerOnScreen(window);
	}

	private static void centerOnScreen(Window window)
	{
		window.setBounds(centerOnScreen(window.getSize()));
	}

	private static Rectangle centerOnScreen(Dimension size)
	{
		return centerOnScreen(size.width, size.height);
	}

	/**
	 * Return a rectangle of the given width and height so that it is centered on
	 * and cropped within the <em>usable</em> part of the screen.
	 * 
	 */
	private static Rectangle centerOnScreen(int width, int height)
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = ge.getCenterPoint();
		Rectangle rect = new Rectangle(center.x - (width / 2), center.y - (height / 2), width, height);
		return rect.intersection(ge.getMaximumWindowBounds());
	}

	@Override
	protected void createWindowContent(JFrame frame)
	{
		viewContext = new ViewContext(new Scale(), new Scale(), frame);
		DesktopView desktopView = new DesktopView(viewContext);
		paperView = new PaperView(viewContext);
		createViews();
		desktopView.setPaperView(paperView);

		viewPanel = new DirtyPredicateViewPanel(desktopView, viewContext);
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
	}

	protected abstract void createViews();
	
	@Override
	public JComponent getTumbnailSource()
	{
		return paperView.getTumbnailComponent();
	}

	@Override
	protected void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);
		createFileMenu(menuBar);
		menuBar.add(scaleMenu);		
		JMenu menu = new JMenu("Test");
		menuBar.add(menu);
		createTestOptionMenuItems(menu);
	}
	
	private void createTestOptionMenuItems(JMenu menu)
	{
		TestOptionMenuBuilder testOptionMenuBuilder = new TestOptionMenuBuilder();
		testOptionMenuBuilder.addOption("Mark dirty regions", Paintable.RANDOM_COLORS);		
		testOptionMenuBuilder.addOption("Use MoveHandle", DragAndDropTool.USE_MOVE_HANDLE);
		testOptionMenuBuilder.addMenuItems(menu);
	}

	protected void setDirty()
	{
		viewPanel.setDirty();
	}

	protected abstract void createFileMenu(JMenuBar menuBar);

	@Override
	protected DirtyPredicateProvider getDirtyPredicateProvider()
	{
		if (viewPanel == null)
			throw new RuntimeException("createWindowContent must be called first");
		return viewPanel;
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
		// paperView.setPageFormat(pageFormat);
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

	private void printWindow(Printable erDiagram) throws PrinterException
	{
		PrinterJob printerJob = getPrinterJob();
		printerJob.setJobName(getPrinterJobName());
		printerJob.setPrintable(erDiagram, getPageFormat());
		if (printerJob.printDialog())
			printerJob.print();
	}

	protected abstract String getPrinterJobName();

	protected class PageSetupAction extends DefaultAction
	{
		public PageSetupAction()
		{
			super("Page Setup...");
			// setSmallIcon(new ImageIcon(DesktopApp.class.getResource("xxxx.png")));
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			pageFormatDialog();
		}
	}

	protected class PrintAction extends DefaultAction
	{
		public PrintAction()
		{
			super("Print...");
			setSmallIcon(new ImageIcon(PrintAction.class.getResource("print.gif")));
			new EnablePredicateAdapter(null, null, this, null, hasModelPredicate);
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			printWindow(paperView);
		}
	}

	protected class SaveAsPdfAction extends DefaultAction
	{
		public SaveAsPdfAction()
		{
			super("Save as PDF...");
			setSmallIcon(new ImageIcon(SaveAsPdfAction.class.getResource("pdf.png")));
			new EnablePredicateAdapter(null, null, this, null, hasModelPredicate);
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			if (pdfFileChooser == null)
				pdfFileChooser = new JFileChooser();
			pdfFileChooser.setSelectedFile(createFile("pdf"));
			if (pdfFileChooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION)
				return;

			File outFile = pdfFileChooser.getSelectedFile();
			if (DefaultForegroundCommand.userConfirmsOverwriteIfExists(outFile, window))			
				paperView.saveAsPdf(outFile, window);
		}
	}
	
	protected class SaveAsSvgAction extends DefaultAction
	{
		public SaveAsSvgAction()
		{
			super("Save as Svg...");
			setSmallIcon(new ImageIcon(SaveAsSvgAction.class.getResource("svg.png")));
			new EnablePredicateAdapter(null, null, this, null, hasModelPredicate);
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			if (svgFileChooser == null)
				svgFileChooser = new JFileChooser();
			svgFileChooser.setSelectedFile(createFile("svg"));
			if (svgFileChooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION)
				return;

			File outFile = svgFileChooser.getSelectedFile();
			if (DefaultForegroundCommand.userConfirmsOverwriteIfExists(outFile, window))			
				paperView.saveAsSvg(outFile);
		}
	}
	
	protected class SaveAsPngAction extends DefaultAction
	{
		public SaveAsPngAction()
		{
			super("Save as Png...");
			new EnablePredicateAdapter(null, null, this, null, hasModelPredicate);
		}

		@Override
		protected void execute(ActionEvent evt) throws Exception
		{
			if (svgFileChooser == null)
				svgFileChooser = new JFileChooser();
			svgFileChooser.setSelectedFile(createFile("png"));
			if (svgFileChooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION)
				return;

			File outFile = svgFileChooser.getSelectedFile();
			if (DefaultForegroundCommand.userConfirmsOverwriteIfExists(outFile, window))			
				paperView.saveAsPng(outFile);
		}
	}



}
