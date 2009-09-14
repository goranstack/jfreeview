package se.bluebrim.view.transaction;



/**
 * Abstract superclass to objects that performs updates of database objects.
 * A Command contains all state necessary to perform the change and update of
 * the database object.
 * 
 * @author G Stack
 */
public abstract class AbstractCommand implements Command
{
	private TransactionalObject targetObject;
	
	public AbstractCommand(TransactionalObject targetObject)
	{
		this.targetObject = targetObject;
	}

	public TransactionalObject getTargetObject()
	{
		return targetObject;
	}

	/**
	 * Return the class name without package name. Perhaps later on we will
	 * have separate name field provided as an argument in the constructor.
	 */
	public String getName()
	{
		String className = getClass().getName();
		return className.substring(className.lastIndexOf(".") + 1);
	}
		

	public void execute(Transaction transaction, CompletedListener completedListener)
	{
		execute(transaction);
		completedListener.completed();
	}
	
}
