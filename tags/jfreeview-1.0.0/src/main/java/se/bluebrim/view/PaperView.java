package se.bluebrim.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.w3c.dom.DOMImplementation;

import se.bluebrim.view.impl.AbstractParentView;
import se.bluebrim.view.impl.AbstractView;
import se.bluebrim.view.impl.ViewContext;
import se.bluebrim.view.paint.Graphics2DWrapper;
import se.bluebrim.view.paint.Paintable;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * View that gives the illusion of a paper by painting a white surface, border and a drop shadow.
 * Added to the root view as a single child. The layout manager of the root view adjust the size
 * and position to make appropriate insets to create the paper illusion.
 *
 */
public class PaperView extends AbstractParentView implements Selectable
{		
	private Border border;
	
	public PaperView(ViewContext viewContext)
	{
		super(viewContext, null);
		setFixedPosition(true);
		border = BorderFactory.createCompoundBorder(new DropShadowBorder(Color.BLACK, 8, .5f, 12, false, false, true, true){
			@Override
			/**
			 * Workaround to avoid a white gap between the line border and the shadow 
			 */
			public Insets getBorderInsets(Component c)
			{
				return new Insets(0, 0, getShadowSize() - 1, getShadowSize() - 1);
			}
		}, BorderFactory.createLineBorder(Color.BLACK));
	}
			
	
	@Override
	public boolean isDropTarget()
	{
		return true;
	}
	
	@Override
	public boolean acceptDrop(TransferableView transferable)
	{
		return true;
	}
	
	@Override
	public boolean acceptDrop(List transferables)
	{
		return true;
	}
	
	@Override
	protected void drawBeforeChildren(Paintable g)
	{
		if (g.getTarget() == Paintable.Target.Screen)
		{
			if (Paintable.RANDOM_COLORS.value) // For debugging of repaint areas		
				g.setRandomColor();
			else
				g.setColor(Color.WHITE);
			Rectangle2D bounds = getBounds();
			Insets insets = border.getBorderInsets(null);
			bounds.setFrame(bounds.getX() + insets.left, bounds.getY() + insets.top, bounds.getWidth() - insets.right - insets.left, bounds.getHeight() - insets.top - insets.bottom);
			g.fill(bounds);
			g.setColor(Color.BLACK);
			border.paintBorder(null, g.getGraphics(), (int)getX(), (int)getY(), (int)getWidth(), (int)getHeight());
		} else
		{
			g.setColor(Color.WHITE);
			g.fill(getBounds());
		}
	}
	
	/**
	 * @deprecated let the action or command handle the file chooser
	 */
	public void saveAsPdf(JFileChooser fileChooser, Component dialogParent)
	{
		if (fileChooser.showSaveDialog(dialogParent) != JFileChooser.APPROVE_OPTION)
			return;

		File outFile = fileChooser.getSelectedFile();
		if (outFile.exists())
			if (JOptionPane.showConfirmDialog(dialogParent, "File already exists. Overwrite?", "Question", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
		
		saveAsPdf(outFile, dialogParent);
	}


	public void saveAsPdf(File outFile, Component dialogParent)
	{
		com.lowagie.text.Rectangle landscape = PageSize.A3.rotate();
		float w = landscape.width();
		float h = landscape.height();
		double xScale = w/getWidth();
		double yScale = h/getHeight();
		double scaleToFit = Math.min(xScale, yScale);
		Document document = new Document(landscape);

		try
		{
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outFile));
			document.open();
			DefaultFontMapper mapper = new DefaultFontMapper();
			PdfContentByte pdfContentByte = writer.getDirectContent();
			PdfTemplate pdfTemplate = pdfContentByte.createTemplate(w, h);
			Graphics2D g2d = pdfTemplate.createGraphics(w, h, mapper);
			g2d.scale(scaleToFit, scaleToFit);
			g2d.translate(-getX(), -getY());  // Eliminate paperview's location at the "desktop"
			pdfTemplate.setWidth(w);
			pdfTemplate.setHeight(h);
			paint(new Graphics2DWrapper(g2d, Paintable.Target.PDF));
			g2d.dispose();
			pdfContentByte.addTemplate(pdfTemplate, 0, 0);
		} catch (DocumentException de)
		{
			showSaveAsPdfError(dialogParent, de);
		} catch (IOException ioe)
		{
			showSaveAsPdfError(dialogParent, ioe);
		}

		document.close();
		try
		{
			Desktop.getDesktop().open(outFile);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void saveAsSvg(File outFile)
	{
		saveAsSvg(outFile, true);
	}
	
	public void saveAsSvg(File outFile, boolean rotate90)
	{		
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();			
		org.w3c.dom.Document document = domImpl.createDocument(null, "svg", null);
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);		
		svgGenerator.translate(-getX(), -getY());  // Eliminate paperview's location at the "desktop"

		if (rotate90)
		{
			svgGenerator.setSVGCanvasSize(new Dimension((int)getHeight(), (int)getWidth()));
			svgGenerator.translate(0, getWidth());			
			svgGenerator.rotate(-Math.toRadians(90));
		} else
		{
			svgGenerator.setSVGCanvasSize(new Dimension((int)getWidth(), (int)getHeight()));			
		}
		
		paint(new Graphics2DWrapper(svgGenerator, Paintable.Target.SVG));
		svgGenerator.dispose();

		FileWriter writer;
		try
		{
			writer = new FileWriter(outFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			svgGenerator.stream(writer);
		}
		catch (SVGGraphics2DIOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void saveAsPng(File outFile)
	{
		saveAsPng(outFile, true);
	}
	
	public void saveAsPng(File outFile, boolean rotate90)
	{
		BufferedImage bufferedImage;
		if (rotate90)
			bufferedImage = new BufferedImage((int)getHeight(), (int)getWidth(), BufferedImage.TYPE_INT_RGB);
		else
			bufferedImage = new BufferedImage((int)getWidth(), (int)getHeight(), BufferedImage.TYPE_INT_RGB);
			
		Graphics2D g2d = (Graphics2D)bufferedImage.getGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fill(new Rectangle2D.Float(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()));
		g2d.translate(-getX(), -getY());  // Eliminate paperview's location at the "desktop"

		if (rotate90)
		{
			g2d.translate(0, getWidth());			
			g2d.rotate(-Math.toRadians(90));			
		}
		paint(new Graphics2DWrapper(g2d, Paintable.Target.BitmapImage));
		g2d.dispose();
		try
		{
			ImageIO.write(bufferedImage, "png", outFile);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public JComponent getTumbnailComponent()
	{
		return new ThumbnailPanel(this);
	}
	
	private void showSaveAsPdfError(Component dialogParent, Exception e)
	{
		JOptionPane.showMessageDialog(dialogParent, "Unable to save as PDF due to\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	private static class ThumbnailPanel extends JPanel
	{
		private PaperView paperView;
		
		public ThumbnailPanel(PaperView paperView)
		{
			super();
			this.paperView = paperView;
			setSize((int)paperView.getWidth(), (int)paperView.getHeight());
			setBackground(Color.WHITE);
		}

		@Override
		public void print(Graphics g)
		{
			Graphics2DWrapper g2d = new Graphics2DWrapper((Graphics2D)g, Paintable.Target.BitmapImage);
//			g2d.translate(-getX(), -getY());  // Eliminate paperview's location at the "desktop"
//			g2d.setColor(Color.WHITE);
//			g2d.fill(getBounds());
			paperView.paint(g2d);
		}
		
		@Override
		public void paint(Graphics g)
		{
			throw new UnsupportedOperationException("Use print instead to avoid NPE in Java2D");
		}
	}

}