package nl.vincentketelaars.mexen.objects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class Turn implements Cloneable {
	
	private Throw[] t;
	private UUID id;
	private Player player;
	
	public Turn(Turn turn) {
		instantiateTurn(turn.getId(), turn.getPlayer(), turn.getThrows());
	}
	
	public Turn(Player player, Throw... t) {
		instantiateTurn(UUID.randomUUID(), player, t);
	}
	
	public Turn(Player player, ArrayList<Throw> t) {
		this.t = new Throw[t.size()];
		instantiateTurn(UUID.randomUUID(), player, t.toArray(this.t));
	}
	
	public Turn(ArrayList<Throw> t, UUID id, Player player) {
		this.t = new Throw[t.size()];
		instantiateTurn(id, player, t.toArray(this.t));
	}
	
	private void instantiateTurn(UUID id, Player player, Throw[] t) {
		this.t = copyThrowArray(t);
		this.id = id;
		this.player = player.clone();
	}
	
	public Throw finalThrow() {
		if (this.t.length == 0)
			return null;
		return this.t[this.t.length - 1].clone();
	}
	
	public Calendar startTime() {
		if (this.t.length == 0)
			return null;
		return t[0].getDateTime();
	}
	
	public Calendar finishTime() {
		if (this.t.length == 0)
			return null;
		return t[t.length - 1].getDateTime();
	}
	
	public Turn clone() {
		return new Turn(this);
	}
	
	public Throw[] getThrows() {
		return copyThrowArray(this.t);
	}
	
	public int getNumThrows() {
		return this.t.length;
	}
	
	private Throw[] copyThrowArray(Throw[] oldT) {
		if (oldT == null)
			return null;
		Throw[] newT = new Throw[oldT.length];
		for (int i = 0; i < oldT.length; i++)
			newT[i] = oldT[i].clone();			
		return newT;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Turn(");
		for (Throw t : this.t)
			sb.append(t.toString() + ", ");
		sb.setLength(sb.length() - 2);
		sb.append(")");
		return sb.toString();
	}

	public UUID getId() {
		return id;
	}

	public Player getPlayer() {
		return player.clone();
	}
}
