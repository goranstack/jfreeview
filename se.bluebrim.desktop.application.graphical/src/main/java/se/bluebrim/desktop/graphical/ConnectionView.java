package se.bluebrim.desktop.graphical;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import se.bluebrim.view.ConnectableView;
import se.bluebrim.view.ParentView;
import se.bluebrim.view.impl.AbstractView;
import se.bluebrim.view.impl.BasicView;
import se.bluebrim.view.impl.SlaveView;
import se.bluebrim.view.model.PropertyChangeNotifier;
import se.bluebrim.view.paint.Paintable;

/**
 * View that draw a line between two other views
 * 
 * @author GStack
 * 
 */
public class ConnectionView extends SlaveView
{
	public enum ArrowHeadOption{START, END, BOTH};
	
	private static final float CONNECTION_LINE_GAP = 10;
	private ConnectableView node1;
	private ConnectableView node2;
	private Color lineColor;
	private Shape shape;
	private Shape arrowHead;
	private ParentView lowestCommonAncestor;

	public ConnectionView(ConnectableView node1, ConnectableView node2, Color lineColor)
	{
		this(node1, node2, lineColor, ArrowHeadOption.END);
	}
	
	public ConnectionView(ConnectableView node1, ConnectableView node2, Color lineColor, final ArrowHeadOption arrowHeadOption)
	{
		super((AbstractView) node1);
		if (node1 == node2)
			throw new IllegalArgumentException("A node can't be connected to it self");
		this.node1 = node1;
		this.node2 = node2;
		this.lineColor = lineColor;
		lowestCommonAncestor = getLowestCommonAncestor(node1, node2);
		lowestCommonAncestor.addChild(this);
		PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				calculateShape(arrowHeadOption);
			}
		};

		listenToAllCenterPointChanges(node1, propertyChangeListener);
		listenToAllCenterPointChanges(node2, propertyChangeListener);
		calculateShape(arrowHeadOption);
	}

	private void listenToAllCenterPointChanges(ConnectableView node, PropertyChangeListener propertyChangeListener)
	{
		listenToCenterPointChanges(node, propertyChangeListener);
		List<ParentView> parentChain = node.getParentChain();
		for (ParentView parentView : parentChain)
		{
			if (parentView.equals(lowestCommonAncestor))
				return;
			else
				listenToCenterPointChanges(parentView, propertyChangeListener);
		}
	}

	private void listenToCenterPointChanges(PropertyChangeNotifier node, PropertyChangeListener propertyChangeListener)
	{
		node.addPropertyChangeListener(BasicView.LOCATION, propertyChangeListener);
		node.addPropertyChangeListener(BasicView.HEIGHT, propertyChangeListener);
		node.addPropertyChangeListener(BasicView.WIDTH, propertyChangeListener);
	}

	/**
	 * See: http://en.wikipedia.org/wiki/Lowest_common_ancestor
	 */
	private ParentView getLowestCommonAncestor(ConnectableView node1, ConnectableView node2)
	{
		List<ParentView> chain1 = node1.getParentChain();
		List<ParentView> chain2 = node2.getParentChain();
		chain1.retainAll(chain2);
		if (chain1.isEmpty())
			throw new IllegalArgumentException("The nodes lacks common ancestor");
		return chain1.get(0);
	}

	@Override
	public Shape getOpticalShape()
	{
		return shape;
	}

	@Override
	public void paint(Paintable g)
	{
		g.setColor(lineColor);
		if (shape == null)
			throw new RuntimeException("Shape must not be null");
		g.fill(shape);
	}

	/**
	 * Extend the shape that is connected to achieve a little gap between the connection line and the
	 * shape.
	 */
	private Shape getExtendedShape(ConnectableView node)
	{
		Stroke stroke = new BasicStroke(CONNECTION_LINE_GAP);
		Shape transformedShape = getTransformedShape(node);
		Area area = new Area(stroke.createStrokedShape(transformedShape));
		area.add(new Area(transformedShape));
		return area;
	}

	private Shape getTransformedShape(ConnectableView node)
	{
		return node.getTransformFor(lowestCommonAncestor).createTransformedShape(node.getConnectionShape());
	}

	/**
	 * See http://forums.sun.com/thread.jspa?threadID=5337340 Got the idea of using a stroked line
	 * from: http://x86.sun.com/thread.jspa?messageID=9499813 A library containing a arrow head
	 * http://sourceforge.net/projects/desl/
	 */
	private void calculateShape(ArrowHeadOption arrowHeadOption)
	{
		Stroke stroke = new BasicStroke(1);
		Point2D arrowTarget1 = getCenterRelative(node1);
		Point2D arrowTarget2 = getCenterRelative(node2);
		Shape strokedLine = stroke.createStrokedShape(new Line2D.Double(arrowTarget1, arrowTarget2));
		Area lineArea = new Area(strokedLine);
		lineArea.subtract(new Area(getExtendedShape(node1)));
		lineArea.subtract(new Area(getExtendedShape(node2)));
		if (arrowHeadOption == ArrowHeadOption.START || arrowHeadOption == ArrowHeadOption.BOTH)
			appendArrowHead(arrowTarget1, lineArea);
		if (arrowHeadOption == ArrowHeadOption.END || arrowHeadOption == ArrowHeadOption.BOTH)
		appendArrowHead(arrowTarget2, lineArea);
		shape = lineArea;
	}

	private void appendArrowHead(Point2D arrowTarget, Area lineArea)
	{
		// Find the point in the path nearest to the arrow target
		Point2D nearestPoint = new Point2D.Float(Float.MAX_VALUE, Float.MAX_VALUE);
		float[] coords = new float[6];
		PathIterator points = lineArea.getPathIterator(null);
		while (!points.isDone())
		{
			int segtype = points.currentSegment(coords);
			switch (segtype)
			{
			case PathIterator.SEG_CLOSE:
				findNearestPoint(coords, arrowTarget, nearestPoint);
				break;

			case PathIterator.SEG_CUBICTO:
				findNearestPoint(coords, arrowTarget, nearestPoint);
				break;

			case PathIterator.SEG_LINETO:
				findNearestPoint(coords, arrowTarget, nearestPoint);
				break;

			case PathIterator.SEG_MOVETO:
				findNearestPoint(coords, arrowTarget, nearestPoint);
				break;

			case PathIterator.SEG_QUADTO:
				findNearestPoint(coords, arrowTarget, nearestPoint);
				break;
			}
			points.next();
		}

		arrowHead = createEquilateralTriangle(nearestPoint.getX(), nearestPoint.getY(), 13, calcAngle(nearestPoint,
				arrowTarget));
		lineArea.add(new Area(arrowHead));
	}

	private Point2D getCenterRelative(ConnectableView node)
	{
		return node.getTransformFor(lowestCommonAncestor).transform(node.getCenter(), null);
	}

	private void findNearestPoint(float[] coords, Point2D target, Point2D nearestSoFar)
	{
		Point2D.Double testPoint = new Point2D.Double(coords[0], coords[1]);
		double testDistance = testPoint.distance(target);
		double minDist = nearestSoFar.distance(target);
		if (testDistance < minDist)
		{
			nearestSoFar.setLocation(testPoint.getX(), testPoint.getY());
		}
	}

	private static double calcAngle(Point2D p1, Point2D p2)
	{
		double x_off = p2.getX() - p1.getX();
		double y_off = p2.getY() - p1.getY();
		return Math.atan2(y_off, x_off);
	}

	private Shape createEquilateralTriangle(double x, double y, float size, double angle)
	{
		// System.out.println("Angle: " + angle);
		double height = Math.sqrt(3) / 2;
		Point2D v1 = new Point2D.Double(-height, 0.4);
		Point2D v2 = new Point2D.Double(-height, -0.4);
		Point2D v3 = new Point2D.Double(0, 0);
		GeneralPath triangle = new GeneralPath();
		triangle.append(new Line2D.Float(v2, v1), false);
		triangle.append(new Line2D.Float(v2, v3), true);
		triangle.append(new Line2D.Float(v3, v1), true);
		AffineTransform at = new AffineTransform();
		at.translate(x, y);
		at.rotate(angle);
		at.translate(CONNECTION_LINE_GAP / 6, 0); // Make sure the line end is completely covered by
																// the arrow.
		at.scale(size, size);

		return at.createTransformedShape(triangle);
	}

}