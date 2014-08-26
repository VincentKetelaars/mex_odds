package nl.vincentketelaars.mexen.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import nl.vincentketelaars.mexen.database.MexGameContract.GameEntry;
import nl.vincentketelaars.mexen.database.MexGameContract.PlayerEntry;
import nl.vincentketelaars.mexen.database.MexGameContract.ThrowEntry;
import nl.vincentketelaars.mexen.database.MexGameContract.TurnEntry;
import nl.vincentketelaars.mexen.objects.Game;
import nl.vincentketelaars.mexen.objects.GameMode;
import nl.vincentketelaars.mexen.objects.Player;
import nl.vincentketelaars.mexen.objects.Throw;
import nl.vincentketelaars.mexen.objects.Turn;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class MexGameDbHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "MexGame.db";

	public MexGameDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(GameEntry.CREATE_TABLE);
		db.execSQL(PlayerEntry.CREATE_TABLE);
		db.execSQL(TurnEntry.CREATE_TABLE);
		db.execSQL(ThrowEntry.CREATE_TABLE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		deleteTables(db);
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	private void deleteTables(SQLiteDatabase db) {
		db.execSQL(GameEntry.DELETE_TABLE);
		db.execSQL(PlayerEntry.DELETE_TABLE);
		db.execSQL(TurnEntry.DELETE_TABLE);
		db.execSQL(ThrowEntry.DELETE_TABLE);
	}

	public void addGameToDatabase(Game g) throws SQLiteException {
		if (g.getPlayers().size() == 0 || g.getTurns().size() == 0) //No point adding a game that doesn't contain anything.
			return;
		
		SQLiteDatabase db = getReadableDatabase();

		// Create Game ContentValues
		ContentValues gameValues = new ContentValues();
		gameValues.put(GameEntry.COLUMN_NAME_GAME_ID, g.getId().toString());
		gameValues.put(GameEntry.COLUMN_NAME_GAME_MODE, g.getGameMode().getOrdinal());
		gameValues.put(GameEntry.COLUMN_NAME_DATE_TIME, g.getCreationDate().getTimeInMillis());
		db.insertWithOnConflict(GameEntry.TABLE_NAME, "null", gameValues, SQLiteDatabase.CONFLICT_IGNORE);

		// Create Player ContentValues for each player in the game
		ContentValues playerValues;
		for (Player p : g.getPlayers()) {
			playerValues = new ContentValues();
			playerValues.put(PlayerEntry.COLUMN_NAME_GAME_ID, g.getId().toString());
			playerValues.put(PlayerEntry.COLUMN_NAME_PLAYER_ID, p.getId().toString());
			playerValues.put(PlayerEntry.COLUMN_NAME_PLAYER_NAME, p.getName());
			playerValues.put(PlayerEntry.COLUMN_NAME_PLAYER_LOCAL, p.isLocal() ? 1 : 0); // Needs to be an Int
			playerValues.put(PlayerEntry.COLUMN_NAME_PLAYER_CREATION_DATE_TIME, p.getCreationTime().getTimeInMillis());
			db.insertWithOnConflict(PlayerEntry.TABLE_NAME, "null", playerValues, SQLiteDatabase.CONFLICT_IGNORE);
		}

		ContentValues turnEntries;
		ContentValues throwEntries;
		for (Turn turn : g.getTurns()) {
			if (turn.getNumThrows() == 0)
				continue;
			turnEntries = new ContentValues();
			turnEntries.put(TurnEntry.COLUMN_NAME_GAME_ID, g.getId().toString());
			turnEntries.put(TurnEntry.COLUMN_NAME_TURN_ID, turn.getId().toString());
			turnEntries.put(TurnEntry.COLUMN_NAME_PLAYER_ID, turn.getPlayer().getId().toString());
			db.insertWithOnConflict(TurnEntry.TABLE_NAME, "null", turnEntries, SQLiteDatabase.CONFLICT_IGNORE);

			for (Throw t : turn.getThrows()) {
				throwEntries = new ContentValues();
				throwEntries.put(ThrowEntry.COLUMN_NAME_TURN_ID, turn.getId().toString());
				throwEntries.put(ThrowEntry.COLUMN_NAME_THROW_ID, t.getId().toString());
				throwEntries.put(ThrowEntry.COLUMN_NAME_THROW_DATE_TIME, t.getDateTime().getTimeInMillis());
				throwEntries.put(ThrowEntry.COLUMN_NAME_THROW_ONE, t.getNumberOne());
				throwEntries.put(ThrowEntry.COLUMN_NAME_THROW_TWO, t.getNumberTwo());
				throwEntries.put(ThrowEntry.COLUMN_NAME_THROW_THREE, t.getNumberThree()); // 0 for 2 dice
				db.insertWithOnConflict(ThrowEntry.TABLE_NAME, "null", throwEntries, SQLiteDatabase.CONFLICT_IGNORE);
			}
		}
		
		db.close();
	}

	public Game retrieveLatestGame() throws SQLiteException {
		SQLiteDatabase db = getReadableDatabase();

		String[] gameProjection = {
				GameEntry.COLUMN_NAME_GAME_ID,
				GameEntry.COLUMN_NAME_DATE_TIME,
				GameEntry.COLUMN_NAME_GAME_MODE
		};

		String sortOrder = GameEntry.COLUMN_NAME_DATE_TIME + " DESC";

		Cursor c = db.query(GameEntry.TABLE_NAME, gameProjection, null, null, null, null, sortOrder);

		if (!c.moveToFirst()) // DESC, so first should be the latest
			return null;
		
		Calendar gameTime = Calendar.getInstance();
		gameTime.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow(GameEntry.COLUMN_NAME_DATE_TIME)));
		String gameId = c.getString(c.getColumnIndexOrThrow(GameEntry.COLUMN_NAME_GAME_ID));
		int gameMode = c.getInt(c.getColumnIndexOrThrow(GameEntry.COLUMN_NAME_GAME_MODE));

		String[] playerProjection = {
				PlayerEntry.COLUMN_NAME_GAME_ID,
				PlayerEntry.COLUMN_NAME_PLAYER_ID,
				PlayerEntry.COLUMN_NAME_PLAYER_CREATION_DATE_TIME,
				PlayerEntry.COLUMN_NAME_PLAYER_LOCAL,
				PlayerEntry.COLUMN_NAME_PLAYER_NAME
		};

		sortOrder = PlayerEntry.COLUMN_NAME_PLAYER_ID + " DESC";
		String selection = PlayerEntry.COLUMN_NAME_GAME_ID + "=?";
		String[] selectionArgs = {gameId};
		c = db.query(PlayerEntry.TABLE_NAME, playerProjection, selection, selectionArgs, null, null, sortOrder);

		ArrayList<Player> players = new ArrayList<Player>();
		while (c.moveToNext()) {
			players.add(getPlayerFromRow(c));
		}

		String[] turnProjection = {
				TurnEntry.COLUMN_NAME_TURN_ID,
				TurnEntry.COLUMN_NAME_PLAYER_ID
		};

		String[] throwProjection = {
				ThrowEntry.COLUMN_NAME_THROW_ID,
				ThrowEntry.COLUMN_NAME_THROW_DATE_TIME,
				ThrowEntry.COLUMN_NAME_THROW_ONE,
				ThrowEntry.COLUMN_NAME_THROW_TWO,
				ThrowEntry.COLUMN_NAME_THROW_THREE,
		};

		sortOrder = TurnEntry.COLUMN_NAME_GAME_ID + " DESC";
		selection = TurnEntry.COLUMN_NAME_GAME_ID + "=?";
		selectionArgs = new String[]{gameId};
		c = db.query(TurnEntry.TABLE_NAME, turnProjection, selection, selectionArgs, null, null, sortOrder);

		sortOrder = ThrowEntry.COLUMN_NAME_TURN_ID + " DESC";
		selection = ThrowEntry.COLUMN_NAME_TURN_ID + "=?";

		ArrayList<Turn> turns = new ArrayList<Turn>();
		ArrayList<Throw> throwsList;
		Cursor cs = null;
		while (c.moveToNext()) {
			String turnId = c.getString(c.getColumnIndexOrThrow(TurnEntry.COLUMN_NAME_TURN_ID));
			if (turnId == null) {
				continue;
			}
			selectionArgs = new String[]{turnId};
			cs = db.query(ThrowEntry.TABLE_NAME, throwProjection, selection, selectionArgs, null, null, sortOrder);

			throwsList = new ArrayList<Throw>();
			while (cs.moveToNext()) {
				throwsList.add(getThrowFromRow(cs));
			}
			
			String playerId = c.getString(c.getColumnIndexOrThrow(TurnEntry.COLUMN_NAME_PLAYER_ID));
			Player turnPlayer = null;
			for (Player p : players) {
				if (p.getId().toString().equals(playerId))
					turnPlayer = p;				
			}
			
			turns.add(new Turn(throwsList, UUID.fromString(turnId), turnPlayer));
		}
		
		if (cs != null)
			cs.close();
		
		c.close();
		db.close();

		return new Game(UUID.fromString(gameId), GameMode.values()[gameMode], turns, gameTime, players);
	}

	private Player getPlayerFromRow(Cursor c) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(c.getLong(c.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME_PLAYER_CREATION_DATE_TIME)));
		return new Player(UUID.fromString(c.getString(c.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME_PLAYER_ID))), 
				cal,
				c.getString(c.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME_PLAYER_NAME)),
				c.getInt(c.getColumnIndexOrThrow(PlayerEntry.COLUMN_NAME_PLAYER_LOCAL)) != 0);
	}

	private Throw getThrowFromRow(Cursor c) {
		return new Throw(UUID.fromString(c.getString(c.getColumnIndexOrThrow(ThrowEntry.COLUMN_NAME_THROW_ID))),
				c.getLong(c.getColumnIndexOrThrow(ThrowEntry.COLUMN_NAME_THROW_DATE_TIME)), 
				c.getInt(c.getColumnIndexOrThrow(ThrowEntry.COLUMN_NAME_THROW_ONE)),
				c.getInt(c.getColumnIndexOrThrow(ThrowEntry.COLUMN_NAME_THROW_TWO)),
				c.getInt(c.getColumnIndexOrThrow(ThrowEntry.COLUMN_NAME_THROW_THREE)));
	}
}