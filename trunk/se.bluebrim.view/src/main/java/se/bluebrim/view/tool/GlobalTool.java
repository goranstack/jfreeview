package se.bluebrim.view.tool;

public interface GlobalTool
{

	/**
	 * Called when a move is entering different view panel from the view panel
	 * where the move started.
	 */
	public abstract void changeToolDispatcher(ToolDispatcher newToolDispatcher);

}