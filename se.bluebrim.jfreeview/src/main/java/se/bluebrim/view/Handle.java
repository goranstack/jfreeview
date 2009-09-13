package se.bluebrim.view;

import java.awt.geom.*;
import java.util.List;

import se.bluebrim.view.impl.ActionModifiers;
import se.bluebrim.view.paint.*;

/**
 * A <strong>Handle</strong> is special kind of view that is used for manipulating
 * an aspect of an other view. The concept of handles is from HotDraw a framework
 * original developed at Tektronix by Kent Beck and Ward Cunningham.<br>
 * Here we only use handles to manipulating size and position of views. A Layoutable
 * view should be able to provide a set of handles, one for each side. When the
 * user drags one of the handles the view is changed by the handle.
 *  
 * @author Görans Stäck
 *
 */

public interface Handle extends View
{

	/**
	 * Coordinates relative view panel
	 */
	public void moveTo(Point2D point);
	
	/**
	 * 
	 * @return The view that should be repainted after each move. If more than one view is affected
	 * you can override this method and return the parent view to affected views. That's for example how
	 * Phase views is handled.
	 */
	public View getTargetView();

	/**
	 * 
	 * @return The rectangle that should be repainted after each move. If more than one view affected
	 * you should create an union of the dirty regions of the affected views
	 */
	public Rectangle2D getRepaintRegion();
	
	/**
	 * Overides by handles that manipulates an aspect of a view that should be
	 * painted during the drag. For example a handle is manipulating the start time
	 * of an allocationview. During the drag the starttime should be drawn to give the
	 * user maximum feedback. The handle use a callback to the view to perform the painting.
	 * 
	 */
	public void paintFeedback(Paintable paintable);

	public void startMoving();
	
	public void stopMoving(ActionModifiers modifiers);

	public PaintableHints getPaintabeHints();

	/**
	 * Return a list of views that is used for painting visual feedback during
	 * handle manipulation. Which feedback views the list contains is dependent
	 * by the combination of handle and target view. This is accomplished by 
	 * double dispathing where the target view ask the handle for feedback views
	 * and the handle calls a specific getFeedbackViews method of the target view.
	 * For example a HorizontalResizeHandle calls the getHorizontalResizeFeedbackViews
	 * of the target view. 
	 */
	public List getFeedbackViews();
}
