package se.bluebrim.view.zoom;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;

import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.tool.AbstractToolDispatcher;
import se.bluebrim.view.tool.Tool;
import se.bluebrim.view.tool.ToolBarOwner;


/**
 * Instances of this class are used to control zooming of a zoomable Component. The ZoomController adds zoom control components to the JToolBar that is supplied
 * by the zoomable component and calls setScale method of the zoomable component according to user gestures.<br>
 * The following components are added to the tool bar:
 * <ul>
 * <li>A magnification glass tool used for selecting a zoom area using rubber banding technique</li>
 * <li>A hand tool used for scrolling like Acrobat reader</li>
 * <li>A textfield showing the current zoom percent. A new value can be entered.</li>
 * <li>A popup menu containing various zoom factor and also an "Adjust to window" choice. When selected the zoomable component is scaled to fit in its
 * scrollpane parent.</li>
 * </ul>
 * The zoomable component is responsible for setting the desired scale factor in its paint method. For example like this:
 * 
 * <pre>
 * public void paint(Graphics g)
 * {
 * 	Graphics2D g2d = (Graphics2D) g;
 * 	g2d.scale(scaleFactor, scaleFactor);
 * 	drawScaledContent(g2d);
 * }
 * 
 * </pre>
 * 
 * Here is an example of how the ZoomController is intergated in DispositionFrame.
 * 
 * <pre>
 * Container contentPane = getContentPane();	// Get the content pane from the window
 * contentPane.setLayout(new BorderLayout());	// The toolbar works best with a BorderLayout where all sides are unoccupied
 * createViews(contentPane);					// The zoomable component is created in this method
 * 
 * JToolBar toolBar = new JToolBar();			// The tool bar is created outside the ZoomController to enable custom tools
 * ButtonGroup toolGroup = new ButtonGroup();	// The ButtonGroup must also be created in case custom tools should be included
 *
 * contentPane.add(toolBar, BorderLayout.NORTH);	// Add the tool bar to the top of content pane. Can be moved by the user.
 * new ZoomController(rootViewPanel, scrollPane, toolBar, toolGroup);
 * 
 * openWindow(false);
 * </pre>
 * 
 * @author G Stack
 */
public class ZoomController extends AbstractToolDispatcher implements  ToolBarOwner
{
	
	static final double MIN_SCALE = 0.01;
	static final double MAX_SCALE = 600;
	
	private Zoomable zoomable;
	private JScrollPane scrollPane;

	private JViewport viewport;
	private JTextField scaleTextField;
	private NumberFormat percentFormat;
	private boolean scaleToFitMode = true;
	private ScaleMenu scaleMenu;
	private ScaleMenu toolBarScaleMenu;
	private ButtonGroup toolGroup;
	private JToolBar toolBar;
	

	public ZoomController(Zoomable zoomable, JScrollPane scrollPane, JToolBar toolBar, JMenu scaleMenu)
	{
		super((ViewPanel)zoomable, false);
		percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setGroupingUsed(false);
		this.zoomable = zoomable;
		this.scrollPane = scrollPane;

		this.toolBar = toolBar;
		viewport = scrollPane.getViewport();
		if (toolBar != null)
			createTools();
		if (scaleMenu != null)
			this.scaleMenu = new ScaleMenu(this, scaleMenu);
		postInit();
	}
	
