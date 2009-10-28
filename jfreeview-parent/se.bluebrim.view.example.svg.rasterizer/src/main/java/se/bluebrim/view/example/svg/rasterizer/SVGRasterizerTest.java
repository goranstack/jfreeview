package se.bluebrim.view.example.svg.rasterizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.batik.transcoder.TranscoderException;

import se.bluebrim.view.batik.SVGRasterizer;

/**
 * TODO:
 * <ul>
 * <li>Add background square pattern like Photoshop and make sure the transparent elements
 * in the SVG are rendered correctly </li>
 * <li>Let the user try other SVG files by dropping them in to the window </li> 
 * </ul>
 * @author Goran Stack
 *
 */
public class SVGRasterizerTest
{

	public static void main(String[] args) throws TranscoderException
	{
      SVGRasterizer rasterizer = new SVGRasterizer(SVGRasterizerTest.class.getResource("test.svg"));
      rasterizer.setBackgroundColor(Color.WHITE);
      BufferedImage img = rasterizer.createBufferedImage();
      JFrame window = new JFrame();
      window.getContentPane().add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
      window.setSize(400, 400);
      window.setLocation(200, 200);
      window.setVisible(true);
	}

}
