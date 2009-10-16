package se.bluebrim.view.impl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.bluebrim.util.StringTool;
import se.bluebrim.view.Handle;
import se.bluebrim.view.Hittable;
import se.bluebrim.view.Layoutable;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.ResizableContainer;
import se.bluebrim.view.ResizeListener;
import se.bluebrim.view.Selectable;
import se.bluebrim.view.TransferableView;
import se.bluebrim.view.View;
import se.bluebrim.view.ViewDropTarget;
import se.bluebrim.view.ViewLayer;
import se.bluebrim.view.geom.DoubleInsets;
import se.bluebrim.view.paint.Graphics2DWrapper;
import se.bluebrim.view.paint.Paintable;
import se.bluebrim.view.select.SelectionManager;
import se.bluebrim.view.tool.DoubleClickGesture;
import se.bluebrim.view.tool.RubberBandGesture;
import se.bluebrim.view.tool.SingleClickGesture;
import se.bluebrim.view.value.BooleanValue;
import se.bluebrim.view.zoom.Scale;


/**
 * Default implementation of the three basic view role interfaces. Use this class
 * as a base class for your specific leaf views. To implement specific parent views
 * inherit from <code>AbstractParentView</code>.
 * A view can choose to hide it self at a certain detail level. The detail
 * level is specified in the ViewContext object.<br>
 * A BasicView also manages handles. Subclasses can add handles and the BasicView
 * will take care of traversing and painting. The paint method is always called even
 * though handles often are invisible.
 *  

 * @author G Stack
 *
 */
public abstract class BasicView extends AbstractView implements Layoutable, Hittable, ViewDropTarget, Printable, Cloneable
{
	public static final BooleanValue SHOW_LAYER_NAME = new BooleanValue(false);
	
	// Bound properties
	public static final String HEIGHT = "height";
	public static final String WIDTH = "width";
	public static final String LOCATION = "location";
		
	/**
	 * Uses NTSC conversion formula.
	 * @param color The color that is converted to a gray value
	 * @return a value between 0 (black) and 255 (white)
	 */
	public static double getGray(Color color)
	{
		return 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
	}

	public static boolean isDark(Color color)
	{
		return getGray(color) < 255/2;
	}
	
	public static Color whiteIfDarkBackground(Color background)
	{
		return isDark(background) ? Color.WHITE : Color.BLACK;
	}

	private float x;
	private float y;
	private float width;
	private float height;
	private List handles; 	// <Handle> Can be empty
	private List dragFeedbackViews; 	// <View> Can be empty
	private List layers;
	private boolean hasFixedX;
	private boolean hasFixedY;
	
	protected AffineTransform transform;	// View to component transformation
	protected boolean highLighted = false;
	protected ViewContext viewContext;
	private ResizableContainer container;
	private ParentView parent;
	private ResizeListener containerResizedListener;
	private float proposedX = -1;		// Fix until drag really moves the view instead of translating the Graphics
	private boolean hidden = false;		// Used to hide view while animating drag with direct painting
	
	public BasicView(ViewContext viewContext)
	{
		this.viewContext = viewContext;
		handles = new ArrayList();
		dragFeedbackViews = new ArrayList();
		transform = new AffineTransform();
		layers = new ArrayList();
		layers.add(ViewLayer.DEFAULT_LAYER);
	}
	
	protected void setParent(ParentView parent)
	{
		if (this.parent != null && parent != null)
			throw new IllegalStateException("There is already a parent.");
		this.parent = parent;
		container = parent;
	}
	
	/**
	 * The root view has no parent view. This method is used assign the ViewPanel as a resizable container
	 */
	public void setContainer(ResizableContainer container)
	{
		if (this.container != null)
			throw new UnsupportedOperationException("There is already a container");
		this.container = container;
	}
	
	protected void addContainerResizedListener(final ResizableContainer container)
	{
		containerResizedListener = new ResizeListener()
		{
			public void resized(String dimension)
			{
				BasicView.this.containerResized(container);
			}
		};
		if (container != null)
			container.addResizeListener(containerResizedListener);
	}
	
	protected void clearHandles()
	{
		handles.clear();
	}
	
