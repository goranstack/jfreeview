package se.bluebrim.view.impl;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import se.bluebrim.view.Hittable;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.ResizeListener;
import se.bluebrim.view.Selectable;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.View;
import se.bluebrim.view.ViewLayer;
import se.bluebrim.view.geom.FloatDimension;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.layout.LayoutAdapter;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.tool.ToolGesture;

/**
 * Abstract super class to objects that has children and also are layoutable
 * 
 * @author GStack
 */
public abstract class AbstractParentView extends BasicView implements ParentView
{	
	protected List<View> children;
	protected boolean sizeControlledByChildren;
	private Layout layout;
	private Shape savedClip;
	private Map<ResizeListener, PropertyChangeListener> resizeListeners;

	public AbstractParentView(ViewContext viewContext)
	{
		this(viewContext, null);
	}

	public AbstractParentView(ViewContext viewContext, Layout layoutManager)
	{
		super(viewContext);
		sizeControlledByChildren = false;
		resizeListeners = new HashMap<ResizeListener, PropertyChangeListener>();
		setLayout(layoutManager);
		children = new ArrayList<View>();
		
		// Do layout if our size is changed
		addResizeListener(new ResizeListener(){

			public void resized(String dimension)
			{
				layout();
			}});
	}
	
	/**
	 * Create a new children list to avoid ConcurrentModificationException
	 */
	public void putOnTop(final View child)
	{
		List tempList = new ArrayList(children);
		if (!tempList.remove(child))
			throw new IllegalArgumentException("Specified child is unknown");
		tempList.add(0, child);
		children = tempList;
	}
	
	public void dispose()
	{
		super.dispose();
		disposeChildren();
	}

