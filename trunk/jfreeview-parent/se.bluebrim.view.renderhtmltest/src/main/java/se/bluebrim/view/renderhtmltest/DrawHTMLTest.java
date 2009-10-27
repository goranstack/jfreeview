package se.bluebrim.view.renderhtmltest;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class DrawHTMLTest
{
	static private final JComponent component = new JLabel();
	

	public static void main(String[] args) throws Exception
	{
		new DrawHTMLTest().run();

	}

	public void run() throws Exception
	{
		component.putClientProperty(BasicHTML.documentBaseKey, getClass().getResource("test.html"));
		String html = loadHTML();
		System.out.println("HTML: \"" + html + "\"");
		View htmlView = BasicHTML.createHTMLView(component, html);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new HTMLPanel(htmlView), BorderLayout.CENTER);
		frame.setSize(700, 700);
		frame.setLocation(100, 100);
		frame.setVisible(true);
	}
	
	private String loadHTML() throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("test.html")));
		StringBuffer fileData = new StringBuffer(1000);
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1)
		{
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();			
	}



	private static class HTMLPanel extends JPanel
	{
		private View htmlView;
		
		public HTMLPanel(View htmlView)
		{
			super();
			this.htmlView = htmlView;
		}

		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			drawTextView((Graphics2D) g, htmlView, new Insets(0, 0 ,0, 0));
		}
		
		private void drawTextView(Graphics2D g, javax.swing.text.View htmlView, Insets textInsets)
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Rectangle2D bounds = getBounds();
			Shape restoreClip = g.getClip();
			g.clip(getBounds()); // fix NPE in javax.swing.text.BoxView.paint when dragging      
			htmlView.paint(g, new Rectangle((int) (bounds.getX() + textInsets.left), (int) (bounds.getY() + textInsets.top),
					(int) (bounds.getWidth() - (textInsets.left + textInsets.right)), (int) (bounds.getHeight() - (textInsets.top + textInsets.bottom))));
			g.setClip(restoreClip);
		}

	}
	
}
