package se.bluebrim.isac.desktop;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Paintable;

/**
 * View that scales and render a BufferedImage
 * 
 * @author GStack
 *
 */
public class BufferedImageView extends AbstractImageView
{
	private BufferedImage bufferedImage;
	
	public BufferedImageView(ViewContext viewContext, URL url, String propertyKey)
	{
		super(viewContext, url, propertyKey);
	}

	@Override
	protected Rectangle2D loadImage(URL url)
	{
		try
		{
			bufferedImage = ImageIO.read(url.openStream());
			return new Rectangle2D.Float(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void paintImage(Paintable g)
	{
		g.drawRenderedImage(bufferedImage);
	}

	@Override
	public Object getModel()
	{
		return bufferedImage;
	}

}
