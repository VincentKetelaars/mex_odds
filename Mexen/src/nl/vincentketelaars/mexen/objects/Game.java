package nl.vincentketelaars.mexen.objects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import android.util.Log;
import nl.vincentketelaars.mexen.general.StaticOperations;

public class Game implements Cloneable {
	
	private Calendar creationDate;
	private Calendar finishDate;
	private GameMode gameMode;
	private ArrayList<Turn> turns;
	private ArrayList<Player> players;
	private UUID id;
	
	public Game(GameMode gm) {
		initiate(null, gm, null, null, null, null);
	}
	
	public Game(GameMode gm, ArrayList<Turn> turns) {
		initiate(null, gm, turns, null, null, null);
	}
	
	public Game(GameMode gm, ArrayList<Turn> turns, Calendar dateTime) {
		initiate(null, gm, turns, dateTime, null, null);
	}
	
	public Game(UUID id, GameMode gm, ArrayList<Turn> turns, Calendar startTime, Calendar finishTime, ArrayList<Player> players) {
		initiate(id, gm, turns, startTime, finishTime, players);
	}
	
	private void initiate(UUID id, GameMode gm, ArrayList<Turn> turns, Calendar startTime, Calendar finishTime, ArrayList<Player> players) {
		if (id == null)
			this.id = (UUID.randomUUID());
		else
			this.id = id;
		
		this.gameMode = gm;
		this.turns = copyTurns(turns);
		
		if (startTime != null)
			this.creationDate = (Calendar) startTime.clone();
		else if (turns != null && !turns.isEmpty())
			this.creationDate = StaticOperations.max(Calendar.getInstance(), turns.get(0).startTime());
		else
			this.creationDate = Calendar.getInstance();
		
		if (finishTime != null)
			this.finishDate = (Calendar) finishTime.clone();
		else
			this.finishDate = StaticOperations.originTime();
		
		if (players != null)
			this.players = copyPlayers(players);
		else
			this.players = new ArrayList<Player>();
	}
	
	public Game clone() {		
		return new Game(this.getId(), this.getGameMode(), this.turns, this.getCreationDate(), this.getFinishDate(), this.players);
	}
	
	public void addTurn(Turn t) {
		if (t != null)
			this.turns.add(t);
	}

	public UUID getId() {
		return id;
	}

	public Calendar getCreationDate() {
		return (Calendar) creationDate.clone();
	}

	public GameMode getGameMode() {
		return gameMode;
	}

	public ArrayList<Turn> getTurns() {
		return copyTurns(this.turns);
	}
	
	private ArrayList<Turn> copyTurns(ArrayList<Turn> oldTurns) {
		if (oldTurns == null)
			return new ArrayList<Turn>();
		ArrayList<Turn> newTurns = new ArrayList<Turn>(oldTurns.size());
		for (Turn t : oldTurns)
			newTurns.add(t.clone());
		return newTurns;
	}

	public ArrayList<Player> getPlayers() {
		return copyPlayers(this.players);
	}
	
	private ArrayList<Player> copyPlayers(ArrayList<Player> oldPlayers) {
		ArrayList<Player> newPlayers = new ArrayList<Player>(oldPlayers.size());
		for (Player p : oldPlayers)
			newPlayers.add(p.clone());
		return oldPlayers;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Game(%d, %d, %s, %s, {", this.creationDate.getTimeInMillis(), 
				this.finishDate.getTimeInMillis(), this.gameMode.getName(), this.id.toString()));
		for (Turn t : this.turns)
			sb.append(t.toString() + ", ");
		if (this.turns.size() > 0)
			sb.setLength(sb.length() - 2);
		sb.append("}, {");
		for (Player p : this.players)
			sb.append(p.toString() + ", ");
		if (this.players.size() > 0)
			sb.setLength(sb.length() - 2);
		sb.append("})");
		return sb.toString();
	}
	
	public boolean addPlayer(Player player) {
		if (!isPlaying(player))
			return this.players.add(player);
		return false;
	}
	
	public boolean isPlaying(Player player) {
		for (Player p : this.players) {
			if (p.getId().equals(player.getId()))
				return true;
		}
		return false;
	}
	
	public int numDice() {
		if (getTurns().size() == 0)
			return -1;
		return getTurns().get(0).latestThrow().numDice();
	}

	public Calendar getFinishDate() {
		return this.finishDate;
	}
	
	public boolean isFinished() {
		return !this.finishDate.equals(StaticOperations.originTime()); // Is not originTime
	}

	public void setFinished() {
		if (this.finishDate.equals(StaticOperations.originTime())) // No overwriting this datetime
			this.finishDate = Calendar.getInstance();
		else
			Log.i("Game", "Trying to overwrite existing finish time!");
	}
}
