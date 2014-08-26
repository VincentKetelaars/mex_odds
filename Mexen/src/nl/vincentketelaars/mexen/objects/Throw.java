package nl.vincentketelaars.mexen.objects;

import java.util.Calendar;
import java.util.UUID;

public class Throw implements Cloneable {
	
	private int[] n;
	private Calendar dateTime;
	private UUID id;
	
	public Throw(int... n) {
		this.n = n;
		this.dateTime = Calendar.getInstance(); // Default locale
		this.id = UUID.randomUUID();
		purge();
	}
	
	public Throw(UUID id, long timeMillis, int... n) {
		this.n = n;
		this.dateTime = Calendar.getInstance();
		this.dateTime.setTimeInMillis(timeMillis);
		this.id = id;
		purge();
	}
	
	public int getNumberOne() {
		return this.n[0];
	}
	
	public int getNumberTwo() {
		return this.n[1];
	}
	
	public int getNumberThree() {
		if (this.n.length < 3)
			return 0;
		return this.n[2];
	}
	
	public int numDice() {
		return this.n.length;
	}
	
	public Calendar getDateTime() {
		return (Calendar) this.dateTime.clone();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Throw(");
		for (int n : this.n)
			sb.append(n + ", ");
		sb.setLength(sb.length() - 2);
		sb.append(")");
		return sb.toString();
	}
	
	/*
	 * Ensure that all dice values are valid. Remove any invalid (i.e. create new array)
	 */
	private void purge() {
		int length = 0;
		for (int i : this.n) {
			if (i > 0 && i <= 6)
				length++;
		}
		int n[] = new int[length];
		int index = 0;
		for (int i : this.n) {
			if (i > 0 && i <= 6) {
				n[index] = i;
				index++;
			}
		}		
		this.n = n;
	}
	
	public Throw clone() {
		Throw t = new Throw(this.n);
		t.dateTime = (Calendar) this.getDateTime();
		return t;
	}

	public UUID getId() {
		return id;
	}
}
