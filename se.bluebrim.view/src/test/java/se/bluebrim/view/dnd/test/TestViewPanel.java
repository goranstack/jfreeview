package se.bluebrim.view.dnd.test;

import java.awt.Dimension;

import se.bluebrim.view.impl.*;
import se.bluebrim.view.swing.ViewPanel;

public class TestViewPanel extends ViewPanel
{
   
	public TestViewPanel(AbstractParentView rootView, ViewContext viewContext)
	{
		super(rootView, viewContext);
	}
		
	public Dimension getMaximumSize()
	{
		return getPreferredSize();
	}
	
	public Dimension getMinimumSize()
	{
		return getPreferredSize();
	}
	
	public String toString()
	{
		return getClass().getName() + " " + getName();
	}	
}
