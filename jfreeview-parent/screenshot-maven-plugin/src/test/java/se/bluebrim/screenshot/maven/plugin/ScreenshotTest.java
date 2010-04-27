package se.bluebrim.screenshot.maven.plugin;

import java.awt.Point;

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
		JPanel panel = new JPanel();
		addComponents(panel);
		button.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, new RedFrameDecorator());
		return panel;
	}
	
	@Screenshot (targetClass=CalloutDecorator.class)
	public JPanel createCalloutDecoratorPanel()
	{
		JPanel panel = new JPanel();
		addComponents(panel);
		textField.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, new CalloutDecorator(2, new Point(20, -8)));
		return panel;
	}
	
	@Screenshot (targetClass=CalloutDecorator.class, scene="transparent")
	public JPanel createCalloutDecoratorBlackPanel()
	{
		JPanel panel = createCalloutDecoratorPanel();
		panel.setOpaque(false);
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
		window.getContentPane().add(panel);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);		
	}

}
