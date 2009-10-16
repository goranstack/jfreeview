package se.bluebrim.view.print;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;

import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.ZoomController;

/**
 * Show a print preview of a view and contains
 * menus for page set up and printing.
 * 
 * TODO: For implementing multi page support read:
 * <a href=http://java.sun.com/developer/onlineTraining/Programming/JDCBook/advprint.html> this</a>
 * 
 * @author G Stack
 *
 */
public class PrintPreviewFrame extends JFrame
{
	// Bound properties
	public static String PRINT_SERVICE_NAME =	"printServiceName";
	public static String PAPER_FORMAT_NAME =	"paperFormatName";
	
	// Constants
	private static double INCH = 25.4;

	private PropertyChangeSupport propertyChangeSupport;
	private ViewContext viewContext;
	private CompositeView reportView;
	private PrinterJob printerJob;
	private PageFormat pageFormat;
	private PaperOnDesktopView paperOnDesktopView;
	private String title;
	private String printServiceName;
	private String paperFormatName;
	private ViewPanel viewPanelToPrint;
	private ViewPanel columnHeaders;
	private ViewPanel rowHeaders;

	private JMenu viewMenu;
	private PropertyChangeListener rebuildReportListener;
	private ViewPanel previewPanel;


	/**
	 * The specified view panels must allow manipulation and should not be the same instances that is displayed on the screen.
	 * Previous version of this class made copies of specified view panels but this was a bad design since the copy method was
	 * unreliable and its often much more easy for the caller to create a new fresh instance instead of copying it here. 
	 */
	public PrintPreviewFrame(final ViewPanel viewPanelToPrint, final ViewPanel columnHeaders, final ViewPanel rowHeaders, String title, ViewContext viewContext)
	{
		propertyChangeSupport = new PropertyChangeSupport(this);
		this.viewContext = viewContext;
		this.viewContext.setScale(new Scale());
		
		previewPanel = new ViewPanel(null, this.viewContext);
		this.viewContext.setComponent(previewPanel);
		createPaperOnDesktopView();
		previewPanel.setRootView(paperOnDesktopView);
				
		createReportView(viewPanelToPrint, columnHeaders, rowHeaders, title);		

		// Needed to unregister listerners at dispose
		this.viewPanelToPrint = viewPanelToPrint;
		this.columnHeaders = columnHeaders;
		this.rowHeaders = rowHeaders;
		addListeners();
		
		this.title = title;

		initPageFormat();
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);	// Without this line the scrollbars shows even though there is nothing to scroll		
		scrollPane.setViewportView(previewPanel);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		paperOnDesktopView.setOwnerComponent(previewPanel);
		paperOnDesktopView.setPageFormat(getPageFormat());
		createMenus();
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		ZoomController zoomController = new ZoomController(previewPanel, scrollPane, toolBar, viewMenu);

		MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher(contentPane);
		previewPanel.addMouseListener(mouseEventDispatcher);
		previewPanel.addMouseMotionListener(mouseEventDispatcher);
		mouseEventDispatcher.addToolDispatcher(zoomController);
		updatePrintServiceName();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * If there is column headers and/or column header return a CompositeView with the column headare at the top
	 * and the row headers to the left and the content view in the middle. Otherwise return the content view.
	 */
	private AbstractParentView createReportBody(final ViewPanel contentPanel, final ViewPanel columnHeaders, final ViewPanel rowHeaders)
	{
		if (columnHeaders == null && rowHeaders == null)
		{
			return contentPanel.getRootView();
		}
		else
		{
			// Report body
			CompositeView reportBodyView = new CompositeView(viewContext);
			Layoutable columnHeadersView = columnHeaders != null ?(Layoutable)columnHeaders.getRootView() : null;
			Layoutable rowHeadersView = rowHeaders != null ?(Layoutable)rowHeaders.getRootView() : null;
			
			if (columnHeadersView != null)
				reportBodyView.addChild(columnHeadersView);
			if (rowHeadersView != null)
				reportBodyView.addChild(rowHeadersView);
			
			// The content panel is already a copy
			ParentView contentView = (ParentView)contentPanel.getRootView();
			viewContext.getSelectionManager().setRootView(contentView);
			reportBodyView.addChild(contentView);
			reportBodyView.setLayout(new ReportBodyLayout(columnHeadersView, contentView, rowHeadersView));
			return reportBodyView;

		}
	}
	
	private void createReportView(final ViewPanel contentPanel, final ViewPanel columnHeaders, final ViewPanel rowHeaders, String title)
	{
		AbstractParentView reportBodyView = createReportBody(contentPanel, columnHeaders, rowHeaders);
		reportView = new CompositeView(viewContext);
		ReportTilteView titleView = new ReportTilteView(viewContext, title, new DoubleInsets(5d, 0, 15d, 5d));
		ScaleToFitView scaleToFitView = new ScaleToFitView(viewContext, reportBodyView);
		// Due to a bug in ScaleToFitView we must paint the scaleToFitView as the last view. ( The reset of scale mess up for following views)
		reportView.addChild(scaleToFitView);
		reportView.addChild(titleView);
		reportView.setLayout(new ReportLayout(titleView, scaleToFitView));
		reportView.layoutTree();
		paperOnDesktopView.setContent(reportView);
	}
	
//	private void layoutReport()
//	{
//		reportView.invalidateLayout();
//		reportView.layout();
//	}
//	
//	public void doLayout()
//	{
//		super.doLayout();
//		layoutReport();
//	}

