package se.bluebrim.view.example.movableviews;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import se.bluebrim.view.TestOptionMenuBuilder;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.example.TestRootView;
import se.bluebrim.view.impl.ResizeHandle;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.VerticalDistributeLayout;
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

public class MovableViewsExample
{
	private JFrame window;
	private JScrollPane scrollPane;
	private ViewPanel viewPanel;
	private JMenu viewMenu;
	private ZoomController zoomController;
	private JSlider detailLevelSlider;
	private JSlider zoomSlider;

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Toolkit.getDefaultToolkit().setDynamicLayout(false);	// Works badly together with OptimalFitLayoutManager
		new MovableViewsExample().run();
	}

	private void run()
	{
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("Movable Views Example");
		window.setIconImage(new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage());

		ViewContext viewContext = new ViewContext(new Scale(), new Scale(), window);
		viewPanel = new ViewPanel(null, viewContext);
		viewPanel.setToolTipText("Start tool tip");		// Needed to start off the tool tipping i think
		viewContext.setComponent(viewPanel);

		TestRootView rootView = new TestRootView(viewContext, new VerticalDistributeLayout(10));
		rootView.setWidth(800);
		rootView.setHeight(600);
		viewPanel.setRootView(rootView);

		SelectionManager selectionManager = new StandardSelectionManager(viewPanel);
		selectionManager.setRootView(rootView);
		viewContext.setSelectionManager(selectionManager);

		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportView(viewPanel);
		Container contentPane = window.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		JToolBar toolBar = new JToolBar();		
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		window.setSize(viewPanel.getUnscaledSize());
		window.setLocation(100,100);
		createMenuBar();
		zoomController = new ZoomController(viewPanel, scrollPane, toolBar, viewMenu);
		Tool selectionTool = new SelectionTool(zoomController, true);
		selectionTool.setTransactionManager(new MockTransactionManager());
		selectionTool.select();
		addSliders(toolBar);

		MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher(contentPane);
		viewPanel.addMouseListener(mouseEventDispatcher);
		viewPanel.addMouseMotionListener(mouseEventDispatcher);
		mouseEventDispatcher.addToolDispatcher(zoomController);
			
		window.setVisible(true);
	   viewPanel.updateCashedViewValues();
	   
	}

	/**
	 * Experimental zoom control slider and separate detail level slider
	 */
	private void addSliders(JToolBar toolBar)
	{
		createZoomSlider(toolBar);
		createDetailLevelSlider(toolBar);		
	}


	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);
		
		viewMenu = new JMenu("View");
		menuBar.add(viewMenu);

		JMenu menu = new JMenu("Test");
		menuBar.add(menu);
		createTestOptionMenuItems(menu);
		JMenuItem menuItem = new JMenuItem("Update cached view values");
		menu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				viewPanel.updateCashedViewValues();
			}
		});

	}
	
	private void createTestOptionMenuItems(JMenu menu)
	{
		TestOptionMenuBuilder testOptionMenuBuilder = new TestOptionMenuBuilder();
		testOptionMenuBuilder.addOption("Random colors", Paintable.RANDOM_COLORS);		
		testOptionMenuBuilder.addOption("Double click delay", Tool.DOUBLE_CLICK_DELAY);		
		testOptionMenuBuilder.addOption("Paint handles", ResizeHandle.PAINT_HANDLES);		
		testOptionMenuBuilder.addOption("Use MoveHandle", DragAndDropTool.USE_MOVE_HANDLE);
		testOptionMenuBuilder.addMenuItems(menu);
	}
	
	
	private void createZoomSlider(JToolBar toolBar)
	{
		final int fieldWidth = 100;
		final int fieldHeight = (int)toolBar.getPreferredSize().getHeight() - 2;

		zoomSlider = new JSlider(){
	    	public Dimension getMaximumSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    	public Dimension getPreferredSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    	public Dimension getMinimumSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    };
		toolBar.add(zoomSlider);
		zoomSlider.setMinimum(20);
		zoomSlider.setMaximum(1600);

		zoomSlider.addChangeListener(new ChangeListener() {
	        // This method is called whenever the slider's value is changed
	        public void stateChanged(ChangeEvent evt) {
	            JSlider slider = (JSlider)evt.getSource();
	            if (slider.getValueIsAdjusting()) 
	            {
	                int value = slider.getValue();
	        		Scale newScaleFactor = new Scale(value/100.0);
	        		zoomController.setScaleFactorKeepCenter(newScaleFactor);
	            }	    
	        }
	    });
	}

	private void createDetailLevelSlider(JToolBar toolBar)
	{
		final int fieldWidth = 100;
		final int fieldHeight = (int)toolBar.getPreferredSize().getHeight() - 2;

		detailLevelSlider = new JSlider(){
	    	public Dimension getMaximumSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    	public Dimension getPreferredSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    	public Dimension getMinimumSize()
	    	{
	    		return new Dimension(fieldWidth, fieldHeight);
	    	}
	    	
	    };
		toolBar.add(detailLevelSlider);
		detailLevelSlider.setMinimum(20);
		detailLevelSlider.setMaximum(1600);

		detailLevelSlider.addChangeListener(new ChangeListener() {
	        // This method is called whenever the slider's value is changed
	        public void stateChanged(ChangeEvent evt) {
	            JSlider slider = (JSlider)evt.getSource();
	            if (slider.getValueIsAdjusting()) 
	            {
	                int value = slider.getValue();
	        		setDetailLevel(new Scale(value/100.0));
	            }	    
	        }
	    });
	}
	
	
	public void setDetailLevel(Scale value)
	{
		viewPanel.setDetailLevel(value);
		scrollPane.revalidate();
		scrollPane.repaint();
		updateSliders();
	}

	
	private void updateSliders()
	{
		zoomSlider.setValue((int)Math.round(viewPanel.getScale().x * 100));
		detailLevelSlider.setValue((int)Math.round(viewPanel.getDetailLevel().x * 100));

	}

}
