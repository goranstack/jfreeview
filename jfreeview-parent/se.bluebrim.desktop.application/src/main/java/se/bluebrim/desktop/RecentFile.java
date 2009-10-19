package se.bluebrim.desktop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * When an application is started a collection of RecentFile's are read from a file and
 * when an application is terminated a collection of RecentFile's are written to a file.
 * RecentFile objects are used as Icons in the RecentFileActions that are used in the
 * open recent file menu. A RecentFile that is currently open in an application has no
 * thumbnail, instead a miniature of the real window content is displayed.
 * 
 * @author GStack
 *
 */
public class RecentFile implements Serializable, Icon
{
	private static final double THUMBNAIL_MAX_WIDTH = 50.0;
	private static final double THUMBNAIL_MAX_HEIGHT = 50.0;
	private static final double INSET = 6.0;
	public static final double ICON_WIDTH = THUMBNAIL_MAX_WIDTH + (INSET * 2);
	public static final double ICON_HEIGHT = THUMBNAIL_MAX_HEIGHT + (INSET * 2);
	private static final long serialVersionUID = 1L;

	private File file;
	private transient JComponent component;
	private transient BufferedImage thumbnail;
	private transient double scale;
	private transient int thumbnailHeight;
	private transient int thumbnailWidth;
	
	public RecentFile(File file, JComponent component)
	{
		this.file = file;
		this.component = component;
		double vScale = THUMBNAIL_MAX_HEIGHT / component.getHeight();
		double hScale = THUMBNAIL_MAX_WIDTH / component.getWidth();
		scale = Math.min(vScale, hScale);
		thumbnailHeight = (int) (component.getHeight() * scale);
		thumbnailWidth = (int) (component.getWidth() * scale);
	}
	
	/**
	 * Use print instead of paint to avoid:
	 * <pre>
	 * java.lang.NullPointerException
	 * at javax.swing.BufferStrategyPaintManager.flushAccumulatedRegion(BufferStrategyPaintManager.java:398)
	 * </pre>
	 */
	private BufferedImage createThumbnail()
	{
		BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = thumbnail.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2d.scale(scale, scale);
		component.print(g2d);
		g2d.dispose();
		return thumbnail;	
	}

	public String getName()
	{
		return file.getName();
	}

	public File getFile()
	{
		return file;
	}
	
  private void writeObject( ObjectOutputStream out ) throws IOException
  {
  	out.defaultWriteObject();
  	if (component != null)
  		ImageIO.write(createThumbnail(), "png", out);
  	else
   		ImageIO.write(thumbnail, "png", out);  	 
  }
  
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
	{
		in.defaultReadObject();
		thumbnail = ImageIO.read(in);
	}

	@Override
	public int getIconHeight()
	{
		return (int) ICON_WIDTH;
	}

	@Override
	public int getIconWidth()
	{
		return (int) ICON_HEIGHT;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		if (component != null)
			drawThumbnail(createThumbnail(), g, x, y);
		else
			drawThumbnail(thumbnail, g, x, y);			
	}
	
	private void drawThumbnail(BufferedImage thumbnail, Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.translate(x, y);
		g2d.fill(new Rectangle2D.Double(0, 0, ICON_WIDTH, ICON_HEIGHT));
		g2d.drawImage(thumbnail, AffineTransform.getTranslateInstance((ICON_WIDTH - thumbnail.getWidth()) / 2.0, (ICON_HEIGHT- thumbnail.getHeight()) / 2.0), null);
		g2d.translate(-x, -y);
	}

	@Override
	public boolean equals(Object obj)
	{

	  if (obj == null) { return false; }
	   if (obj == this) { return true; }
	   if (obj.getClass() != getClass()) {
	     return false;
	   }
	   RecentFile another = (RecentFile) obj;
	   return new EqualsBuilder()
	                 .append(file, another.file)
	                 .isEquals();
		}
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(47, 13).append(file).toHashCode();
	}
	
	
}
