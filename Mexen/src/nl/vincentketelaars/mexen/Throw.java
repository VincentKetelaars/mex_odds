package nl.vincentketelaars.mexen;

import java.util.Calendar;

public class Throw {
	
	private int[] n;
	private Calendar dateTime;
	
	public Throw(int... n) {
		this.n = n;
		this.dateTime = Calendar.getInstance(); // Default locale
	}
	
	public int getNumberOne() {
		return this.n[0];
	}
	
	public int getNumberTwo() {
		return this.n[1];
	}
	
	public int getNumberThree() {
		return this.n[2];
	}
	
	public int numDice() {
		return this.n.length;
	}
	
	public Calendar getDateTime() {
		return this.dateTime;
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
}