	public void timeScaleHasChanged()
	{
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			BasicView view = (BasicView)iter.next();
			view.timeScaleHasChanged();			
		}
	}
	
	protected void disposeChildren()
	{
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			View view = (View)iter.next();
			view.dispose();			
		}
	}

	/**
	 * Add all children to specified layer as well
	 */
	public void addToLayer(ViewLayer layer)
	{
		super.addToLayer(layer);
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			View view = (View)iter.next();
			view.addToLayer(layer);			
		}		
	}
	
	/**
	 * Remove all children from specified layer as well
	 */
	public void removeFromLayer(ViewLayer layer)
	{
		super.removeFromLayer(layer);
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			View view = (View)iter.next();
			view.removeFromLayer(layer);			
		}		
	}
	
	
	protected void paintLayer(Paintable g)
	{
//		System.out.println("AbstractParentView paintLayer " + toString());
		drawBeforeChildren(g);
		beforePaintChildren(g);
		paintChildren(g);
		afterPaintChildren(g);
		drawAfterChildren(g);
	}
	
	protected void drawBeforeChildren(Paintable g)
	{
		
	}
	
	protected void drawAfterChildren(Paintable g)
	{
		
	}
	/**
	 * The children's locations is relative to us
	 */
	protected void beforePaintChildren(Paintable g)
	{
//		savedClip = g.getClip();
		Shape opticalShape = getOpticalShape();
// TODO: Do we need clipping? Selection rectangle and drag feedback is clipped.
//		if (g.isPrinting())
//			if (opticalShape != null && !belongsToLayer(ViewLayer.DRAG_LAYER))
//				g.clip(opticalShape);
		g.translate(getX(), getY());
	}

	/**
	 * Reset translation to previous
	 */
	protected void afterPaintChildren(Paintable g)
	{
		g.translate(-getX(), -getY());
//		g.setClip(savedClip);
	}

	/**
	 * Children is painted in reverse order to make the
	 * first child be visible on top
	 */
	protected void paintChildren(Paintable g)
	{
		Rectangle clipBounds = g.getClipBounds();
		for (ListIterator iter = children.listIterator(children.size()); iter.hasPrevious();) 
		{
			View view = (View) iter.previous();
			if (clipBounds != null && g.getTarget() == Paintable.Target.Screen)
			{
				if (view.getOpticalShape().intersects(clipBounds.getX(), clipBounds.getY(), clipBounds.getWidth(), clipBounds.getHeight()))
					paintChild(g, view);
//				else
//					System.out.println(view.getClass().getName() + ". Skipped paint for view outside clipBounds. ClipBounds: " + 
//							clipBounds + " .View bounds: " + view.getOpticalShape().getBounds());
			} else
				paintChild(g, view);
		}
	}

	private void paintChild(Paintable g, View view)
	{
		if (view.belongsToLayer(g.getLayer()))
			view.paint(g);				
	}
	
	/**
	 * Used by the <code>LayoutManager</code> to layout the children
	 */
	public List getChildren()
	{
		return children;
	}
	
	public void addChild(View view)
	{
		addChild(view, null);
	}
	
	public void addChild(View view, Object layoutConstraints)
	{
		if (view == null)
			throw new IllegalArgumentException("null not allowed");
		if (view instanceof BasicView)
			((BasicView)view).setParent(this);
		if (layout instanceof LayoutAdapter && view instanceof Layoutable)
			((LayoutAdapter)layout).addLayoutable((Layoutable)view, layoutConstraints);
		children.add(view);
	}

	
	public void removeChild(View view)
	{
		if (view instanceof BasicView)
		{
			((BasicView)view).removeParentListeners();
			((BasicView)view).setParent(null);
		}
		if (layout instanceof LayoutAdapter && view instanceof Layoutable)
			((LayoutAdapter)layout).removeLayoutable((Layoutable)view);
		children.remove(view);		
	}
	
	public void removeAllChildren()
	{
		disposeChildren();
		children.clear();
	}
		
	protected void layout()
	{
		if (layout != null)
		{
			// System.out.println("AbstractParentView.layout " + getViewType());
			layout.layoutViews(this);
			updateViewTransformation(transform);
			if (sizeControlledByChildren)
				adjustSizeToChildren();
		}
	}
	
	/**
	 * Returns the smallest height that contains all children
	 */
	private float getChildrenHeight()
	{
		float height = 0;
		for (Iterator iter = children.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			height = Math.max(height, view.getBottomBound());
		}
		return height;		
	}

	/**
	 * Returns the smallest width that contains all children
	 */
	private float getChildrenWidth()
	{
		float width = 0;
		for (Iterator iter = children.iterator(); iter.hasNext();) 
		{
			Layoutable view = (Layoutable) iter.next();
			width = Math.max(width, view.getRightBound());
		}
		return width;		
	}

	public final void traverseDepthFirst(ViewVisitor visitor)
	{
		visitor.visitBeforeChildren(this);
		traverseChildrenDepthFirst(visitor);	
		visitor.visitAfterChildren(this);
		super.traverseDepthFirst(visitor);

	}

	protected void traverseChildrenDepthFirst(ViewVisitor visitor)
	{
		for (Iterator iter = children.iterator(); iter.hasNext();) 
		{
			AbstractView view = (AbstractView) iter.next();
			view.traverseDepthFirst(visitor);
		}
	}
	
	public final void traverseHeightFirst(ViewVisitor visitor)
	{
		super.traverseHeightFirst(visitor);
		visitor.visitBeforeChildren(this);
		traverseChildrenHeightFirst(visitor);		
		visitor.visitAfterChildren(this);
	}

	protected void traverseChildrenHeightFirst(ViewVisitor visitor)
	{
		for (Iterator iter = children.iterator(); iter.hasNext();) 
		{
			AbstractView view = (AbstractView) iter.next();
			view.traverseHeightFirst(visitor);
		}
	}


	/**
	 * Visit all views and and let the ToolGesture make a call back
	 * to the view. Each kind of ToolGesture has a cooresponding call back
	 * method in the Hittable interface. <br>
	 * Transform the gesture geometry to local view coordinate system
	 * along the way for easy hit testing at arrival to each view.
	 */
	public void hitChildren(final ToolGesture toolGesture, final ViewFilter filter)
	{
		traverseDepthFirst(new ViewVisitor()
		{
			public void visitBeforeChildren(ParentView parentView)
			{
				toolGesture.translateGestureGeometry(-parentView.getX(), -parentView.getY());
			}
			
			public void visitAfterChildren(ParentView parentView)
			{
				toolGesture.translateGestureGeometry(parentView.getX(), parentView.getY());
			}
				
			public void visit(Hittable view)
			{
				if (filter.accept(view))
					toolGesture.hitView(view);
			}
		});
	}
	
	public List getViewUnderMouse(final Point2D point)
	{
		return getViewsUnderMouse(point, View.ACCEPT_ALL_VIEW_FILTER);
	}
	
	/**
	 * Return the the views that contains the specified point. In case of overlapping
	 * views the view at lowest level in the hierarchy is returned first in the list. For example
	 * when a child is under the mouse the child is returned as the first item in the list and its 
	 * parent as the second item in the list. When siblings are overlapping the the order of the 
	 * siblings in the parent list is preserved in the returned list. <br>
	 * It's possible to skip views by specifying a filter.
	 */
	public List getViewsUnderMouse(Point2D point, final ViewFilter filter)
	{
		class ViewUnderMouseFinder extends ViewVisitor
		{
			Point2D hitPoint;
			List viewsUnderMouse = new ArrayList();
			
			public ViewUnderMouseFinder(Point2D point)
			{
				hitPoint = new Point2D.Double(point.getX(), point.getY());

			}
			public void visitBeforeChildren(ParentView parentView)
			{
				hitPoint.setLocation(hitPoint.getX() - parentView.getX(), hitPoint.getY() - parentView.getY());
			}

			public void visitAfterChildren(ParentView parentView)
			{
				hitPoint.setLocation(hitPoint.getX() + parentView.getX(), hitPoint.getY() + parentView.getY());
			}

			public void visit(AbstractView view)
			{
				if (view.getBounds().contains(hitPoint) && filter.accept(view))
				{
					viewsUnderMouse.add(view);
				}
			}
		};
		ViewUnderMouseFinder visitor = new ViewUnderMouseFinder(point);
		traverseDepthFirst(visitor);
		
		return visitor.viewsUnderMouse;

	}
	
	public void updateViewTransformation(final AffineTransform transform)
	{
//		System.out.println("AbstractParentView.updateViewTransformation");
		traverseDepthFirst(new ViewVisitor()
		{
			public void visitBeforeChildren(ParentView parentView)
			{
				transform.translate(parentView.getX(), parentView.getY());
			}

			public void visitAfterChildren(ParentView parentView)
			{
				transform.translate(-parentView.getX(), -parentView.getY());
			}

			public void visit(BasicView view)
			{
				view.setTransform((AffineTransform) transform.clone());
			}
		});

	}

	public List getAllDescendents()
	{
		final List allDescendents = new ArrayList();
		traverseDepthFirst(new ViewVisitor()
			{
				public void visit(AbstractView view)
				{
					allDescendents.add(view);
				}
			});
		return allDescendents;
	}
	
	public void adjustSizeToChildren()
	{
		adjustHeightToChildren();
		adjustWidthToChildren();		
	}
	
	public void adjustHeightToChildren()
	{
		setHeight(getChildrenHeight());		
	}
	
	public void adjustWidthToChildren()
	{
		setWidth(getChildrenWidth());
	}

	public boolean isChild(TransferableView transferable)
	{
		return children.contains(transferable);
	}

	public void setLayout(Layout layout)
	{
		if (layout instanceof LayoutAdapter)
			((LayoutAdapter)layout).setParentView(this);
		this.layout = layout;
	}
	
	public void addResizeListener(final ResizeListener resizeListener)
	{
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt)
			{
				resizeListener.resized(evt.getPropertyName());
				
			}};
		resizeListeners.put(resizeListener, propertyChangeListener);
		addPropertyChangeListener(BasicView.WIDTH, propertyChangeListener);
		addPropertyChangeListener(BasicView.HEIGHT, propertyChangeListener);
		
	}
	
	public void removeResizeListener(ResizeListener resizeListener)
	{
		PropertyChangeListener propertyChangeListener = (PropertyChangeListener)resizeListeners.get(resizeListener);
		removePropertyChangeListener(BasicView.WIDTH, propertyChangeListener);
		removePropertyChangeListener(BasicView.HEIGHT, propertyChangeListener);		
		resizeListeners.remove(resizeListener);
	}
	
	public FloatDimension getSize2D()
	{
		return new FloatDimension(getWidth(), getHeight());
	}
	
	public void layoutTree()
	{
		traverseDepthFirst(new ViewVisitor(){

			public void visit(AbstractView view)
			{
				if (view instanceof AbstractParentView)
				{
					AbstractParentView abstractParentView = ((AbstractParentView)view);
					abstractParentView.layout();
				}
			}});
		layout();
	}

	/**
	 * Skip children not in the children list for example handles and
	 * special subclasses of AbstractParentView who keeps children in
	 * separate instance variables.
	 */
	public BasicView getCopy(ViewContext viewContext)
	{
		AbstractParentView copy = (AbstractParentView)super.getCopy(viewContext);
		copy.children = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			BasicView view = (BasicView)iter.next();
			copy.children.add(view.getCopy(viewContext));			
		}
		return copy;
	}

	/**
	 * For debugging purposes
	 */
	public void dumpViewTree()
	{
		final String indentStep = "   ";
		traverseHeightFirst(new ViewVisitor(){
			String lineIndent = "";

			public void visitBeforeChildren(ParentView parentView)
			{
				lineIndent = lineIndent + indentStep;
			}
			
			public void visitAfterChildren(ParentView parentView)
			{
				lineIndent = lineIndent.substring(indentStep.length());
			}
			
			public void visit(BasicView view)
			{
				System.out.println(lineIndent + view.getDebugString());
			}});		
	}

	public void setSizeControlledByChildren(boolean sizeControlledByChildren)
	{
		this.sizeControlledByChildren = sizeControlledByChildren;
	}
	
	/**
	 * Traverses the view hierarchy using a visitor that looks for
	 * a Selectable with the specified id. Terminates visiting by
	 * throwing a StopVisitingException when the Selectable is found.
	 */
	public Selectable getSelectable(final Object id)
	{
		class StopVisitingException extends RuntimeException{}
		class FindSelectableVisitor extends ViewVisitor
		{
			Selectable foundSelectable;
			
			public void visit(AbstractView view)
			{
				if (view instanceof Selectable && ((Selectable)view).getId().equals(id))
				{
					foundSelectable = (Selectable)view;
					throw new StopVisitingException();
				}
			}			
		}

		FindSelectableVisitor visitor = new FindSelectableVisitor();
		try
		{
			traverseDepthFirst(visitor);
		}
		catch (StopVisitingException e)
		{
		}
		if (visitor.foundSelectable != null)
			return visitor.foundSelectable;
		else
			// Not very likely but we better test our self as well
			if (this instanceof Selectable && ((Selectable)this).getId().equals(id))
				return (Selectable)this;
			else
				return null;
	}

	protected Layout getLayout()
	{
		return layout;
	}

}
