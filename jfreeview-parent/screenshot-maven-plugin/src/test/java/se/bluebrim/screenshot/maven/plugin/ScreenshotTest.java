package se.bluebrim.screenshot.maven.plugin;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ScreenshotTest {

	@Screenshot (targetClass=RedFrameDecorator.class)
	public JComponent createRedFrameDecoratorPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel("Hello world"));
		JTextField textField = new JTextField();
		textField.setColumns(10);
		panel.add(textField);
		JButton button = new JButton("Press me");
		button.putClientProperty(ScreenshotDecorator.CLIENT_PROPERTY_KEY, new RedFrameDecorator());
		panel.add(button);
		return panel;
	}
}