	/**
	 * Listen for property changes that forces us to rebuild the report
	 */
	private void addListeners()
	{
				
		rebuildReportListener = new PropertyChangeListener(){
		
					public void propertyChange(PropertyChangeEvent evt)
					{
						rebuildReport(viewPanelToPrint, columnHeaders, rowHeaders, title);
					}};
		viewPanelToPrint.addPropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);		
		columnHeaders.addPropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);		
		rowHeaders.addPropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);		
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
		DoubleInsets margins = getPageMarginsAsPixels();
		Paper paper = pageFormat.getPaper();
		paper.setImageableArea(margins.left, margins.top, paper.getWidth() - (margins.left + margins.right), paper.getHeight() - (margins.top + margins.bottom));
		pageFormat.setPaper(paper);
	}


	private void createPaperOnDesktopView()
	{
		paperOnDesktopView = new PaperOnDesktopView(viewContext, this, 1.05f, PaperOnDesktopView.LAYOUT_CONTENT_TO_FIT);
		paperOnDesktopView.setWidth(400);
		paperOnDesktopView.setHeight(400);
	}

	
	private void createMenus()
	{
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		createFileMenu(menuBar);
		viewMenu = new JMenu("Skala");
		menuBar.add(viewMenu);

	}

	protected void createFileMenu(JMenuBar menuBar)
	{	    
		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
	    // Page Format menu item
		JMenuItem item = new JMenuItem("Page Setup");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				pageFormatDialog();
			}
		});
		fileMenu.add(item);
	
	    // Print menu item
		item = new JMenuItem("Print");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printWindow();
			}
		});
		fileMenu.add(item);	
	}

	public void printWindow()
	{
		PrinterJob printerJob = getPrinterJob();
		printerJob.setJobName(title);	
	    printerJob.setPrintable(reportView, getPageFormat());
	    
	     try {
	    	 if (printerJob.printDialog())
	    	 	printerJob.print();
	    } catch (PrinterException e) {
	    	JOptionPane.showMessageDialog(this, "Kunde inte skriva ut beroende på\n" + e, "Fel vid utskrift", JOptionPane.ERROR_MESSAGE);
	    }
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
		paperOnDesktopView.setPageFormat(pageFormat);
		updatePrintServiceName();
	}
	
	public DoubleInsets getPageMarginsAsPixels()
	{
		DoubleInsets pageMargins = new DoubleInsets(10, 10, 10, 10);		// Millimeters
		// top, left bottom, right
		return new DoubleInsets(pixels(pageMargins.top), pixels(pageMargins.left), pixels(pageMargins.bottom), pixels(pageMargins.right));
	}
	
	private final double pixels(double millimeters)
	{
		return millimeters/INCH * 72.;
	}

	private void rebuildReport(final ViewPanel viewPanelToPrint, final ViewPanel columnHeaders, final ViewPanel rowHeaders, final String title)
	{
		createReportView(viewPanelToPrint, columnHeaders, rowHeaders, title);
		paperOnDesktopView.setContent(reportView);
		paperOnDesktopView.setPageFormat(getPageFormat());   // Gives the content a proper size. TODO: Fix in setContent-method instead
		previewPanel.updateCashedViewValues();
		repaint();
	}

	public String getPaperFormatName()
	{
		return paperFormatName;
	}

	private void setPaperFormatName(String paperFormatName)
	{
		String oldValue = this.paperFormatName;
		this.paperFormatName = paperFormatName;
		propertyChangeSupport.firePropertyChange(PAPER_FORMAT_NAME, oldValue, paperFormatName);
		System.out.println("PrintPreviewFrame.setPaperFormatName: " + paperFormatName);
	}

	public String getPrintServiceName()
	{
		return printServiceName;
	}

	private void updatePrintServiceName()
	{		
		String oldValue = printServiceName;
		printServiceName = getPrinterJob().getPrintService().getName();
		propertyChangeSupport.firePropertyChange(PRINT_SERVICE_NAME, oldValue, printServiceName);
//		System.out.println("PrintPreviewFrame.setPrintServiceName: " + printServiceName);
	}

	public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) 
	{
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener); 
	}
	
	public void dispose()
	{
		super.dispose();
		previewPanel.dispose();
		viewPanelToPrint.removePropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);		
		columnHeaders.removePropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);		
		rowHeaders.removePropertyChangeListener(ViewPanel.ROOT_VIEW, rebuildReportListener);
	}

	public void dumpViewTree()
	{
		paperOnDesktopView.dumpViewTree();
		
	}

}
