package se.bluebrim.view.test;

import java.awt.*;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.paint.Paintable;

/**
 * Container for a collection of NumberView's
 * 
 * @author G Stack
 */
public class NumberGroupView extends AbstractParentView implements Layoutable, Selectable, NamedView
{
	private String name;
		
	public NumberGroupView(ViewContext viewContext, Layout layoutManager)
	{
		super(viewContext, layoutManager);
		setWidth(800);
		setHeight(250);
	}
	
	protected void drawBeforeChildren(Paintable g)
	{
		Composite composite = g.getComposite();
		g.setStroke(new BasicStroke(getFrameThickness()));
		if (Paintable.RANDOM_COLORS.value)
			g.setRandomColor();
		else
			g.setColor(Color.PINK);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.fill(getBounds());
		g.setColor(Color.BLUE);
		g.draw(getBounds());
		g.setComposite(composite);
	}
	
	protected float getFrameThickness()
	{
		return isHighLighted() ? getSelectedFrameThickness() : getUnselectedFrameThickness();
	}
		
	public boolean isDropTarget()
	{
		return true;
	}

	
	public boolean acceptDrop(TransferableView transferable)
	{
		return transferable instanceof NumberView && !children.contains(transferable);
	}


	/**
	 * TODO: Create a commando that performs the transfer
	 */
	public void drop(TransferableView transferable, ActionModifiers modifiers)
	{
		if (transferable instanceof NumberView) 
		{
			NumberView numberView = (NumberView)transferable;
			((NumberGroupView)numberView.getParent()).children.remove(numberView);
			addChild(numberView);
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
	
}
