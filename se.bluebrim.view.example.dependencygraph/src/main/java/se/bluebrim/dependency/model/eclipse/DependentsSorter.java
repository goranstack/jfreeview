package se.bluebrim.dependency.model.eclipse;

import java.util.*;

/**
 * Helper class for sorting dependents such as objects only are dependent on previous object in the collection. For
 * example if C depends on B that depends on A the order will A, B and C. The class is used for ordering the path
 * elements in the required project element in the <code>build_classpath.xml</code> file. <br>
 * To use the class call the <code>orderDependents</code> method with an iterator containing objects implementing the
 * <code>OrderDependent</code> and <code>Comparable</code> interfaces. The return value is a <code>Collection</code>
 * with the ordered objects.
 * 
 * @author Markus Persson, Göran Stäck
 */
public class DependentsSorter
{
	public static class CycleException extends Exception
	{
		private CycleException(String message)
		{
			super(message);
		}
	}

	private static class GraphNode
	{
		private static final int UNVISITED = 0;
		private static final int IN_PROGRESS = 1;
		private static final int DONE = 2;

		private Object m_payload;
		private Object[] m_prerequisites;
		private int m_state = UNVISITED;
		// Initialized just to cause ArrayIndexOutOfBoundsException ...
		private int m_seqNum = -1;

		public GraphNode(OrderDependent payload)
		{
			this(payload, payload.getPrerequisites());
		}

		public GraphNode(Object payload, Object[] prerequisites)
		{
			m_payload = payload;
			m_prerequisites = prerequisites;
		}

		public boolean unvisited()
		{
			return m_state == UNVISITED;
		}

		public boolean inProgress()
		{
			return m_state == IN_PROGRESS;
		}

		public Object getPayload()
		{
			return m_payload;
		}

		public int getSequenceNumber()
		{
			return m_seqNum;
		}

		public int numberFrom(int nextSeqNum, Map nodeByKey) throws CycleException
		{
			m_state = IN_PROGRESS;
			if (m_prerequisites != null)
			{
				for (int i = 0; i < m_prerequisites.length; ++i)
				{
					GraphNode node = (GraphNode)nodeByKey.get(m_prerequisites[i]);
					if (node != null)
					{
						if (node.inProgress())
						{
							throw new CycleException("From " + m_payload + " to " + node.getPayload());
						}
						if (node.unvisited())
						{
							nextSeqNum = node.numberFrom(nextSeqNum, nodeByKey);
						}
					}
				}
			}
			// No more prerequisites
			m_state = DONE;
			m_seqNum = nextSeqNum;

			return nextSeqNum + 1;
		}
	}

	/**
	 * Note: For this to work it is required that the elements returned by the iterator <code>dependents</code> is an
	 * instance of OrderDependent. That is, the statement <br/><code>element instance of OrderDependent</code> <br/>
	 * should evaluate to true for every element.
	 */
	public static Collection orderDependents(Iterator dependents) throws CycleException
	{
		Map nodeByKey = new HashMap();
		List nodes = new ArrayList();

		while (dependents.hasNext())
		{
			OrderDependent dependent = (OrderDependent)dependents.next();
			GraphNode node = new GraphNode(dependent, dependent.getPrerequisites());
			nodeByKey.put(dependent.getDependencyKey(), node);
			nodes.add(node);
		}

		// Number all nodes
		Object[] ordered = new Object[nodes.size()];
		int nextSeqNum = 0;
		Iterator nodeIter = nodes.iterator();
		while (nodeIter.hasNext())
		{
			GraphNode node = (GraphNode)nodeIter.next();
			if (node.unvisited())
			{
				nextSeqNum = node.numberFrom(nextSeqNum, nodeByKey);
			}
			ordered[node.getSequenceNumber()] = node.getPayload();
		}

		return Arrays.asList(ordered);
	}

}
