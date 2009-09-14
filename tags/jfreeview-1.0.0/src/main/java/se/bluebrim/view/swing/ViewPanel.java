package se.bluebrim.view.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import se.bluebrim.view.ResizableContainer;
import se.bluebrim.view.ResizeListener;
import se.bluebrim.view.Selectable;
import se.bluebrim.view.ToolTipProvider;
import se.bluebrim.view.View;
import se.bluebrim.view.View.ViewFilter;
import se.bluebrim.view.geom.FloatDimension;
import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.impl.ViewVisitor;
import se.bluebrim.view.paint.Graphics2DWrapper;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.paint.PaintableHints;
import se.bluebrim.view.select.SelectableLookup;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.tool.ToolGesture;
import se.bluebrim.view.value.BooleanValue;
import se.bluebrim.view.zoom.Scale;
import se.bluebrim.view.zoom.Zoomable;



/**
/**
 * A <code>ViewPanel</code> is a Swing component that holds a view hierarchy. Paint and layout calls 
 * among others are delegated to the root view in the view hierarchy. <br>
 * Zoom capability is achieved by changing the scale of the AffineTransform in the Graphics 
 * that is passed in the paint call before passing it further to the root view. <br>
 * Preferred size is calculated by applying the scale factor on the size of the root view.
 * That will trigger the scrollbars when zooming in case this component is placed in a JScrollPane.<br>
 * A <code>ViewPanel</code> also has methods used by tools when manipulating views i the view hierarchy.
 * 
 * @author GStack
 *
 */
public class ViewPanel extends JPanel implements Zoomable, ResizableContainer, SelectableLookup
{
	// Bounded properties
	public static final String ROOT_VIEW = "rootView";
	
	public static final BooleanValue PAINT_DIRTY_REGION = new BooleanValue(false);		// For debugging
	
	protected AbstractParentView rootView;
	protected ViewContext viewContext;
	private PaintableHints paintableHints;
	private Map<ResizeListener, ComponentAdapter> resizeListeners;
	private PropertyChangeListener scaleChangeListener;

	public ViewPanel(AbstractParentView rootView, ViewContext viewContext)
	{
		// Most layout managers won't work properly without an initial non zero size.
//		if (rootView.getWidth() <= 0 || rootView.getHeight() <= 0)
//			throw new IllegalArgumentException("rootView must have a non zero size");
		this.rootView = rootView;
		this.viewContext = viewContext;
		doLayout();
		
		resizeListeners = new HashMap<ResizeListener, ComponentAdapter>();		
		createListeners(viewContext);
	}
	

	private void createListeners(ViewContext viewContext)
	{
		// Listen for size changes on our self's
		addResizeListener(new ResizeListener()
		{
			public void resized(String dimension)
			{
				if (ViewPanel.this.rootView != null)
					updateViewTransformation(ViewPanel.this.rootView, ViewPanel.this.viewContext);
			}
		});
		scaleChangeListener = new PropertyChangeListener(){
		
					public void propertyChange(PropertyChangeEvent evt)
					{
						updateCashedViewValues();
						repaint();
					}};
		viewContext.addPropertyChangeListener(ViewContext.SCALE, scaleChangeListener);		
	}
	
	public void dispose()
	{
		viewContext.removePropertyChangeListener(ViewContext.SCALE, scaleChangeListener);
		if (rootView != null)
			rootView.dispose();		
	}
		
	/**
	 * Public for testing purposes
	 *
	 */
	public void updateCashedViewValues()
	{
		updateViewTransformation(rootView, viewContext);
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Paintable g2dw = new Graphics2DWrapper(g2d, Paintable.Target.Screen, viewContext);
		g2dw.setPaintableHints(paintableHints);
		if (rootView != null)
			rootView.paint(g2dw);
		if (PAINT_DIRTY_REGION.value)
			paintDirtyRegions(g2dw);
	}

	/**
	 * For debugging purposes. Check that dirty region calculation is correct
	 */
	private void paintDirtyRegions(final Paintable paintable)
	{
		paintable.setToScale(1, 1);		// Dirty region are already scaled
		rootView.traverseDepthFirst(new ViewVisitor()
		{
			public void visit(BasicView view)
			{
				if (view instanceof AbstractParentView)
					return;
				paintable.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
				paintable.setColor(Color.RED);
				paintable.fill(view.getDirtyRegion());
			}
		});
	}

	public Scale getScale()
	{
		return viewContext.getScale();
	}


	public void setScale(Scale scale)
	{
		viewContext.setScale(scale);
		viewContext.setDetailLevel(scale);	// TODO: Experimental synchronizing of these values
	}

