package se.bluebrim.view.impl;

import se.bluebrim.view.*;

/**
 * Superclass to objects that are used for traversing the view hiearchy
 * using the visit methods.
 * 
 * @author G Stack
 */
public abstract class ViewVisitor
{

	public void visitBeforeChildren(ParentView parentView)
	{

	}

	public void visitAfterChildren(ParentView parentView)
	{

	}

	
	public void visit(AbstractView view)
	{
			
	}

	public void visit(Hittable view)
	{
			
	}
	
	public void visit(BasicView view)
	{
			
	}



	
	
	

}
