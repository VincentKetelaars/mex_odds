package nl.vincentketelaars.mexen.objects;

import java.util.Calendar;
import java.util.UUID;

public class Player implements Cloneable {

	private UUID id;
	private Calendar creationDate;
	private String name;
	private boolean local = false;

	/**
	 * Initial player construction
	 */
	public Player(UUID id, Calendar dateTime, String name, boolean local) {
		this.id = id;
		this.creationDate = (Calendar) dateTime.clone();
		this.name = name;
		this.local = local;
	}
	
	public static Player instantiatePlayer() {
		return new Player(UUID.randomUUID(), Calendar.getInstance(), null, true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getCreationTime() {
		return (Calendar) creationDate.clone();
	}

	public UUID getId() {
		return this.id;
	}
	
	public boolean isLocal() {
		return this.local;
	}
	
	public Player clone() {
		return new Player(this.id, this.creationDate, this.name, this.local);
	}

	public String toString() {
		return String.format("Player(%s, %s, %d, %b)", this.id.toString(), this.name, this.creationDate.getTimeInMillis(), this.local);
	}
}
