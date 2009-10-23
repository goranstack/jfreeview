package se.bluebrim.view.example;

import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.layout.Layout;
import se.bluebrim.view.layout.OptimalFitLayout;

public class TestRootView extends AbstractParentView implements NamedView
{

	public TestRootView(ViewContext viewContext, Layout layoutManager)
	{
		super(viewContext, layoutManager);
		
		// Group 1; 1-18
		NumberGroupView numberGroupView = new NumberGroupView(viewContext, new OptimalFitLayout());
		numberGroupView.setName("G1");
		addChild(numberGroupView);
		for (int i = 1; i <= 18; i++) 
		{
			numberGroupView.addChild(new NumberView(viewContext, i));
		}
		
		// Group 2; 19-40
		numberGroupView = new NumberGroupView(viewContext, new OptimalFitLayout());
		numberGroupView.setName("G2");
		addChild(numberGroupView);
		for (int i = 19; i <= 40; i++) 
		{
			numberGroupView.addChild(new NumberView(viewContext, i));
		}
		
//		layout();

	}

	public String getName()
	{
		return "RootView";
	}
					
}