	/**
	 * A preferred size larger than the scrollpane triggers scrolling.
	 * It's important to truncate preferred size calculation instead of round. Otherwise
	 * the scrollbars shows up when we don't want them.
	 */
	public Dimension getPreferredSize()
	{
		if (rootView != null)
		{
			int preferredWidth = (int)(rootView.getWidth() * viewContext.getScale().x);
			int preferredHeight = (int)(rootView.getHeight() * viewContext.getScale().y);
			return new Dimension(preferredWidth, preferredHeight);
		}
		else
			return super.getPreferredSize();
	}

		
	public void hit(ToolGesture toolGesture)
	{
		hit(toolGesture, View.ACCEPT_ALL_VIEW_FILTER);
	}

	public void hit(ToolGesture toolGesture, ViewFilter filter)
	{
		toolGesture.scaleGestureGeometry(viewContext.getScale().createInverted());
		rootView.hitChildren(toolGesture, filter);
	}


	public List getViewsUnderMouse(Point point, View.ViewFilter filter)
	{
		Scale invertedScale = viewContext.getScale().createInverted();
		if (rootView != null)
			return rootView.getViewsUnderMouse(new Point2D.Double(point.x * invertedScale.x, point.y * invertedScale.y), filter);
		else
			return new ArrayList();
	}
		
	private final void updateViewTransformation(AbstractParentView rootView, ViewContext viewContext)
	{
		rootView.updateViewTransformation(viewContext.getTransform());
	}

	public void paintImmediately()
	{
		paintImmediately(getBounds());
	}

	public SelectionManager getSelectionManager()
	{
		return viewContext.getSelectionManager();
	}


	public void setDetailLevel(Scale detailLevel)
	{
		viewContext.setDetailLevel(detailLevel);
	}

	public Scale getDetailLevel()
	{
		return viewContext.getDetailLevel();
	}
	
	public void setViewContext(ViewContext viewContext)
	{
		this.viewContext = viewContext;
	}

	public void setRootView(AbstractParentView rootView)
	{
		AbstractParentView oldValue = this.rootView;
		if (oldValue != null)
			oldValue.dispose();
		this.rootView = rootView;
		rootView.setContainer(this);
		setSize((int)rootView.getWidth(), (int)rootView.getHeight());
		updateViewTransformation(rootView, viewContext);
		firePropertyChange(ROOT_VIEW, oldValue, rootView);
	}
	
	public ViewContext getViewContext()
	{
		return viewContext;
	}
	
	public String getToolTipText(MouseEvent event) 
	{
		List views = getViewsUnderMouse(event.getPoint(), View.ONLY_TOOLTIP_PROVIDER_VIEW_FILTER);
		return views.size() > 0 ? ((ToolTipProvider)views.get(0)).getToolTipText(): null;
	}
	
	
	public Dimension getUnscaledSize()
	{
		return new Dimension((int)rootView.getWidth(), (int)rootView.getHeight());
	}

	public AbstractParentView getRootView()
	{
		return rootView;
	}

	public void addResizeListener(final ResizeListener resizeListener)
	{
		ComponentAdapter componentAdapter = new ComponentAdapter(){

			public void componentResized(ComponentEvent e)
			{
				resizeListener.resized(null);
			}};
		resizeListeners.put(resizeListener, componentAdapter);
		addComponentListener(componentAdapter);		
	}
	
	public void removeResizeListener(ResizeListener resizeListener)
	{
		ComponentAdapter componentAdapter = (ComponentAdapter)resizeListeners.get(resizeListener);
		resizeListeners.remove(resizeListener);
		removeComponentListener(componentAdapter);
		resizeListeners.remove(resizeListener);
	}
	
	public FloatDimension getSize2D()
	{
		return new FloatDimension(getWidth(), getHeight());
	}
	
	public void layoutTree()
	{
		rootView.layoutTree();
		updateViewTransformation(rootView, viewContext);
	}
	
	/**
	 * For debugging purposes
	 */
	public void dumpViewTree()
	{
		rootView.dumpViewTree();
	}
	
	/*
	 * For debugging purposes
	 */
	public int getNoOfListeners()
	{
		return resizeListeners.size();
	}

	public void setPaintableHints(PaintableHints paintableHints)
	{
		this.paintableHints = paintableHints;
	}


	public Selectable getSelectable(Object id)
	{
		return rootView.getSelectable(id);
	}


	/**
	 * Subclasses override this to adjust the root view to the specified
	 * aspect ratio (width/height). Views that has a time scale has the capability
	 * to adjust its aspect ration by changing the time scale. By doing this the view 
	 * will make a better fit to a certain paper format at printing.
	 */
	public void adjustToAspectRatio(double aspectRatio)
	{
		
	}


	
}