	protected void removeContainerResizedListener()
	{
		if (container != null)
			container.removeResizeListener(containerResizedListener);
	}
	
	/**
	 * Terminate present parent relation before setting
	 * up the new one. The view will maintain its absolute
	 * position that is its position relative the root view.
	 */
	public void changeParent(ParentView newParent)
	{
		Point2D p1 = parent.getLocationInRoot();
		removeParentListeners();
		parent.removeChild(this);
		newParent.addChild(this);
		addParentListeners(newParent);
		parent = newParent;
		setTransform(viewContext.getTransform());
		putOnTop();
		Point2D p2 = parent.getLocationInRoot();
		translate((float)(p1.getX() - p2.getX()), (float)(p1.getY() - p2.getY()));
		container = parent;
	}
	
	public void addHandle(Handle handle)
	{
		handles.add(handle);
	}

	public void removeHandle(Handle handle)
	{
		handles.remove(handle);
	}

	/**
	 * Paint the handles to display the resize feedback. Tried to
	 * call repaint for the activive handle in ResizeTool mouseDragged
	 * method but this did not work. This will do for now.
	 */
	private void paintHandles(Paintable g)
	{
		for (Iterator iter = handles.iterator(); iter.hasNext();) 
		{
			Handle handle = (Handle) iter.next();
			handle.paint(g);
		}
	}

	private void paintDragFeedbackViews(Paintable g)
	{
		for (Iterator iter = dragFeedbackViews.iterator(); iter.hasNext();) 
		{
			Handle handle = (Handle) iter.next();
			handle.paintFeedback(g);
		}
	}

	protected void traverseHandles(ViewVisitor visitor)
	{
		for (Iterator iter = handles.iterator(); iter.hasNext();) 
		{
			AbstractHandle handle = (AbstractHandle) iter.next();
			handle.visit(visitor);			
		}
	}
	
	/**
	 * Handles are a kind of children so we visit them first.
	 */
	public void traverseDepthFirst(ViewVisitor visitor)
	{
		traverseHandles(visitor);
		super.traverseDepthFirst(visitor);
	}
	
	public void traverseHeightFirst(ViewVisitor visitor)
	{
		super.traverseHeightFirst(visitor);
//		traverseHandles(visitor);
	}
	
	/**
	 * Return a transform that can be used to transform geometry of self
	 * to be relative specified ancestor view instead of relative parent view 
	 */
	public AffineTransform getTransformFor(ParentView ancestor)
	{
		List<ParentView> parentChain = getParentChain();
		double tx = 0;
		double ty = 0;
		for (ParentView parentView : parentChain)
		{
			if (parentView.equals(ancestor))
				return AffineTransform.getTranslateInstance(tx, ty);
			else
			{
				tx = tx + parentView.getX();
				ty = ty + parentView.getY();
			}
		}
		throw new IllegalArgumentException("Specified ancestor is not an ancestor");		
	}
	
	
	public List<ParentView> getParentChain()
	{
		List<ParentView> parentChain = new ArrayList<ParentView>();
		ParentView parent = getParent();
		while (parent != null)
		{
			parentChain.add(parent);
			parent = parent.getParent();
		}
		return parentChain;
	}
	
	/**
	 * Call private paint once for each layer and set the current layer in the
	 * Paintable object. Only views that belongs to the current layer will be
	 * painted. This is how views that are dragged are painted at the top of
	 * the other views during drag or resize.
	 */
	public final void paint(Paintable g)
	{
		if (hidden && !g.getPaintableHints().getPaintHidden())
			return;
		if (isRootView() && getLayers().size() > 1)
		{
			for (Iterator iter = getLayers().iterator(); iter.hasNext();)
			{
				ViewLayer currentLayer = (ViewLayer)iter.next();
				g.setLayer(currentLayer);
//				System.out.println("AbstractParentView paint layer: " + g.getLayer().getName());
				paintLayer(g);
			}
		} else
			paintLayer(g);
		paintHandles(g);
		paintDragFeedbackViews(g);

	}

	private boolean isRootView()
	{
		return getParent() == null;
	}
	
	protected void paintLayer(Paintable g)
	{
	}

	
	public Shape getOpticalShape()
	{
		return expandOpticalShape(getBounds());
	}
	
