package se.bluebrim.view.example.svg;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.geom.Dimension2D;
import java.net.MalformedURLException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import se.bluebrim.desktop.graphical.SvgView;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.View;
import se.bluebrim.view.dnd.MouseEventDispatcher;
import se.bluebrim.view.example.svg.resource.SVGSampleProvider;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.AbstractLayout;
import se.bluebrim.view.swing.ViewPanel;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.ZoomController;

/**
 * Desktop application with one single SVGView browsing a series of SVG-files
 * 
 * @author Goran Stack
 * 
 */
public class SvgViewExample
{
	private JFrame window;
	private JScrollPane scrollPane;
	private ViewPanel viewPanel;
	private JMenu viewMenu;
	private ZoomController zoomController;
	private SVGSampleProvider svgSamples;
	private SvgView svgView;

	public static void main(String[] args) throws MalformedURLException
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		Toolkit.getDefaultToolkit().setDynamicLayout(false); // Works badly
		// together with
		// OptimalFitLayoutManager
		new SvgViewExample().run();
	}

	private void run() throws MalformedURLException
	{
		svgSamples = new SVGSampleProvider();
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("SVGView Example");
		window.setIconImage(new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage());

		ViewContext viewContext = new ViewContext(new Scale(), new Scale(), window);
		viewPanel = new ViewPanel(null, viewContext);
		viewContext.setComponent(viewPanel);

		CheckerBoardView rootView = new CheckerBoardView(viewContext, new CenterLayout());
		rootView.setWidth(1000);
		rootView.setHeight(800);
		viewPanel.setRootView(rootView);
		Container contentPane = window.getContentPane();

		Component originatorBar = svgSamples.createOriginatorBar();
		svgView = new SvgView(viewContext, svgSamples.next().getResource(), null);
		rootView.addChild(svgView);
		rootView.layoutTree();

		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setViewportView(viewPanel);
		contentPane.setLayout(new BorderLayout());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		JToolBar toolBar = createToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(originatorBar, BorderLayout.SOUTH);

		window.setLocation(100, 100);
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
	
	private JToolBar createToolBar()
	{
		JToolBar toolBar = svgSamples.createNavigatorToolbar(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SvgViewExample.this.displayNextSample();
			}
		});
		return toolBar;
	}


	protected void displayNextSample() 
	{
		svgView.setImage(svgSamples.next().getResource());
		viewPanel.repaint();
	}


	/**
	 * Hard wired to layout one single view
	 */
	private class CenterLayout extends AbstractLayout
	{
		@Override
		public Dimension2D getMinimumLayoutSize(ParentView container)
		{
			Dimension2D dim = new Dimension();
			View view = (View) container.getChildren().get(0);
			dim.setSize(view.getWidth(), view.getHeight());
			return dim;
		}

		@Override
		public void layoutViews(ParentView container)
		{
			if (container.getChildren().size() > 0) {
				Layoutable layoutable = (Layoutable) container.getChildren().get(0);
				layoutable.setX((container.getWidth() - layoutable.getWidth()) / 2);
				layoutable.setY((container.getHeight() - layoutable.getHeight()) / 2);
			}
		}

	}

}
