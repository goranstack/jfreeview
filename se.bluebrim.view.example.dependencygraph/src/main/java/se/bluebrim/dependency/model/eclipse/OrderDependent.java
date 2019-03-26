package se.bluebrim.dependency.model.eclipse;

/**
 * Interface for objects whose order with respect to other objects is dependent on prerequisites being satisfied.
 * 
 * PENDING: Find a good name for this interface and rewrite the comment above!
 * 
 * @author Markus Persson
 */
public interface OrderDependent
{
	/**
	 * Returns a dependency key, normally a string, which could be used by other nodes being dependant on this one. The
	 * keys are compared using the equals() and hashCode() contracts.
	 * 
	 * Null may not be returned!
	 */
	public Object getDependencyKey();

	/**
	 * Returns an array of dependency keys for other order dependent nodes which is prerequisites for this one. Null (or
	 * an empty array) may be returned if this node has no prerequisites. The keys are compared using the equals() and
	 * hashCode() contracts.
	 */
	public Object[] getPrerequisites();
}
