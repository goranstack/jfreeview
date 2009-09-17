import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Found at: http://forums.sun.com/thread.jspa?forumID=20&threadID=5190827
 *
 */
public class Pointers extends JPanel
{
	Rectangle r1 = new Rectangle(40, 60, 100, 150);
	Rectangle r2 = new Rectangle(200, 250, 175, 100);
	int barb = 20;
	double phi = Math.toRadians(20);

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(Color.blue);
		g2.draw(r1);
		g2.draw(r2);
		g2.setPaint(Color.red);
		g2.draw(getPath());
	}

	private GeneralPath getPath()
	{
		double x1 = r1.getCenterX();
		double y1 = r1.getCenterY();
		double x2 = r2.getCenterX();
		double y2 = r2.getCenterY();
		double theta = Math.atan2(y2 - y1, x2 - x1);
		Point2D.Double p1 = getPoint(theta, r1);
		Point2D.Double p2 = getPoint(theta + Math.PI, r2);
		GeneralPath path = new GeneralPath(new Line2D.Float(p1, p2));
		// Add an arrow head at p2.
		double x = p2.x + barb * Math.cos(theta + Math.PI - phi);
		double y = p2.y + barb * Math.sin(theta + Math.PI - phi);
		path.moveTo((float) x, (float) y);
		path.lineTo((float) p2.x, (float) p2.y);
		x = p2.x + barb * Math.cos(theta + Math.PI + phi);
		y = p2.y + barb * Math.sin(theta + Math.PI + phi);
		path.lineTo((float) x, (float) y);
		return path;
	}

	private Point2D.Double getPoint(double theta, Rectangle r)
	{
		double cx = r.getCenterX();
		double cy = r.getCenterY();
		double w = r.width / 2;
		double h = r.height / 2;
		double d = Point2D.distance(cx, cy, cx + w, cy + h);
		double x = cx + d * Math.cos(theta);
		double y = cy + d * Math.sin(theta);
		Point2D.Double p = new Point2D.Double();
		int outcode = r.outcode(x, y);
		switch (outcode)
		{
		case Rectangle.OUT_TOP:
			p.x = cx - h * ((x - cx) / (y - cy));
			p.y = cy - h;
			break;
		case Rectangle.OUT_LEFT:
			p.x = cx - w;
			p.y = cy - w * ((y - cy) / (x - cx));
			break;
		case Rectangle.OUT_BOTTOM:
			p.x = cx + h * ((x - cx) / (y - cy));
			p.y = cy + h;
			break;
		case Rectangle.OUT_RIGHT:
			p.x = cx + w;
			p.y = cy + w * ((y - cy) / (x - cx));
			break;
		default:
			System.out.println("Non-cardinal outcode: " + outcode);
		}
		return p;
	}

	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new Pointers());
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}
}
