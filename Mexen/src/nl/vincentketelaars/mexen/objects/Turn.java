package nl.vincentketelaars.mexen.objects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import nl.vincentketelaars.mexen.general.StaticOperations;
import android.util.Log;

public class Turn implements Cloneable {
	
	private ArrayList<Throw> t;
	private UUID id;
	private Player player;
	private Calendar finishTime;
	private int currentThrowTurn = -1;
	
	public Turn(Turn turn) {
		instantiateTurn(turn.getId(), turn.getPlayer(), turn.getThrows(), turn.getFinishTime().getTimeInMillis(), turn.getCurrentThrowTurn());
	}
	
	public Turn(Player player) {
		instantiateTurn(UUID.randomUUID(), player, null, false);
	}
	
	public Turn(Player player, ArrayList<Throw> t, boolean finished) {
		instantiateTurn(UUID.randomUUID(), player, t, finished);
	}
	
	public Turn(ArrayList<Throw> t, UUID id, Player player, long finishTime, int throwNumber) {
		instantiateTurn(id, player, t, finishTime, throwNumber);
	}
	
	private void instantiateTurn(UUID id, Player player, ArrayList<Throw> t, boolean finished) {
		long time = StaticOperations.originTime().getTimeInMillis();
		if (finished) {
			time = Calendar.getInstance().getTimeInMillis();
		}
		instantiateTurn(id, player, t, time, this.currentThrowTurn);
	}
	
	private void instantiateTurn(UUID id, Player player, ArrayList<Throw> t, long finishTime, int throwNumber) {
		this.t = copyThrowArray(t);
		this.id = id;
		this.player = player.clone();
		this.finishTime = Calendar.getInstance();
		this.finishTime.setTimeInMillis(finishTime);
		this.currentThrowTurn = throwNumber;
	}
	
	public Throw latestThrow() {
		if (this.t.size() == 0)
			return null;
		return this.t.get(t.size() - 1).clone();
	}
	
	public Calendar startTime() {
		if (this.t.size() == 0)
			return null;
		return t.get(0).getDateTime();
	}
	
	public Turn clone() {
		return new Turn(this);
	}
	
	public  ArrayList<Throw> getThrows() {
		return copyThrowArray(this.t);
	}
	
	public int getNumThrows() {
		return this.t.size();
	}
	
	private ArrayList<Throw> copyThrowArray(ArrayList<Throw> oldT) {
		if (oldT == null)
			return new ArrayList<Throw>();
		ArrayList<Throw> newT = new ArrayList<Throw>();
		for (Throw th : oldT)
			newT.add(th.clone());		
		return newT;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Turn(");
		sb.append(this.id.toString() + ", ");
		sb.append(this.player.toString() + ", ");
		sb.append(Long.toString(this.finishTime.getTimeInMillis()) + ", ");
		sb.append(Integer.toString(this.currentThrowTurn) + ", ");
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

	public Calendar getFinishTime() {
		return finishTime;
	}
	
	public boolean isFinished() {
		return !this.finishTime.equals(StaticOperations.originTime()); // Is not originTime
	}
	
	public void setFinished() {
		if (this.finishTime.equals(StaticOperations.originTime())) // No overwriting this finishTime
			this.finishTime = Calendar.getInstance();
		else
			Log.i("Turn", "Trying to overwrite existing finish time!");
	}
	
	public void addThrow(Throw th) {
		this.t.add(th);
	}

	public int getCurrentThrowTurn() {
		return currentThrowTurn;
	}

	public void setCurrentThrowTurn(int currentThrowTurn) {
		this.currentThrowTurn = currentThrowTurn;
	}
}