	/**
	 * TODO: Can't get this to work. Tried to expand with half frame thickness.
	 * The dirty region got to small from time to time. Can't understand why.
	 * Expand with whole frame thickness even if that should be more than enough.
	 *
	 */
	protected Shape expandOpticalShape(Rectangle2D rect)
	{
		float thickness = getSelectedFrameThickness();		// Always use selected frame thickness in case a child is selected  near the edge
		Rectangle2D.Double opticalRect = new Rectangle2D.Double(
				rect.getX() - thickness, 
				rect.getY() - thickness,
				rect.getWidth() + thickness*2,
				rect.getHeight() + thickness*2);
		return expandOpticalShapeWithDragFeedbackViews(opticalRect);		
	}
	
	private Shape expandOpticalShapeWithDragFeedbackViews(Rectangle2D opticalRect)
	{
		for (Iterator iter = dragFeedbackViews.iterator(); iter.hasNext();)
		{
			View view = (View)iter.next();
			opticalRect = opticalRect.createUnion(view.getOpticalShape().getBounds2D());			
		}
		return opticalRect;
	}

	public Shape getDirtyRegion()
	{
		return super.getDirtyRegion();
	}
			
	protected float getFrameThickness()
	{
		return isSelected() ? getSelectedFrameThickness() : getUnselectedFrameThickness();
	}
	
	protected float getSelectedFrameThickness()
	{
		return 2.0f;
	}
	
	protected float getUnselectedFrameThickness()
	{
		return 1f;
	}
	
	public float getHeight()
	{
		return height;
	}

	public void setHeight(float height)
	{
		Float oldValue = new Float(this.height);
		this.height = height;
		firePropertyChange(HEIGHT, oldValue, new Float(height));
	}	

	public void setSilentHeight(float height)
	{
		this.height = height;
	}	

	public float getWidth()
	{
		return width;
	}

	public void setWidth(float width)
	{
		Float oldValue = new Float(this.width);
		this.width = width;
		firePropertyChange(WIDTH, oldValue, new Float(width));
	}

	public void setSilentWidth(float width)
	{
		this.width = width;
	}

	public float getX()
	{
		return x;
	}

	public void setX(float x)
	{
		this.x = x;
	}

	public float getY()
	{
		return y;
	}

	public void setY(float y)
	{
		this.y = y;
	}
	
	public void setLocation(Point2D point)
	{
		Point2D oldValue = new Point2D.Float(x, y);		
		x = (float)point.getX();
		y = (float)point.getY();
		firePropertyChange(LOCATION, oldValue, new Point2D.Float(x, y));
	}
	
