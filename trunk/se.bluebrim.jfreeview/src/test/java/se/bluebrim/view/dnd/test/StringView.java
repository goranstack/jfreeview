package se.bluebrim.view.dnd.test;

import java.awt.*;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.test.NamedView;

public class StringView extends BasicView implements Selectable, TransferableView
{
	private String text;
	
	
	public StringView(ViewContext viewContext, String text, TestViewContainer parent)
	{
		super(viewContext);
		this.text = text;
	}

	protected void paintLayer(Paintable g)
	{
		g.setPaint(Color.ORANGE);
		g.fill(getBounds());
		g.setPaint(Color.BLACK);		
		g.setStroke(new BasicStroke(getFrameThickness()));
		g.draw(getBounds());
//		drawCenteredText(g, text);
		drawCenteredText(g, "x: " + (int)getX() + ", y: " + (int)getY() + " " + ((NamedView)getParent()).getName());
	}
		
	
	public Object getModel()
	{
		return text;
	}

	public String getText()
	{
		return text;
	}
	
	public TransferableView getTransferableCopy(ViewContext viewContext)
	{
		return super.getTransferableCopy(viewContext);
	}


}
