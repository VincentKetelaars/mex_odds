package nl.vincentketelaars.mexen.objects;

import java.util.Calendar;
import java.util.UUID;

public class Throw implements Cloneable {
	
	private int[] n;
	private boolean[] v;
	private Calendar dateTime;
	private UUID id;
	
	public Throw(boolean[] v, int... n) {
		this.n = n;
		this.v = v;
		this.dateTime = Calendar.getInstance(); // Default locale
		this.id = UUID.randomUUID();
		purgeArray();
	}
	
	public Throw(UUID id, long timeMillis, boolean[] v, int... n) {
		this.n = n;
		this.v = v;
		this.dateTime = Calendar.getInstance();
		this.dateTime.setTimeInMillis(timeMillis);
		this.id = id;
		purgeArray();
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
	
	public boolean getVast(int dice) {
		if (dice >= 0 && dice < this.v.length) {
			return this.v[dice];
		}
		return false;
	}
	
	public void setVast(int dice, boolean vast) {
		if (dice >= 0 && dice < this.v.length)
			this.v[dice] = vast;
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
		sb.append(this.id.toString() + ", ");
		sb.append(Long.toString(this.dateTime.getTimeInMillis()) + ", ");
		for (int i = 0; i < this.n.length; i++)
			sb.append(this.n[i] + " " + this.v[i] + ", ");
		sb.setLength(sb.length() - 2);
		sb.append(")");
		return sb.toString();
	}
	
	/*
	 * Ensure that all dice values are valid. Remove any invalid (i.e. create new array)
	 */
	private void purgeArray() {
		int length = 0;
		for (int i : this.n) {
			if (i > 0 && i <= 6)
				length++;
		}
		int n[] = new int[length];
		boolean v[] = new boolean[length];
		int index = 0;
		for (int i : this.n) {
			if (i > 0 && i <= 6) {
				n[index] = i;
				v[index] = this.v[index];
				index++;
			}
		}		
		this.n = n;
		this.v = v;
	}
	
	public Throw clone() {
		return new Throw(this.id, this.getDateTime().getTimeInMillis(), this.v, this.n);
	}

	public UUID getId() {
		return id;
	}
}
