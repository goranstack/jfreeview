package se.bluebrim.view.example.svg.rasterizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.apache.batik.transcoder.TranscoderException;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;

import se.bluebrim.view.batik.SVGRasterizer;

/**
 * Uses Batik framework to display SVG images in a Swing panel. This is
 * normally done by JSVGCanvas but we needed a more lightweight component that
 * only take care of the actual rendering. The SVGRasterizer can handle the SVG
 * rendering in Views that contains SVG-images.<br>
 * Find more SVG files at: http://openclipart.org/media/tags/svg 
 *  
 * @author Goran Stack
 * 
 */
public class SVGRasterizerTest
{
	private static final Random random = new Random(System.currentTimeMillis());

	private JLabel label;
	private Container contentPane;
	private AbstractAction hyperLinkAction;
	private Map<URL, URL> svgSamples = new HashMap<URL, URL>();
	private Iterator<Map.Entry<URL, URL>> sampleIterator;

	public static void main(String[] args) throws TranscoderException, MalformedURLException
	{
		new SVGRasterizerTest().run();
	}

	private void run() throws TranscoderException, MalformedURLException
	{
		JFrame window = new JFrame();
		window.setTitle("SVG Rasterizer Test");
		window.setIconImage(new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = window.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(createCenterPanel(), BorderLayout.CENTER);
		contentPane.add(createSouthPanel(), BorderLayout.SOUTH);
		contentPane.add(createNorthPanel(), BorderLayout.NORTH);
		window.pack();
		window.setLocationRelativeTo(null); // Center on screen

		DropTarget dropTarget = new DropTarget(contentPane, TransferHandler.COPY_OR_MOVE, new FileDropListener());
		contentPane.setDropTarget(dropTarget);
		window.setVisible(true);
		initializeSampleMap();
		displayNextSample();
	}

	private void initializeSampleMap() throws MalformedURLException
	{
		addSample("http://vector-art.blogspot.com", "Bolt_NWR1.svg");
		addSample("http://xmlgraphics.apache.org/batik", "batik3D.svg");
		addSample("http://www.isc.tamu.edu/~lewing", "NewTux.svg");
		addSample("http://openclipart.org/media/people/mokush", "mokush_Realistic_Coffee_cup_-_Front_3D_view.svg");
		addSample("http://openclipart.org/media/people/Chrisdesign", "glossy-buttons.svg");
		addSample("http://openclipart.org/media/people/Chrisdesign", "tutanchamun.svg");
	}
	
	private void addSample(String originator, String resourceName) throws MalformedURLException
	{
		svgSamples.put(new URL(originator), SVGRasterizerTest.class.getResource(resourceName));		
	}
	
	private void displayNextSample()
	{
		if (sampleIterator == null || !sampleIterator.hasNext())
			sampleIterator = svgSamples.entrySet().iterator();

		Map.Entry<URL, URL> entry = sampleIterator.next();
		hyperLinkAction.putValue(Action.NAME, entry.getKey().toExternalForm());
		try {
			SVGRasterizerTest.this.displaySVG(entry.getValue());
		} catch (TranscoderException e1) {
			throw new RuntimeException(e1);
		}
	}

	private Component createNorthPanel()
	{
		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.add(new JLabel("Drag & Drop SVG files into this window or select next sample: "));
		JButton nextButton = new JButton(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SVGRasterizerTest.this.displayNextSample();
			}
		});
		nextButton.setContentAreaFilled(true);
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		nextButton.setIcon(new ImageIcon(getClass().getResource("nav_forward.gif")));
		toolBar.add(nextButton);
		return toolBar;
	}
		
	private Component createCenterPanel()
	{
		return new JScrollPane(createLayeredPane());
	}

	/**
	 * JLayeredPane won't work if any layout is set according to:
	 * http://forum.java.sun.com/thread.jspa?forumID=57&threadID=770737 <br>
	 * Therefore we have to do all layouting by our self.
	 */
	private JLayeredPane createLayeredPane()
	{
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		final CheckerBoardPanel checkerBoardPanel = new CheckerBoardPanel();

		JLayeredPane layeredPane = new JLayeredPane()
		{
			@Override
			public Dimension getPreferredSize()
			{
				return new Dimension(1000, 800);
			}

			@Override
			public void doLayout()
			{
				super.doLayout();
				label.setSize(getSize());
				checkerBoardPanel.setSize(getSize());
			}
		};

		layeredPane.add(checkerBoardPanel, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(label, JLayeredPane.DRAG_LAYER);
		return layeredPane;
	}
	
	private Component createSouthPanel()
	{
		Box box = new Box(BoxLayout.X_AXIS);
		box.add(createHyperLinkButton());
		return box;
	}

	@SuppressWarnings("unchecked")
	private JButton createHyperLinkButton()
	{
		hyperLinkAction = new AbstractAction("Xxxxxxxx")
		{
			public void actionPerformed(ActionEvent e)
			{
				try {
					Desktop.getDesktop().browse(new URI((String) hyperLinkAction.getValue(Action.NAME)));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		};
		JButton hyperLinkButton = new JButton(hyperLinkAction);
		hyperLinkButton.setBorderPainted(false);
		hyperLinkButton.setContentAreaFilled(false);

		Map map = hyperLinkButton.getFont().getAttributes();
		map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
		map.put(TextAttribute.FOREGROUND, Color.BLUE);
		hyperLinkButton.setFont(new Font(map));
		hyperLinkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return hyperLinkButton;
	}

	private void displaySVG(URL resource) throws TranscoderException
	{
		SVGRasterizer rasterizer = new SVGRasterizer(resource);
		BufferedImage img = rasterizer.createBufferedImage();
		label.setIcon(new ImageIcon(img));
	}

	private void handleException(Exception e, File file)
	{
		String message = file != null ? "The file: \"" + file.getPath() + "\" could not be loaded as an SVG image"
				: "An error occurred while handling the drop";
		JXErrorPane.showDialog(contentPane, new ErrorInfo("Drop failure", message, null, null, e, Level.SEVERE, null));
	}

	private static class CheckerBoardPanel extends JPanel
	{
		private int square = 8;
		private Rectangle rectangle = new Rectangle(0, 0, square, square);
		private Color lightGray = new Color(239, 239, 239);

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;
			int rows = getHeight() / square + 1;
			Rectangle clipBounds = g.getClipBounds();

			for (int row = 0; row < rows; row++) {
				drawRow(g2d, row, clipBounds);
			}
		}

		private void drawRow(Graphics2D g2d, int row, Rectangle clipBounds)
		{
			Rectangle rowBounds = new Rectangle(0, row * square, getWidth(), square);
			if (rowBounds.intersects(clipBounds))
			{
				AffineTransform at = g2d.getTransform();
				int columns = getWidth() / square + 1;
				g2d.translate(0, row * square);
				for (int column = 0; column < columns; column++) {
					drawSquare(g2d, row, column, clipBounds);
				}
				g2d.setTransform(at);
			}
		}

		private void drawSquare(Graphics2D g2d, int row, int column, Rectangle clipBounds)
		{
			Rectangle squareBounds = new Rectangle(column * square, row * square, square, square);
			if (squareBounds.intersects(clipBounds))
			{
				AffineTransform at = g2d.getTransform();
				g2d.translate(column * square, 0);
				g2d.setColor((isEven(row) && isEven(column)) || (isOdd(row) && isOdd(column)) ? lightGray :getSquareColor());
				g2d.fill(rectangle);
				g2d.translate(square, 0);
				g2d.setTransform(at);
			}
		}
		
		/**
		 * Uncomment to verify clipping testing. Reveals what is repainted. Areas that
		 * should not be repainted keep the colors.
		 *
		 */
		private Color getSquareColor()
		{
//			return new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
			return Color.WHITE;
		}

		private boolean isEven(int number)
		{
			return number % 2 == 0;
		}

		private boolean isOdd(int number)
		{
			return !isEven(number);
		}
	}

	private class FileDropListener extends DropTargetAdapter
	{
		@SuppressWarnings("unchecked")
		public void drop(DropTargetDropEvent dropTargetDropEvent)
		{
			File file = null;
			try {
				Transferable tr = dropTargetDropEvent.getTransferable();
				if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
					if (fileList.size() > 0) {
						file = fileList.get(0);
						URL url = file.toURI().toURL();
						SVGRasterizerTest.this.displaySVG(url);
						hyperLinkAction.putValue(Action.NAME, url.toExternalForm());
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete(true);
				} else {
					dropTargetDropEvent.rejectDrop();
				}
			} catch (TranscoderException e) {
				handleException(e, file);
			} catch (UnsupportedFlavorException e) {
				handleException(e, file);
			} catch (IOException e) {
				handleException(e, file);
			}
		}

		private void handleException(Exception e, File file)
		{
			SVGRasterizerTest.this.handleException(e, file);
		}

	}

}