	public void setBounds(float x, float y, float width, float height)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		firePropertyChange(HEIGHT);
		firePropertyChange(WIDTH);
		firePropertyChange(LOCATION);
	}
	
	public float getMinHeight()
	{
		return 0;
	}

	public float getMinWidth()
	{
		return 0;
	}
	
	
	public float getMaxHeight()
	{
		return Float.MAX_VALUE;
	}

	public float getMaxWidth()
	{
		return Float.MAX_VALUE;
	}

	public float getMinX()
	{
		return 0;
	}

	public float getMinY()
	{
		return 0;
	}
	
	
	public float getMaxX()
	{
		return Float.MAX_VALUE;
	}

	public float getMaxY()
	{
		return Float.MAX_VALUE;
	}
	/**
	 * Overrides by views with Layoutable children and a LayotManager
	 */
	protected void layout()
	{
		
	}
	
	public void setTransform(AffineTransform transform)
	{
		this.transform = transform;
	}
	
	

	public void visit(ViewVisitor visitor)
	{
		super.visit(visitor);
		visitor.visit(this);
		visitor.visit((Hittable)this);		
	}

	/**
	 * Subclasses that implements <code>Selectable</code> don't have to implement
	 * anything.
	 */
	public boolean isSelected()
	{
		return this instanceof Selectable && getSelectionManager().isSelected((Selectable)this);
	}

	/**
	 * Subclasses that implements <code>Selectable</code> don't have to implement
	 * anything.
	 */
	public void select(MouseEvent mouseEvent)
	{
		putOnTop();
		getSelectionManager().select((Selectable)this, mouseEvent);
	}
	
	public void selectionChanged()
	{
		getViewContext().repaint(getSelectedDirtyRegion().getBounds());		
	}
	
	protected Shape getSelectedDirtyRegion()
	{
		if (isSelected())
			return getDirtyRegion();
		else
		{
			Rectangle2D rect = getDirtyRegion().getBounds2D();
			float outset = getSelectedFrameThickness() - getUnselectedFrameThickness();
			return new Rectangle2D.Double(rect.getX() - outset, rect.getY() - outset, rect.getWidth() + 2*outset, rect.getHeight() + 2*outset);
		}
	}

	protected void deselect()
	{
		getSelectionManager().deselect((Selectable)this);
	}

	public void putOnTop()
	{
		if (parent != null)
		{
			parent.putOnTop();
			parent.putOnTop(this);
		}
	}
	
	/**
	 * Make the view a child to the root view
	 */
	public void pullUpToRoot()
	{
		changeParent(getRootView());
	}
		
	protected void removeParentListeners()
	{
		
	}
	
	protected void addParentListeners(ParentView parent)
	{
		
	}
	
	protected ParentView getRootView()
	{
		ParentView rootView = parent;
		while (rootView.getParent() != null)
			rootView = rootView.getParent();
		return rootView;		
	}
	
	protected SelectionManager getSelectionManager()
	{
		return viewContext.getSelectionManager();
	}
	
	public boolean acceptDrop(TransferableView transferable)
	{
		return false;
	}
	
	public void drop(TransferableView transferable, Point location, ActionModifiers modifiers, Component dialogParent)
	{
		
	}
	
	
	public boolean isHighLighted()
	{
		return highLighted;
	}

	public void setHighLighted(boolean highLighted)
	{
		this.highLighted = highLighted;
	}
	
	public void singleClickGesture(SingleClickGesture singleClickGesture)
	{
		if (this instanceof Selectable)
		{
			((Selectable)this).select(singleClickGesture.getMouseEvent());
			singleClickGesture.consumed();
		}
	}
	
	/**
	 * The view is hitted by a double click. Views that reacts on double
	 * click overrides this method.
	 */
	public void doubleClickGesture(DoubleClickGesture doubleClickGesture)
	{
		
	}
	
	/**
	 * Selection of views hitted by rubberband gesture is handled by
	 * the SelectionTool. Views that want's additional behaviour can override
	 * this method.
	 */
	public void rubberBandGesture(RubberBandGesture rubberBandGesture)
	{
		
	}

	public boolean isVisible()
	{
		return true;
	}

	/**
	 * The default implementation of this method is to return true if 
	 * all transferables are accepted
	 */
	public boolean acceptDrop(List transferables)
	{
		boolean accept = true;
		for (Iterator iter = transferables.iterator(); iter.hasNext();) 
		{
			TransferableView transferable = (TransferableView) iter.next();
			if (!acceptDrop(transferable))
				return false;
		}
		return accept;
	}
	
	public boolean isChildren(List transferables)
	{
		boolean isChildren = true;
		for (Iterator iter = transferables.iterator(); iter.hasNext();) 
		{
			TransferableView transferable = (TransferableView) iter.next();
			if (!isChild(transferable))
				return false;
		}
		return isChildren;
	}
	
	public boolean isChild(TransferableView transferable)
	{
		return false;
	}


	/**
	 * The default implementation of this method is to call drop for each
	 * transferable in the list. That works fine when the list contains only
	 * one element which is common, but when we handle multiple transferables
	 * perhaps all of them should be processed in the same transaction.
	 */
	public void drop(List transferables, Point location, ActionModifiers modifiers, Component dialogParent)
	{
		for (Iterator iter = transferables.iterator(); iter.hasNext();) 
		{
			drop((TransferableView) iter.next(), location, modifiers, dialogParent);
		}
	}

	public AffineTransform getTransform()
	{
		return transform;
	}

	protected void drawLeftJustifiedText(Paintable g, String text, float leftInset, float topInset)
	{
		float ascent = getAscent(g, text);
		Rectangle2D bounds = getTextBounds(g, text);
		Point2D center = getCenter();
		g.translate(leftInset, center.getY() + topInset);
		g.drawString(text, 0, (float)(ascent - bounds.getHeight()/2.0));
		g.translate(-leftInset, -(center.getY() + topInset));
	}

	protected void drawRightJustifiedText(Paintable g, String text, float rightInset, float topInset)
	{
		float ascent = getAscent(g, text);
		Rectangle2D bounds = getTextBounds(g, text);
		Point2D center = getCenter();
		g.translate(-rightInset, center.getY() + topInset);
		g.drawString(text, (float)(getWidth() - bounds.getWidth()), (float)(ascent - bounds.getHeight()/2.0));
		g.translate(rightInset, -(center.getY() + topInset));
	}
	
	protected void drawCenteredTextAtTop(Paintable g, String text)
	{
		float ascent = getAscent(g, text);
		Rectangle2D bounds = getTextBounds(g, text);
		Point2D center = getCenter();
		g.drawString(text, (float)(center.getX() - bounds.getWidth()/2.0), getY()+ ascent);
	}
	

	/**
	 * Draw the text in the rectangle by breaking it up in lines. Continue below the rectangle
	 * if the text is to long.
	 */
	protected void drawTextLayout(Paintable g, Rectangle2D rect, String text, Color color, Font font)
	{
		if (StringTool.isNullOrEmpty(text))
			return;
		int textLength = text.length();
		// Tried to do this in loadViewState but the text was blank. Can't figure out why.
		AttributedString attributedString = new AttributedString(text);
		attributedString.addAttribute(TextAttribute.FONT, font, 0, textLength);		
		attributedString.addAttribute(TextAttribute.FOREGROUND, color, 0, textLength);		
		
		float inset = 2;
		g.translate(rect.getX() + inset, rect.getY() + inset);
		float width = (float)(rect.getWidth() - (2 * inset));
		g.setColor(color);
		g.setFont(font);
		LineBreakMeasurer linebreaker = new LineBreakMeasurer(attributedString.getIterator(), g.getFontRenderContext());
		float y = 0.0f;
		while (linebreaker.getPosition() < textLength) 
		{
			TextLayout textLayout = linebreaker.nextLayout(width);

			y += textLayout.getAscent();
			textLayout.draw(g.getGraphics(), 0, y);
			y += textLayout.getDescent() + textLayout.getLeading();
		}
		g.translate(-(rect.getX() + inset), -(rect.getY() + inset));
		
	}
	
	protected void drawTextView(Paintable g, javax.swing.text.View htmlView, DoubleInsets textInsets)
	{
		Rectangle2D bounds = getBounds();
      Shape restoreClip = g.getClip();
      g.clip(getBounds());		// fix NPE in javax.swing.text.BoxView.paint when dragging      
		htmlView.paint(g.getGraphics(), new Rectangle((int)(bounds.getX()+ textInsets.left), 
			(int)(bounds.getY() + textInsets.top), 
			(int)(bounds.getWidth() - (textInsets.left + textInsets.right)), 
			(int)(bounds.getHeight() - (textInsets.top + textInsets.bottom))));
		g.setClip(restoreClip);
	}


	protected Paint getFillPaint(Color fillColor)
	{
		Rectangle2D bounds = getBounds();
		if (modelHasUnconfirmedChanges())
		{
			return new GradientPaint((float)bounds.getX(), (float)bounds.getY(), fillColor, (float)(bounds.getX() + 10), (float)(bounds.getY() + 10), Color.LIGHT_GRAY, true);
		} else
			return fillColor;
	}

	protected boolean modelHasUnconfirmedChanges()
	{
		return false;
	}

	public BufferedImage getVisualRepresentation()
	{
		BufferedImage image = new BufferedImage((int)getWidth(), (int)getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		paint(new Graphics2DWrapper(g2d, Paintable.Target.BitmapImage, viewContext));
		return image;
	}
	
	public TransferableView getTransferableCopy(ViewContext viewContext)
	{
		BasicView copy = getCopy(viewContext);
		copy.convertToGlobalLocation();
		return (TransferableView)copy;
	}

	public BasicView getCopy(ViewContext viewContext)
	{
		try
		{
			BasicView clone = (BasicView)clone();
			clone.transform = new AffineTransform();	// Don't copy cashed data
			clone.setViewContext(viewContext);
			return clone;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Should support clone", e);
		}
	}
	
	public BasicView getCopy()
	{
		return getCopy(viewContext);
	}
	
	public Object getId()
	{
		return this;
	}

	public boolean hasFixedX()
	{
		return hasFixedX;
	}
	
	public boolean hasFixedY()
	{
		return hasFixedY;
	}
	
	public void setFixedX(boolean isXFixed)
	{
		hasFixedX = isXFixed;
	}
	
	public void setFixedY(boolean isYFixed)
	{
		hasFixedY = isYFixed;
	}
	
	public boolean hasFixedPosition()
	{
		return hasFixedX && hasFixedY;
	}

	public void setFixedPosition(boolean isPositionFixed)
	{
		hasFixedX = isPositionFixed;
		hasFixedY = isPositionFixed;
	}

	protected Dimension2D getStringSize(String string)
	{		
		Rectangle2D bounds = getFont().getStringBounds(string, ViewContext.DEFAULT_FONT_RENDER_CONTEXT);
		Dimension2D size = new Dimension();
		// StringBounds has negative Y representing the ascent
//		size.setSize(bounds.getWidth(), bounds.getHeight() - bounds.getY());
		size.setSize(bounds.getWidth(), bounds.getHeight());
		return size;
	}

	protected Dimension2D getStringSize(String string, DoubleInsets insets)
	{
		Dimension2D size = getStringSize(string);
		size.setSize(size.getWidth() + insets.left + insets.right, size.getHeight() + insets.top + insets.bottom);
		return size;
	}

	protected Font getFont()
	{
		return viewContext.getDefaultFont();
	}

	protected Rectangle2D centerRectangleOverView(Dimension2D rectangleSize)
	{
		return new Rectangle2D.Double((getWidth()- rectangleSize.getWidth())/2f + getX(), (getHeight()- rectangleSize.getHeight())/2f + getY(), rectangleSize.getWidth(), rectangleSize.getHeight());
	}


	public void setViewContext(ViewContext viewContext)
	{
		this.viewContext = viewContext;
	}
	
	public void translate(float dx, float dy)
	{
		setLocation(new Point2D.Float(getX() + dx, getY() + dy));
	}


	public void setPositionDevice(float x, float y)
	{
		Point2D src = new Point2D.Float(x, y);
		Point2D dest = new Point2D.Float(0, 0);
		try
		{
			transform.inverseTransform(src, dest);
		}
		catch (NoninvertibleTransformException e)
		{
		}
		setX((float)dest.getX());
		setY((float)dest.getY());
		
	}

	/**
	 * Converts the x and y coordinates to a coordinate relative to the root view
	 * instead of relative to the parent view. Used when before adding views to
	 * the drag layer.
	 */
	private void convertToGlobalLocation()
	{
		Point2D gLoc = getLocationInRoot();
		x = (float)gLoc.getX();
		y = (float)gLoc.getY();
	}

	public Point2D componentToView(Point point)
	{
		double scaledX = point.x / viewContext.getScale().x;
		double scaledY = point.y / viewContext.getScale().y;
//		System.out.println("BasicView.componentToView: x: " + (int)scaledX + " y: " + (int)scaledY);
		scaledX = scaledX - getX();
		scaledY = scaledY - getY();
		ParentView container = parent;
		while(container != null)
		{
			scaledX = scaledX - container.getX();
			scaledY = scaledY - container.getY();
			container = container.getParent();
		}
		return new Point2D.Double(scaledX, scaledY);
	}
	
		
	/**
	 * The default behaviour is to do nothing
	 */
	public void reflectInTransferable(TransferableView transferable)
	{
		
	}
	
	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException
	{
		if (pageIndex > 0) 
            return Printable.NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        Scale scaleToFit = calculateScaleToFit(pageFormat.getImageableWidth(), pageFormat.getImageableHeight(), getWidth(), getHeight());
        g2d.scale(scaleToFit.x, scaleToFit.y);
        Graphics2DWrapper graphics2DWrapper = new Graphics2DWrapper(g2d, Paintable.Target.Printer);
        paint(graphics2DWrapper);
        return Printable.PAGE_EXISTS;	
    }

	protected Scale calculateScaleToFit(double destWidth, double destHeight, double sourceWidth, double sourceHeight)
	{
		double horizontalScaleFactor = destWidth / sourceWidth;
		double verticalScaleFactor = destHeight / sourceHeight;
		double scale = Math.min(1.0, Math.min(horizontalScaleFactor, verticalScaleFactor));
		return new Scale(scale, scale);
	}
	
	public String getViewType()
	{
		String className = getClass().getName();
		return className.substring(className.lastIndexOf(".")+1);
	}
	
	public String getDebugString()
	{
		return getViewType() + " x: " + getX() + " y: " + getY() + " width: " + getWidth() + " height: " + getHeight();
	}

	/**
	 * Overidden by views that reflect the time scale in the view context
	 */
	public void timeScaleHasChanged()
	{
	}

	public boolean belongsToLayer(ViewLayer layer)
	{
		return layers.contains(layer);
	}
	
	/**
	 * Add all ancestors to specified layer as well
	 */
	public void addToLayer(ViewLayer layer)
	{
		if (layers.contains(layer))
			return;
		layers.add(layer);
		ParentView parentView = parent;
		while (parentView != null && parentView instanceof BasicView)
		{
			if (!((BasicView)parentView).layers.contains(layer))
				((BasicView)parentView).layers.add(layer);
			parentView = parentView.getParent();
		}
	}
	
	/**
	 * Remove all ancestors from specified layer as well
	 */
	public void removeFromLayer(ViewLayer layer)
	{
		layers.remove(layer);
		ParentView parentView = parent;
		while (parentView != null && parentView instanceof BasicView)
		{
			((BasicView)parentView).layers.remove(layer);
			parentView = parentView.getParent();
		}
	}

	public ParentView getParent()
	{
		return parent;
	}

	protected List getLayers()
	{
		return layers;
	}
	
	protected String getCommaSeparatedLayers()
	{
		StringBuffer buffer = new StringBuffer();
		for (Iterator iter = layers.iterator(); iter.hasNext();)
		{
			ViewLayer layer = (ViewLayer)iter.next();
			buffer.append(layer.getName());
			if (iter.hasNext())
				buffer.append(", ");			
		}
		return buffer.toString();
	}

	/**
	 * Called from Resizehandle to handle the case where a view is resized over 
	 * the edge of it's parent and therefore partly invisible due to clipping.
	 */
	public void expandParentIfNeeded()
	{
		if (parent != null)
		{
			if (getBottomBound() > parent.getHeight())
				parent.setSilentHeight(getBottomBound());
			if (getRightBound() > parent.getWidth())
				parent.setSilentWidth(getRightBound());
			parent.expandParentIfNeeded();
		}
		
	}

	public ViewContext getViewContext()
	{
		return viewContext;
	}
	
	public void startHandleManipulation(Handle handle)
	{
		super.startHandleManipulation(handle);
		dragFeedbackViews = handle.getFeedbackViews();
	}
		
	public void stopHandleManipulation(ActionModifiers modifiers)
	{
		super.stopHandleManipulation(modifiers);
		dragFeedbackViews.clear();
//		proposedX = -1;
	}

	public float getProposedX()
	{
		return proposedX;
	}

	public void setProposedX(float proposedX)
	{
		this.proposedX = proposedX;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
	
	public List getDragFeedbackViews()
	{
		return dragFeedbackViews;
	}

	public boolean lockVerticalMovement(ActionModifiers actionModifiers)
	{
		return false;
	}

	public boolean lockHorizontalMovement(ActionModifiers actionModifiers)
	{
		return false;
	}

	/**
	 * 
	 * Create handles to enable resizing of the left and right sides
	 */
	protected final void createResizeHandles()
	{
		addHandle(new WestResizeHandle(this));
		addHandle(new EastResizeHandle(this));
		addHandle(new NorthResizeHandle(this));
		addHandle(new SouthResizeHandle(this));
	}

}
