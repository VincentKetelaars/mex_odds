package nl.vincentketelaars.mexen.objects;

import java.util.ArrayList;

public class Turn {
	
	private Throw[] t;
	
	public Turn(Throw... t) {
		this.t = t;
	}
	
	public Turn(ArrayList<Throw> t) {
		this.t = new Throw[t.size()];
		this.t = t.toArray(this.t);
	}
	
	public Throw finalThrow() {
		return this.t[this.t.length - 1];
	}
}
