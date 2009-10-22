package se.bluebrim.view;

import se.bluebrim.view.geom.FloatDimension;

/**
 * Implemented by object that is able to notify listeners about resize
 * events.
 * 
 * @author G Stack
 *
 */
public interface ResizableContainer
{
	public void addResizeListener(ResizeListener resizeListener);
	public void removeResizeListener(ResizeListener resizeListener);
	
	/**
	 * Can't use getWidth and getHeight because the return type differ between
	 * implementing classes, and the name getSize is already in use as well.
	 */
	public FloatDimension getSize2D();
	
	public void layoutTree();
	
}
