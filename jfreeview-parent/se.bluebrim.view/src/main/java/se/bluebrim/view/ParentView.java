package se.bluebrim.view;

import java.awt.geom.Point2D;
import java.util.List;

import se.bluebrim.view.model.PropertyChangeNotifier;


/**
 * Interface implemented by views that is a parent node in a view hierachy. 
 * A <code>ParentView</code> has a <code>LayoutManager</code> that manages 
 * how the children are layed out. Some <code>LayoutManager</code>'s also adjust
 * the size of the parent view. That's why we extends the Layoutable interface.
 * 
 * @author G Stack
 */
public interface ParentView extends View, Layoutable, ResizableContainer, PropertyChangeNotifier
{
	/**
	 * Used by LayoutManager to layout the children
	 */
	public List getChildren();  // <LayoutableView>
	
	public void addChild(View view);

	public void addChild(View view, Object layoutConstraints);

	public void removeChild(View view);
	
	public List getAllDescendents(); 	// <View>
		
	public void putOnTop(View child);
	
	public Point2D getLocationInRoot();
	
	/**
	 * Set the minimum size that include all children that is no part of any children
	 * is allowed to be outside this parent view. 
	 */
	public void adjustSizeToChildren();


			
}
