package se.bluebrim.view.print;

import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.Layout;

/**
 * Used by the PrintPreview to compose a report in case several views
 * should be printed in the same report.
 * 
 * @author GStack
 *
 */
public class CompositeView extends AbstractParentView
{

	public CompositeView(ViewContext viewContext)
	{
		super(viewContext, null);
	}

	public CompositeView(ViewContext viewContext, Layout layoutManager)
	{
		super(viewContext, layoutManager);
	}
	
	
}
