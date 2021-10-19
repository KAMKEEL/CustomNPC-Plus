package noppes.npcs.scripted.event;

public 
class ScriptEvent{
	private boolean isCancelled = false;
	
	public void setCancelled(boolean bo){
		isCancelled = bo;
	}
	
	public boolean isCancelled(){
		return isCancelled;
	}
}
