package se.bluebrim.view.zoom;

import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Implemented by Components that should be controlled by a ZoomController.
 * The implementor should reflect the scale factor in preferred size. 
 * The Component should reside in a scroll pane since zoom
 * without scrolling capability make little sence.
 *  
 * @author G Stack
 */
public interface Zoomable
{
	
	/**
	 * <ul>
	 * <li>1.0 = actual size</li>
	 * <li>0.5 = half the size</li>
	 * <li>2.0 = double size</li>
	 * </ul>
	 */
	public Scale getScale();

	/**
	 * <ul>
	 * <li>1.0 = actual size</li>
	 * <li>0.5 = half the size</li>
	 * <li>2.0 = double size</li>
	 * </ul>
	 * Call invalidate since the preferred size will change as a result of the changed scale factor. <br>
	 * Example of an implementation:
	 * 
	 * <pre>
	 * public void setScale(Scale scale)
	 * {
	 * 	this.scale = scale;
	 * 	invalidate();
	 * }
	 * </pre>
	 */
	public void setScale(Scale scale);

	/**
	 * Controls the level of details that is painted in the views. It's normally the
	 * same value as the scale factor but occasionally it can be valuable to control
	 * this aspect separatly from the scale factor. Use case: <br>
	 * You have zoomed in a lot an a view that you like to monitor from a long
	 * distance from the screen in an industrial environment.
	 * 
	 */
	public void setDetailLevel(Scale detailLevel);
	public Scale getDetailLevel();
	/**
	 * Example of an implementation:
	 * 
	 * <pre>
	 * public void paint(Graphics g) {
	 * 	Graphics2D g2d = (Graphics2D) g;
	 * 	g2d.scale(scaleFactor, scaleFactor);
	 * 	drawScaledContent(g2d);
	 * }
	 * 
	 * </pre>
	 * 
	 * For a pretty result from painting you can add the following lines:
	 * 
	 * <pre>
	 * g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
	 * 		RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	 * g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	 * 		RenderingHints.VALUE_ANTIALIAS_ON);
	 * </pre>
	 * 
	 */
	public void paint(Graphics g);

	/**
	 * Return the unscaled size multiplied by scale factor. The returned dimension is reflected in 
	 * the scrollbars as the scrollable area size.<br>
	 * Example of an implementation:
	 * 
	 * <pre>
	 * public Dimension getPreferredSize()
	 * {
	 * 	Dimension size = super.getPreferredSize();
	 * 	int preferredWidth = (int) Math.round(getUnscaledSize().getWidth() * scaleFactor);
	 * 	int preferredHeight = (int) Math.round(getUnscaledSize().getHeight() * scaleFactor);
	 * 	return new Dimension(preferredWidth, preferredHeight);
	 * }
	 * </pre>
	 */
	public Dimension getPreferredSize();
	
	public Dimension getUnscaledSize();
	
	public void paintImmediately();
		
}
