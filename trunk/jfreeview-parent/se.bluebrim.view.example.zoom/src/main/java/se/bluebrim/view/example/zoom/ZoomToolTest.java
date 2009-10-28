package se.bluebrim.view.example.zoom;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.example.NamedView;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.layout.VerticalDistributeLayout;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.ZoomController;

public class ZoomToolTest
{
	private JFrame window;
	private JScrollPane scrollPane;
	private ViewPanel viewPanel;
	private JMenu viewMenu;
	private ZoomController zoomController;

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Toolkit.getDefaultToolkit().setDynamicLayout(false);	// Works badly together with OptimalFitLayoutManager
		new ZoomToolTest().run();
	}

	private void run()
	{
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("Zoom Tool Test");
		window.setIconImage(new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage());

		ViewContext viewContext = new ViewContext(new Scale(), new Scale(), window);
		viewPanel = new ViewPanel(null, viewContext);
		viewContext.setComponent(viewPanel);

		ZoomToolTestView rootView = new ZoomToolTestView(viewContext, new VerticalDistributeLayout(10));
		rootView.setWidth(500);
		rootView.setHeight(500);
		viewPanel.setRootView(rootView);

		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportView(viewPanel);
		Container contentPane = window.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		JToolBar toolBar = new JToolBar();		
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		window.setLocation(100,100);
		createMenuBar();
		zoomController = new ZoomController(viewPanel, scrollPane, toolBar, viewMenu);

		MouseEventDispatcher mouseEventDispatcher = new MouseEventDispatcher(contentPane);
		viewPanel.addMouseListener(mouseEventDispatcher);
		viewPanel.addMouseMotionListener(mouseEventDispatcher);
		mouseEventDispatcher.addToolDispatcher(zoomController);

		window.pack();			
		window.setVisible(true);
	  viewPanel.updateCashedViewValues();
	   
	}


	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		window.setJMenuBar(menuBar);
		
		viewMenu = new JMenu("View");
		menuBar.add(viewMenu);

	}
	
						
	
	private class ZoomToolTestView extends AbstractParentView implements NamedView
	{

		public ZoomToolTestView(ViewContext viewContext, Layout layoutManager)
		{
			super(viewContext, layoutManager);		
		}

		public String getName()
		{
			return "RootView";
		}
		
		@Override
		protected void paintLayer(Paintable g)
		{
			g.draw(new Rectangle2D.Double(100.,100.,100.,100.));
		}
						
	}

}
