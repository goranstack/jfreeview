package se.bluebrim.view.transaction;


/**
 * Implemented by objects that is stored in a database and must be modified
 * in the context of a transaction to guarantee atomicity, consistency, isolation, and durability
 * <a href=http://en.wikipedia.org/wiki/ACID>(ACID)</a> of the performed modifications.<br>
 * Implementors should implement a getWritable-method that takes a Transaction as argument and
 * returns a writable isolated instance.<br>
 * TransactionalObject's has an extra set method's for each property that takes an additional
 * transaction argument. When that kind of set method is called the modification is performed
 * by a transaction. The set method without transaction argument should only be used internally
 * by the transaction.
 *  
 * 
 * @author G Stack
 */
public interface TransactionalObject
{
	public class InvalidStateException extends RuntimeException
	{
		public InvalidStateException(String message)
		{
			super(message);
		}
		
	}
	/**
	 * Return an object that identifies the TransactionalObject in the transaction context.
	 * Used to find the target object of a command since we can't use object reference when
	 * the transaction make copies of target objects to accomplish isolation.
	 */
	public Object getOid();
	
	/**
	 * Return a writable isolated version of the TransactionalObject. Isolated
	 * means that the object is isolated from changes by other transactions.
	 */
	public TransactionalObject getWritable(Transaction transaction);
	
	/**
	 * Updates are sent to database but we have no response from database yet
	 * confirming that the requsted updates has been stored in the database.
	 * This property are used by views to visualize this state for the user for
	 * example by using striped fill colors instead of solid colors.
	 */
	public boolean hasUnconfirmedChanges();
	
	/**
	 * Set to true by transactions after sending an update request to the database and
	 * set to false by transactions after receiving database response.
	 */
	public void setHasUnconfirmedChanges(boolean hasUnconfirmedChanges);
	
	/**
	 * @deprecated Can't handle structural changes of views for example
	 * parent switching in drag and drop operations. <br>
	 * Use addTransactionAbortedListener instead to rebuild the entire
	 * view structure when needed.
	 *
	 */
	public void notifiyViewsOutOfSync();
	
	public void validate() throws InvalidStateException;
	 
}
