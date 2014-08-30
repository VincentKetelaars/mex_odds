package nl.vincentketelaars.mexen.activities;

import java.util.Calendar;
import java.util.UUID;

import nl.vincentketelaars.mexen.objects.Player;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class GenericActivity extends Activity {

	private Player localPlayer;
	private final String PLAYER_ID = "player_id";
	private final String PLAYER_NAME = "player_name";
	private final String PLAYER_DATE_TIME = "player_date";
	protected final String SHARED_PREFERENCES_FILE = "MexGameSharedPreferences";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences sp = getSharedPreferences(SHARED_PREFERENCES_FILE, MODE_PRIVATE);
		if (sp.contains(PLAYER_ID)) {
			UUID id = UUID.fromString(sp.getString(PLAYER_ID, null));
			Calendar dateTime = Calendar.getInstance();
			dateTime.setTimeInMillis(sp.getLong(PLAYER_DATE_TIME, 0));
			localPlayer = new Player(id, dateTime, sp.getString(PLAYER_NAME, null), true);
		} else {
			localPlayer = Player.instantiatePlayer();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (localPlayer != null) {
			SharedPreferences.Editor spe = getSharedPreferences(SHARED_PREFERENCES_FILE, MODE_PRIVATE).edit();
			spe.putString(PLAYER_ID, localPlayer.getId().toString());
			spe.putLong(PLAYER_DATE_TIME, localPlayer.getCreationTime().getTimeInMillis());
			spe.putString(PLAYER_NAME, localPlayer.getName());
			spe.commit();
		} else {
			Log.e("GenericActivity", "For some reason the local player is null");
		}
	}
	
	protected Player localPlayer() {
		return localPlayer;
	}
	
}
