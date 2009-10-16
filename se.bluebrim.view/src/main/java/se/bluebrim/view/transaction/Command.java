package se.bluebrim.view.transaction;

/**
 * Implemented by command objects. A command is an object that represents a modification
 * of database object
 * 
 * @author G Stack
 */
public interface Command
{
	public interface CompletedListener
	{
		public void completed();
	}
	
	public abstract void execute(Transaction transaction);

	public abstract void execute(Transaction transaction, CompletedListener completedListener);

	/**
	 * Return the target object for the command. This is not the writable version of the
	 * object and this method is only used by the transaction to make a writable version of
	 * the target object before calling the execute method of the Command
	 */
	public TransactionalObject getTargetObject();

	public String getName();

}