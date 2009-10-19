package se.bluebrim.view;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import se.bluebrim.view.paint.Paintable;



/**
 * Interface implemented by all views. 
 * 
 * @author G Stack
 */
public interface View
{
	public interface ViewFilter
	{
		 boolean accept(View view);
	}

	public static final ViewFilter ACCEPT_ALL_VIEW_FILTER = new ViewFilter()
	{
		public boolean accept(View view)
		{
			return true;
		}
	};
	
	public static final ViewFilter ONLY_DROP_TARGET_VIEW_FILTER = new ViewFilter()
	{
		public boolean accept(View view)
		{
			return view.isDropTarget();
		}
	};
	
	public static final ViewFilter ONLY_TRANSFERABLE_VIEW_FILTER = new ViewFilter()
	{
		public boolean accept(View view)
		{
			return view instanceof TransferableView;
		}
	};

	public static final ViewFilter ONLY_TOOLTIP_PROVIDER_VIEW_FILTER = new ViewFilter()
	{
		public boolean accept(View view)
		{
			return view instanceof ToolTipProvider;
		}
	};
	
	public float getX();
	
	public float getY();
	
	public float getHeight();

	public float getWidth();

	public Rectangle2D getBounds();
	
	public void updateViewFromModel();
	
	public AffineTransform getTransform();
	
	public void paint(Paintable g);
	
	/**
	 * Called when the container is resized. Not all view care about this but
	 * if for example the view adjust its geometry to the container this metod
	 * is overridden.
	 */
	public void containerResized(ResizableContainer container);
	
	public BufferedImage getVisualRepresentation();
	
	public Cursor getCursor();
	
	/**
	 * Return a rectangle that includes all graphics that is painted
	 * by the view. Could be larger than the geometrical bounds
	 * because the view paints outside the geometrical bounds.
	 */
	public Shape getOpticalShape();
	

	/**
	 * Return the geometrical bounds of the view transformed into the
	 * coordinate system of the component that owns the view hierachy.
	 * Useful for XOR drawing when tracking a drag.
	 */
	public Rectangle getBoundsRelativeComponent();

	public Point2D getLocation();

	/**
	 * Return the location of the view transformed into the
	 * coordinate system of the component that owns the view hierachy.
	 */
	public Point2D getLocationRelativeComponent();

	/**
	 * Return the location of the view relative to the root view.
	 */
	public Point2D getLocationInRoot();
	
	/**
	 * Return the optical bounds of the view transformed into the
	 * coordinate system of the component that owns the view hierachy.
	 * Useful as argument to the repaint method when only the view
	 * needs to be repainted. <br>
	 * If you change a state of a view that affects the optical bounds you
	 * must repaint the union of the optical bounds before and after the state change.
	 */
	public Shape getDirtyRegion();
	
	/**
	 * Views that register as listener should overide this method and unregister
	 * as listener.
	 */
	public void dispose();
	
	public void addToLayer(ViewLayer layer);
	public void removeFromLayer(ViewLayer layer);
	public boolean belongsToLayer(ViewLayer layer);

	public ParentView getParent();

	/**
	 * The view is placed on top of its siblings. Overlapping siblings is covered
	 * by a view that is place on top.
	 */
	public void putOnTop();

	/**
	 * For debugging purposes
	 */
	public String asString();
	
	// Roles
	public boolean isDropTarget();
	

}