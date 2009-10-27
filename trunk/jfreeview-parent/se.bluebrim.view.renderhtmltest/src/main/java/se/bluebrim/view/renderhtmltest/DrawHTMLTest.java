package se.bluebrim.view.renderhtmltest;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import se.bluebrim.view.swing.HTMLPanel;

public class DrawHTMLTest
{
	public static void main(String[] args) throws Exception
	{
		new DrawHTMLTest().run();
	}

	public void run() throws Exception
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new BorderLayout());
		HTMLPanel htmlPanel = new HTMLPanel(getClass().getResource("/test.html"), HTMLPanel.createOpenInBrowserHyperlinkListener());
		contentPane.add(new JScrollPane(htmlPanel), BorderLayout.CENTER);
		frame.setSize(700, 800);
		frame.setLocation(100, 100);
		frame.setVisible(true);
	}
	

		

	
}
