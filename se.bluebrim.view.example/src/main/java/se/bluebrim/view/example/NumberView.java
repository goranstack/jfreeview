package se.bluebrim.view.example;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import se.bluebrim.view.*;
import se.bluebrim.view.impl.*;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.tool.DoubleClickGesture;


public class NumberView extends AbstractParentView implements Layoutable , TransferableView , PopupMenuProvider ,
	ToolTipProvider , HorizontalResizeFeedbackProvider, VerticalResizeFeedbackProvider
{
	private int number;
	private JPopupMenu popupMenu;
	private Font font;
	private WestResizeHandle westResizeHandle;
	private EastResizeHandle eastResizeHandle;
	private NorthResizeHandle northResizeHandle;
	private SouthResizeHandle southResizeHandle;

	public NumberView(ViewContext viewContext, int number)
	{
		super(viewContext, new TinyViewLayout());
		this.number = number;
		setWidth(80);
		setHeight(40);
		font = new Font("SansSerif", Font.PLAIN, 12);
		addSomeChildren();
		createPopupMenu();
		createHandles();
	}

	private void addSomeChildren()
	{
		for (int i = 0; i < 5; i++)
		{
			TinySometimesHiddenView view = new TinySometimesHiddenView(viewContext);
			addChild(view);
		}
	}

	protected void paintLayer(Paintable g)
	{
		if (Paintable.RANDOM_COLORS.value) // For debugging of repaint areas
			g.setRandomColor();
		else
			g.setColor(getFillColor());
		g.fill(getBounds());
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(getFrameThickness()));
		g.draw(getBounds());
		drawNumber(g);
		super.paintLayer(g); // Paint the children

	}

	private void drawNumber(Paintable g)
	{
		g.setColor(Color.GRAY);
		g.setFont(font);
		float ascent = g.getFont().getLineMetrics("123", g.getFontRenderContext()).getAscent();
		FontMetrics fontMetrics = g.getFontMetrics();
		String text = number + " " + ((NamedView)getParent()).getName();
		Rectangle2D bounds = fontMetrics.getStringBounds(text, g.getGraphics());
		Point2D center = getCenter();
		g.translate(center.getX(), center.getY());
		g.drawString(text, (float)(-bounds.getWidth() / 2.0), (float)(ascent - bounds.getHeight() / 2.0));
		g.translate(-center.getX(), -center.getY());
	}

	/**
	 * @return Returns the number.
	 */
	public int getNumber()
	{
		return number;
	}

	public void showPopupMenu(Component invoker, Point point)
	{
		popupMenu.show(invoker, point.x, point.y);

	}

	private void createPopupMenu()
	{
		popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem(number + " Choice one"));
		popupMenu.add(new JMenuItem(number + " Choice two"));
		popupMenu.add(new JMenuItem(number + " Choice tree"));
		popupMenu.add(new JMenuItem(number + " Choice four"));
	}

	public float getMinHeight()
	{
		return 8;
	}

	public float getMinWidth()
	{
		return 16;
	}

	public float getMaxWidth()
	{
		return 200;
	}

	public void doubleClickGesture(DoubleClickGesture doubleClickGesture)
	{
		JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(doubleClickGesture.getComponent()),
			"Double clicked at: " + number);
	}

	public String getToolTipText()
	{
		return "My number is: " + number;
	}

	/**
	 * If our childern is hidden due to detail level compute a fill color based on the hidden children otherwise white or
	 * random.
	 * 
	 */
	private Color getFillColor()
	{
		boolean visible = false;
		boolean state = false;
		for (Iterator iter = children.iterator(); iter.hasNext();)
		{
			TinySometimesHiddenView view = (TinySometimesHiddenView)iter.next();
			visible = visible || view.isVisible();
			state = state || view.getState();
		}

		if (visible)
			return Color.WHITE;
		else
			return state ? TinySometimesHiddenView.STATE_TRUE_COLOR : TinySometimesHiddenView.STATE_FALSE_COLOR;

	}

	/**
	 * 
	 * Create handles to enable resizing of the left and right sides
	 */
	private final void createHandles()
	{
		westResizeHandle = new WestResizeHandle(this);
		addHandle(westResizeHandle);
		eastResizeHandle = new EastResizeHandle(this);
		addHandle(eastResizeHandle);
		northResizeHandle = new NorthResizeHandle(this);
		addHandle(northResizeHandle);
		southResizeHandle = new SouthResizeHandle(this);
		addHandle(southResizeHandle);
	}


	public Object getModel()
	{
		return new Integer(number);
	}

	public String getWestSideFeedback()
	{
		return "X: " + getX();
	}

	public String getEastSideFeedback()
	{
		return "Width: " + getWidth();
	}

	public String getNorthSideFeedback()
	{
		return "Y: " + getY();
	}

	public String getSouthSideFeedback()
	{
		return "Height: " + getHeight();
	}

	public List getMoveFeedbackViews()
	{
		List feedbackViews = new ArrayList();
		feedbackViews.add(northResizeHandle);
		feedbackViews.add(westResizeHandle);
		return feedbackViews;
	}
	
	public List getEastResizeFeedbackViews()
	{
		List feedbackViews = new ArrayList();
		feedbackViews.add(eastResizeHandle);
		return feedbackViews;
	}

	public List getWestResizeFeedbackViews()
	{
		List feedbackViews = new ArrayList();
		feedbackViews.add(westResizeHandle);
		return feedbackViews;
	}

	public List getNorthResizeFeedbackViews()
	{
		List feedbackViews = new ArrayList();
		feedbackViews.add(northResizeHandle);
		return feedbackViews;
	}

	public List getSouthResizeFeedbackViews()
	{
		List feedbackViews = new ArrayList();
		feedbackViews.add(southResizeHandle);
		return feedbackViews;
	}
	
	public String toString()
	{
		return super.toString() + " no: " + number;
	}

}
