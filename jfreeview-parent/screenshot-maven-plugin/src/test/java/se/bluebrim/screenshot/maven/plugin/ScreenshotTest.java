package se.bluebrim.screenshot.maven.plugin;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXFrame;

public class ScreenshotTest {
	
	private JTextField textField;
	private JButton button;

	
	public ScreenshotTest() 
	{
	}


	private void addComponents(JPanel panel) 
	{
		panel.add(new JLabel("Hello world"));
		textField = new JTextField();
		textField.setColumns(10);
		panel.add(textField);
		button = new JButton("Press me");
		panel.add(button);
	}
	
	
	@Screenshot (targetClass=RedFrameDecorator.class)
	public JComponent createRedFrameDecoratorPanel()
	{
		JPanel panel = new DecoratedPanel();
		addComponents(panel);
		button.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, new RedFrameDecorator());
		return panel;
	}
	
	@Screenshot (targetClass=CalloutDecorator.class)
	public JPanel createCalloutDecoratorPanel()
	{
		JPanel panel = new DecoratedPanel();
		addComponents(panel);
		textField.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, new CalloutDecorator(8));
		return panel;
	}
	
	@Screenshot (targetClass=CalloutDecorator.class, scene="transparent")
	public JPanel createCalloutDecoratorTransparentPanel()
	{
		JPanel panel = createCalloutDecoratorPanel();
		panel.setOpaque(false);
		return panel;
	}
	
	@Screenshot (targetClass=CompositeDecorator.class)
	public JPanel createCompositeDecoratorPanel()
	{
		JPanel panel = new DecoratedPanel();
		JButton comp = new JButton("Button");
		panel.add(comp);
		CompositeDecorator composite = new CompositeDecorator();
		composite.add(new TextDecorator("left", new LeftSide()));
		composite.add(new TextDecorator("right", new RightSide()));
		composite.add(new TextDecorator("top", new Top()));
		composite.add(new TextDecorator("bottom", new Bottom()));
		composite.add(new CalloutDecorator(1, new BottomLeftCorner()));
		comp.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, composite);

		return panel;
	}
	
	
	
	public static void main(String[] args) {
		final ScreenshotTest instance = new ScreenshotTest();
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				instance.openInWindow();
			}
		});
	}

	protected void openInWindow()
	{
		JXFrame window = new JXFrame(getClass().getSimpleName(), true);
		JPanel panel = new JPanel();
		panel.add(createRedFrameDecoratorPanel());
		panel.add(createCalloutDecoratorPanel());
		panel.add(createCompositeDecoratorPanel());
		window.getContentPane().add(panel);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);		
	}
	
	private static class DecoratedPanel extends JPanel
	{
		@Override
		public void paint(Graphics g) 
		{
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setClip(null);
			ScreenshotScanner.decorateScreenshot(this, g2d);
		}
	}

}
