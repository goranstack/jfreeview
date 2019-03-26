package se.bluebrim.view.paint;

/**
 * Used to control various paint behavior in Paintable objects
 * 
 * @author GStack
 *
 */
public class PaintableHints
{
	public static final PaintableHints DEFAULT = new PaintableHints();
	public static final PaintableHints SUPRESS_FILL_CONTAINERS = new PaintableHints().setFillContainers(false);
	public static final PaintableHints PAINT_HIDDEN = new PaintableHints().setPaintHidden(true);

	private boolean fillContainers = true;
	private boolean paintHidden = false;

	public PaintableHints()
	{
	}
	
	public boolean getFillContainers()
	{
		return fillContainers;
	}

	public PaintableHints setFillContainers(boolean fillContainers)
	{
		this.fillContainers = fillContainers;
		return this;
	}

	public boolean getPaintHidden()
	{
		return paintHidden;
	}

	public PaintableHints setPaintHidden(boolean paintHidden)
	{
		this.paintHidden = paintHidden;
		return this;
	}


}
