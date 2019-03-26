package se.bluebrim.view.layout;

import java.awt.LayoutManager;

import javax.swing.BoxLayout;

import se.bluebrim.view.ParentView;

/**
 * A LayoutAdapter that makes it possible to use a BoxLayout
 * as a Layout in the view framework.
 * 
 * @author GStack
 * 
 */
public class BoxLayoutAdapter extends LayoutAdapter
{
	private int axis;
	
	public BoxLayoutAdapter(ParentView parentView, int axis)
	{
		this.axis = axis;
	}

	public void setParentView(ParentView parentView)
	{
		this.layoutTarget = new LayoutTarget(parentView);
		LayoutManager layoutManager = new BoxLayout(layoutTarget, axis);
		layoutTarget.setLayout(layoutManager);
	}


}