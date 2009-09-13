package se.bluebrim.view.dnd.test;

import java.awt.*;
import java.util.List;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.test.NamedView;


public class TestViewContainer extends AbstractParentView implements Selectable, ViewDropTarget, NamedView
{
	
	private String name;

	public TestViewContainer(ViewContext viewContext)
	{
		super(viewContext);
	}
	
	protected void paintLayer(Paintable g)
	{
		g.setPaint(Color.WHITE);
		g.fill(getBounds());
		g.setPaint(Color.BLACK);		
		g.draw(getBounds());
		super.paintLayer(g);
	}
	
	public boolean acceptDrop(List transferables)
	{
		return true;
	}
	
	public boolean isDropTarget()
	{
		return true;
	}

	/**
	 * Since the transferble is a copy of the view in the drag source the removal of the view in
	 * the drag source is based on model identity instead.
	 */
	public void drop(TransferableView transferable, Point location, ActionModifiers modifiers, Component dialogParent)
	{
//		System.out.println("TestViewContainer.drop: DragSource=" + dragSource.getName() + " noOfChildren=" + dragSource.getChildren().size());
		transferable.changeParent(this);
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
