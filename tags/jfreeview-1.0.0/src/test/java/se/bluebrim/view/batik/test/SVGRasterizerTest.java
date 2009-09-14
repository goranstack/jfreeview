package se.bluebrim.view.batik.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.batik.transcoder.TranscoderException;

import se.bluebrim.view.batik.SVGRasterizer;

public class SVGRasterizerTest
{

	public static void main(String[] args) throws TranscoderException
	{
      SVGRasterizer rasterizer = new SVGRasterizer(SVGRasterizerTest.class.getResource("test.svg"));
      rasterizer.setBackgroundColor(Color.WHITE);
      BufferedImage img = rasterizer.createBufferedImage();
      javax.swing.JFrame f = new javax.swing.JFrame();
      f.getContentPane().add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);  f.pack();
      f.setVisible(true);
 
	}

}
