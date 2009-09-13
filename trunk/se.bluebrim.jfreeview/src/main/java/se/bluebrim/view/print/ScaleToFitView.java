package se.bluebrim.view.print;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.zoom.Scale;
/**
 * View wrapper that scales the wrapped view to fit in the size 
 * of the ScaleToFitView. If the view context has time scale the time
 * scale is used to fit the view in x dimension and the x scale is set to 1.
 * 
 * @author G Stack
 *
 */
public class ScaleToFitView extends AbstractParentView
{
	private Scale scaleToFit;
	private Scale inverseScaleToFit;
	private Layoutable viewToScale;

	public ScaleToFitView(ViewContext viewContext, Layoutable viewToScale)
	{
		super(viewContext);
		this.viewToScale = viewToScale;
		addChild(viewToScale);
		calculateScaleToFit();
		addResizeListener(new ResizeListener(){

			public void resized(String dimension)
			{
				calculateScaleToFit();				
			}});
	}


	protected void beforePaintChildren(Paintable g)
	{
		super.beforePaintChildren(g);
      g.scale(scaleToFit);
	}
	
	protected void afterPaintChildren(Paintable g)
	{
		super.afterPaintChildren(g);
		g.scale(inverseScaleToFit);
	}
	
	private final void calculateScaleToFit()
	{
		if (getWidth() == 0 || getHeight() == 0)
			scaleToFit = new Scale();
		else
		{
			scaleToFit = calculateScaleToFit(getWidth(), getHeight(), viewToScale.getWidth(), viewToScale.getHeight());
//			viewContext.adjustTimeScaleToFit(getWidth());		Do not have any effect. Can't see why
//			viewToScale.timeScaleHasChanged();			
		}
		inverseScaleToFit = scaleToFit.createInverted();
	}
		
	/**
	 * Skip layout of children since they are scaled to fit
	 */
	protected void layout()
	{		
	}
	
	/**
	 * viewToScale is not included in the child collection
	 */
	public void dispose()
	{
		super.dispose();
		viewToScale.dispose();
	}

}