	/**
	 * Call this after the window is opened to avoid annoying extra repaints
	 * at up start caused by initial resized event from zoomable component.
	 * Did not help and is therefore a private method called from the constructor.
	 */
	private void postInit()
	{
		// Listen for scollPane resized events
		scrollPane.addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				scaleToFitParent();
			}
		});

		addResizeListenerToZoomableComponent();
		scaleToFitMode = true;
	}


	/**
	 * The zoom factor must change if we are in scaleToFitMode and the zoomableComponent is resized.
	 * For example if the zoomableComponent is displaying a A4 paper and the user changes to A3, then
	 * the zoom factor must decrease to anable the larger A3 to fit in the window.
	 */
	private void addResizeListenerToZoomableComponent()
	{
		((Component) zoomable).addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				scaleToFitParent();
			}
		});
	}
		
	
	
	private void createTools()
	{
		toolGroup = new ButtonGroup();
		toolBar.setBorderPainted(true);
		
		// Scroll hand tool is initial selected if none is selected at this point 
		Tool tool = new ScrollHandTool(this);
		if (toolGroup.getSelection() == null)
			tool.select();

		// Zoom tool
		tool = new ZoomTool(this);
		
		// Scale "combo box" is a textfield and a button with a popup menu
		toolBarScaleMenu = new ScaleMenu(this, new JPopupMenu());
	    createScaleTextField(toolBar);
		createScaleMenuButton(toolBar);	
	}
	
	
	/**
	 * Create a button in the toolbar with a small down arrow. Pops up
	 * the scale menu when pressed. The button is placed to the right
	 * of the scale text field. (Se Acrobat reader and others)
	 */
	private void createScaleMenuButton(JToolBar toolBar)
	{
		Icon icon = new ImageIcon(getClass().getResource("popupmenu.gif"));
		final int height = (int)toolBar.getPreferredSize().getHeight() - 2;
		final int width = icon.getIconWidth();
		final JButton scaleMenuButton = new JButton(){
	    	public Dimension getMaximumSize()
	    	{
	    		return new Dimension(width, height);
	    	}
	    	
	    	public Dimension getPreferredSize()
	    	{
	    		return new Dimension(width, height);
	    	}
	    	
	    	public Dimension getMinimumSize()
	    	{
	    		return new Dimension(width, height);
	    	}
	    	
	    };
		
		scaleMenuButton.setContentAreaFilled(true);
		scaleMenuButton.setMargin(new Insets(0, 0, 0, 0));
		scaleMenuButton.setIcon(icon); //$NON-NLS-1$
		toolBar.add(scaleMenuButton);
		
		scaleMenuButton.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent evt)
			{
				showPopupMenu(evt);
			}
			
			private void showPopupMenu(MouseEvent evt)
			{
				Rectangle bounds = scaleMenuButton.getBounds();
				toolBarScaleMenu.show(evt.getComponent(), 0, (int)bounds.getHeight());
			}
		});
	}

	/**
	 * Create a text field showing the current scale factor. The user can edit the value.
	 */
	private void createScaleTextField(JToolBar toolBar)
	{
		final int fieldWidth = 60; // TODO: Measure text for the current look&feel text field font
		final int fieldHeight = (int)toolBar.getPreferredSize().getHeight() - 2;
		scaleTextField = new JTextField(){
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
		scaleTextField.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e)
			{
				handleScaleFieldInput();
			}});
	    toolBar.add(scaleTextField);
	}
	
	
	public void setScaleToFitMode(boolean scaleToFitMode)
	{
		this.scaleToFitMode = scaleToFitMode;
		if (scaleToFitMode)
			scaleToFitParent();
	}
			
	/**
	 * The user has entered a percent value in the text field
	 */
	private void handleScaleFieldInput()
	{
        String text = scaleTextField.getText();
        if (!text.endsWith("%"))
        	text = text + "%";
        double value = 0;
        try {
			Number number = percentFormat.parse(text);
			value = number.doubleValue();
		} catch (ParseException e) {
			updateScaleField();
			return;
		}
		value = Math.max(MIN_SCALE, value);
		value = Math.min(MAX_SCALE, value);
		scaleToFitMode = false;
		setScaleFactor(new Scale(value));
		
	}
	
	public void setScaleFactorKeepCenter(Scale scale)
	{
		Scale oldScale = zoomable.getScale();
		double x = scrollPane.getBounds().getCenterX();
		double y = scrollPane.getBounds().getCenterY();
		int dx = (int)(x * scale.x/oldScale.x - x) + viewport.getViewPosition().x; 
		int dy = (int)(y * scale.y/oldScale.y - y) + viewport.getViewPosition().y; 
		setScaleAndScroll(scale, dx, dy);
	}

	public void setScaleFactor(Scale scale)
	{		
		zoomable.setScale(scale);
		scrollPane.revalidate();
		scrollPane.repaint();
		updateComponents();
	}
	
	
	public void setScaleAndScroll(Scale scale, int x, int y)
	{
		// Maximize zoom
		Scale maximizedScale = new Scale(Math.min(scale.x, ZoomController.MAX_SCALE), Math.min(scale.y, ZoomController.MAX_SCALE));
		Scale oldScale = zoomable.getScale();
		// TODO: Is this correct?
		if ((Math.abs(oldScale.x - maximizedScale.x) < ZoomController.MIN_SCALE) || 
				(Math.abs(oldScale.y - maximizedScale.y) < ZoomController.MIN_SCALE))
			return;
		scaleToFitMode = false;
		zoomable.setScale(maximizedScale);
		viewport.setViewSize(zoomable.getPreferredSize());
		// TODO: This algorithm is not perfect. The view position do not reflect the zoom rectangle exactly.
		viewport.setViewPosition(new Point((int)(x * maximizedScale.x/oldScale.x), (int)(y * maximizedScale.y/oldScale.y)));
		updateComponents();
		
	}
		
    private void scaleToFitParent()
    {
    	if (scaleToFitMode)
    	{   		
	    	Rectangle bounds = scrollPane.getBounds();
			Dimension unscaledSize = zoomable.getUnscaledSize();
			double horizontalScaleFactor = bounds.getWidth() / unscaledSize.getWidth();
			double verticalScaleFactor = bounds.getHeight() / unscaledSize.getHeight();
			setScaleFactor(new Scale(Math.min(horizontalScaleFactor, verticalScaleFactor)));
	    	}
    }
 

	private void updateComponents()
	{
		if (hasToolBar())
		{
			updateScaleField();
			toolBarScaleMenu.updateMenuState();
		}
		if (hasScaleMenu())
			scaleMenu.updateMenuState();
		
	}


	private boolean hasScaleMenu()
	{
		return scaleMenu != null;
	}

	private boolean hasToolBar()
	{
		return toolBarScaleMenu != null;
	}

	private void updateScaleField()
	{
		scaleTextField.setText(getScaleFactorAsString());
	}

	/**
	 * TODO: Handle separate x and y scale
	 */
	public String getScaleFactorAsString()
	{
		return percentFormat.format(zoomable.getScale().x);
	}

	public NumberFormat getPercentFormat()
	{
		return percentFormat;
	}

	public boolean isScaleToFitMode()
	{
		return scaleToFitMode;
	}

	public ButtonGroup getToolGroup()
	{
		return toolGroup;
	}

	public Zoomable getZoomable()
	{
		return zoomable;
	}

	public JToolBar getToolBar()
	{
		return toolBar;
	}

	public JScrollPane getScrollPane()
	{
		return scrollPane;
	}

	public void zoomInOneStep()
	{
		// TODO Auto-generated method stub		
	}
		
}
