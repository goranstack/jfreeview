package se.bluebrim.example.svgsalamander;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import se.bluebrim.view.example.svg.resource.SVGSampleProvider;

import com.kitfox.svg.app.beans.SVGPanel;

/**
 *
 * 
 * 
 * @author Goran Stack
 * 
 */
public class SVGSalamanderExample
{
	private JFrame window;
	private SVGSampleProvider svgSamples;
	private SVGPanel svgPanel;


	public static void main(String[] args) throws MalformedURLException
	{
		new SVGSalamanderExample().run();
	}

	private void run() throws MalformedURLException
	{
		svgSamples = new SVGSampleProvider();
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("JSVGCanvas Example");
		window.setIconImage(new ImageIcon(getClass().getResource("jfreeview-logo-32x32.png")).getImage());
		Container contentPane = window.getContentPane();
		Component originatorBar = svgSamples.createOriginatorBar();
		JToolBar toolBar = createToolBar();
		svgPanel = new SVGPanel();
		svgPanel.setAntiAlias(true);
		contentPane.add(createContentPane());
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(originatorBar, BorderLayout.SOUTH);
		displayNextSample();
		window.setSize(1000, 800);
		window.setLocationRelativeTo(null); // Center on screen
		window.setVisible(true);
	}

	private Component createContentPane()
	{
		return svgPanel;
	}

	private JToolBar createToolBar()
	{
		JToolBar toolBar = svgSamples.createNavigatorToolbar(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SVGSalamanderExample.this.displayNextSample();
			}
		});
		return toolBar;
	}

	protected void displayNextSample()
	{
		URL resource = svgSamples.next().getResource();
		try
		{
			svgPanel.setSvgURI(resource.toURI());
		} catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		svgPanel.repaint();
	}

}
