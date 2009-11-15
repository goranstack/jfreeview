package se.bluebrim.example.svgcanvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGScrollPane;
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractRotateInteractor;
import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import se.bluebrim.view.example.svg.resource.SVGSampleProvider;

/**
 * Desktop application with one single JSVGCanvas browsing a series of SVG-files
 * The icons for the tool bar was found at: http://www.iconfinder.net <br>
 * The purpose for this class is to learn how to include SVG graphics into
 * Swing components. It's not obvious how this is done with the Batik framework.
 * Here are some links to investigate further on this matter: <br>
 * http://en.wikipedia.org/wiki/Scalable_Vector_Graphics<br>
 * https://svgsalamander.dev.java.net/ <br>
 * http://www.jroller.com/aalmiray/entry/svgicons_with_jidebuilder <br>
 * http://weblogs.java.net/blog/kirillcool/archive/2006/08/svgbased_resiza_1.html <br>
 * http://weblogs.java.net/blog/kirillcool/archive/2006/09/svg_and_java_ui_1.html <br>
 * https://scalableicons.dev.java.net/ <br>
 * 
 * 
 * @author Goran Stack
 * 
 */
public class SVGCanvasExample {
	private JFrame window;
	private SVGSampleProvider svgSamples;
	private CustomInteractorsSVGCanvas canvas;
	private JToggleButton moveTool;
	private JToggleButton rotateTool;
	private JToggleButton zoomTool;
	private RSyntaxTextArea sourceTextArea;

	public static void main(String[] args) throws MalformedURLException {
		new SVGCanvasExample().run();
	}

	private void run() throws MalformedURLException {
		svgSamples = new SVGSampleProvider();
		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setTitle("JSVGCanvas Example");
		window.setIconImage(new ImageIcon(getClass().getResource(
				"jfreeview-logo-32x32.png")).getImage());
		Container contentPane = window.getContentPane();
		Component originatorBar = svgSamples.createOriginatorBar();
		JToolBar toolBar = createToolBar();
		canvas = new CustomInteractorsSVGCanvas();
		canvas.replaceInteractors();
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
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Graphics", new JSVGScrollPane(canvas));
		sourceTextArea = new RSyntaxTextArea();
		sourceTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		sourceTextArea.setEditable(false);
		tabbedPane.add("Source", new RTextScrollPane(sourceTextArea));
		return tabbedPane;
	}

	private JToolBar createToolBar() {
		JToolBar toolBar = svgSamples
				.createNavigatorToolbar(new AbstractAction() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SVGCanvasExample.this.displayNextSample();
					}
				});
		ButtonGroup buttonGroup = new ButtonGroup();
		toolBar.add(moveTool = createToolBarButton(buttonGroup, "move.png"));
		toolBar
				.add(rotateTool = createToolBarButton(buttonGroup, "rotate.png"));
		toolBar.add(zoomTool = createToolBarButton(buttonGroup, "zoom.png"));
		moveTool.setSelected(true);
		return toolBar;
	}

	private JToggleButton createToolBarButton(ButtonGroup buttonGroup, String iconName) {
		JToggleButton button = new JToggleButton(new ImageIcon(getClass().getResource(
				iconName)));
		buttonGroup.add(button);
		button.setContentAreaFilled(true);
		button.setMargin(new Insets(0, 0, 0, 0));
		return button;
	}

	protected void displayNextSample() {
		URL resource = svgSamples.next().getResource();
		try {
			canvas.setURI(resource.toURI().toString());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		try {
			sourceTextArea.setText(new JEditorPane(resource).getText());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * To change the way the interactors are triggered we have to replace
	 * the already created interactors with new ones redefining the startInteraction
	 * method. Since there are no mutator methods in JSVGCanvas for this purpose
	 * we to subclass JSVGCanvas.
	 *
	 */
	private class CustomInteractorsSVGCanvas extends JSVGCanvas {
		private void replaceInteractors() {
			List interactors = getInteractors();
			replaceZoomInteractor(interactors);
			replaceImageZoomInteractor(interactors);
			replacePanInteractor(interactors);
			replaceRotateInteractor(interactors);
		}

		private void replaceZoomInteractor(List interactors) {
			interactors.remove(zoomInteractor);
			zoomInteractor = new AbstractZoomInteractor() {
				public boolean startInteraction(InputEvent ie) {
		            int mods = ie.getModifiers();
		            return
		                ie.getID() == MouseEvent.MOUSE_PRESSED &&
		                (mods & InputEvent.BUTTON1_MASK) != 0 &&
		                !((mods & InputEvent.SHIFT_MASK) != 0) &&
		                zoomTool.isSelected();
				}
			};
			interactors.add(zoomInteractor);
		};
		
		private void replacePanInteractor(List interactors) {
			interactors.remove(panInteractor);
			panInteractor = new AbstractPanInteractor() {
				public boolean startInteraction(InputEvent ie) {
		            int mods = ie.getModifiers();
		            return
		                ie.getID() == MouseEvent.MOUSE_PRESSED &&
		                (mods & InputEvent.BUTTON1_MASK) != 0 &&
		                moveTool.isSelected();
				}
			};
			interactors.add(panInteractor);
		};

		private void replaceRotateInteractor(List interactors) {
			interactors.remove(rotateInteractor);
			rotateInteractor = new AbstractRotateInteractor() {
				public boolean startInteraction(InputEvent ie) {
		            int mods = ie.getModifiers();
		            return
		                ie.getID() == MouseEvent.MOUSE_PRESSED &&
		                (mods & InputEvent.BUTTON1_MASK) != 0 &&
		                rotateTool.isSelected();
				}
			};
			interactors.add(rotateInteractor);
		};
		
		private void replaceImageZoomInteractor(List interactors) {
			interactors.remove(imageZoomInteractor);
			imageZoomInteractor = new AbstractImageZoomInteractor() {
				public boolean startInteraction(InputEvent ie) {
		            int mods = ie.getModifiers();
		            return
		                ie.getID() == MouseEvent.MOUSE_PRESSED &&
		                (mods & InputEvent.BUTTON1_MASK) != 0 &&
		                (mods & InputEvent.SHIFT_MASK) != 0 &&
		                zoomTool.isSelected();
				}
			};
			interactors.add(imageZoomInteractor);
		};


	}

}
