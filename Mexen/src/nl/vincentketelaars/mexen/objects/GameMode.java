package nl.vincentketelaars.mexen.objects;

public enum GameMode { 
	FREEPLAY("FreePlay", 0), 
	PLAYER("Player", 1), 
	LOCALGAME("LocalGame", 2);
	
	private String name;
	private int ordinal;
	
	private GameMode(String name, int ordinal) {
		this.name = name;
		this.ordinal = ordinal;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getOrdinal() {
		return this.ordinal;
	}
}