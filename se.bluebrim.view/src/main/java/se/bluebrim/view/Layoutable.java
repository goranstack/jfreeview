package se.bluebrim.view;

import java.awt.geom.Point2D;

import se.bluebrim.view.impl.ActionModifiers;

/**
 * Implemented by views that is layouted by a Layout
 * 
 * @author GStack
 *
 */
public interface Layoutable extends View
{
	public void setX(float x);
	public void setY(float y);
	public void setLocation(Point2D point);
	public void setBounds(float x, float y, float width, float height);
	
	public void setWidth(float width);
	public void setSilentWidth(float width);

	public void setHeight(float height);
	public void setSilentHeight(float width);
	
	public float getMinWidth();
	public float getMaxWidth();
	
	public float getMinHeight();
	public float getMaxHeight();
	
	public float getMinX();
	public float getMaxX();
	
	public float getMinY();
	public float getMaxY();

	public float getRightBound();	
	public float getBottomBound();
	
	public boolean lockVerticalMovement(ActionModifiers actionModifiers);
	public boolean lockHorizontalMovement(ActionModifiers actionModifiers);

	public float getPreferredHeight();
	public void timeScaleHasChanged();
	
	/**
	 * Make the the parents big enough to hold us
	 */
	public void expandParentIfNeeded();
		
}
