/*  This file is part of Simple Dice.
 *
 *  Simple Dice is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Dice is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Dice.  If not, see <http://www.gnu.org/licenses/>.
 */

package nl.vincentketelaars.mexen.activities;

import java.util.Locale;
import java.util.Random;

import nl.vincentketelaars.mexen.R;
import nl.vincentketelaars.mexen.database.MexGameDbHelper;
import nl.vincentketelaars.mexen.objects.Game;
import nl.vincentketelaars.mexen.objects.GameMode;
import nl.vincentketelaars.mexen.objects.Throw;
import nl.vincentketelaars.mexen.objects.Turn;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class RollDice extends GenericActivity implements SensorEventListener {
	private Activity activity;
	private final int rollAnimations = 50;
	private final int delayTime = 15;
	private final int[] diceImages = new int[] { R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6 };
	private Drawable dice[] = new Drawable[6];
	private final Random randomGen = new Random();
	private int roll[];
	private ImageView[] dies;
	private ImageView[] vastImages;
	private boolean[] vast;
	private Handler animationHandler;
	private TextView chanceTextView;
	private TextView higherChanceTextView;
	protected Button throwButton;
	private SoundPool soundPool;
	private SparseIntArray soundMap;

	protected GameMode currentMode = GameMode.FREEPLAY;
	protected Game currentGame;
	protected Turn currentTurn;
	private MexGameDbHelper MGDbHelper;
	private SensorManager sensorManager;
	private float MINIMUM_ACCELARATION = 2;
	private boolean throwOnShake;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make title bar icon clickable, and go home
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setVolumeControlStream(AudioManager.STREAM_MUSIC); // User can modify music volume
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new SparseIntArray();
		soundMap.put(R.raw.roll, soundPool.load(this, R.raw.roll, 1)); // Note: If load returns 0 it failed

		activity = this;

		currentGame = new Game(currentMode);

		MGDbHelper = new MexGameDbHelper(this);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		getActionBar().setTitle(String.format(getResources().getString(R.string.roll_activity_name), numDice()));
	}

	protected Point getSize() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	protected void setupViews() {
		chanceTextView = (TextView) findViewById(R.id.throw_chance_result_textview);
		higherChanceTextView = (TextView) findViewById(R.id.throw_higher_chance_result_textview);

		throwButton = (Button) findViewById(R.id.throw_button);
		throwButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rollDice();
			}
		});
	}

	protected void setupDice(ImageView[] diesIn, ImageView[] vastImagesIn, int[] rollIn, boolean vastIn[]) {
		this.dies = diesIn;
		this.vastImages= vastImagesIn;
		this.roll = rollIn;
		this.vast = vastIn;

		// Get the dice
		for (int i = 0; i < diceImages.length; i++) {
			dice[i] = getResources().getDrawable(diceImages[i]);
		}

		// Set drawable
		animationHandler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message msg) {
				for (int i = 0; i < dies.length; i++) {
					dies[i].setImageDrawable(dice[roll[i]]);
				}
				return true;
			}
		});

		// Listener that increments the number on the dice
		OnClickListener diceListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!changeDiceValueAllowed())
					return;
				for (int i = 0; i  < dies.length; i++) {
					if (dies[i].getId() == v.getId() && !vast[i]) {
						roll[i] = (roll[i] + 1) % diceImages.length;
					}
				}
				animationHandler.sendEmptyMessage(0);
				evaluateChances();
			}
		};

		// Listener that sets vast
		OnLongClickListener vastListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				int numVast = 0;
				for (boolean v : vast) {
					if (v) numVast++;
				}
				for (int i = 0; i  < dies.length; i++) {
					if (dies[i].getId() == view.getId()) {
						if (vastImages[i].getVisibility() == View.VISIBLE) {
							setVast(i, false);
							break;
						}
						if (isSpecialVastCase()) break;
						if (vastImages[i].getVisibility() == View.INVISIBLE && numVast < dies.length - 1 &&
								roll[i] < getHighestVastNumber()) { // Only vast till highest allowed number
							setVast(i, true);
						}
					}
				}
				evaluateChances();
				return true;
			}
		};

		for (ImageView d : dies) {
			d.setOnClickListener(diceListener); // Set the listener for each dice
			d.setOnLongClickListener(vastListener); // Set the long listener for each dice
		}
		evaluateChances();
	}

	protected abstract boolean isSpecialVastCase();

	private void rollDice() {
		setThrowButtonEnabled(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < rollAnimations; i++) {
					doRoll();					
				}
				evaluateChances();
				afterRollDice();
				setThrowButtonEnabled(true);
			}
		}).start();
		soundPool.play(soundMap.get(R.raw.roll), 1f, 1f, 0, 0, 1f); // Play once at current volume
	}

	private void setThrowButtonEnabled(final boolean enabled) {
		throwButton.post(new Runnable() {
			@Override
			public void run() {
				throwButton.setEnabled(enabled);
			}
		});		
	}

	private void doRoll() { // only does a single roll
		for (int i = 0; i < dies.length; i++) {
			if (!vast[i])
				roll[i] = randomGen.nextInt(6);
		}
		synchronized (getLayoutInflater()) {
			animationHandler.sendEmptyMessage(0);
		}
		try { // delay to alloy for smooth animation
			Thread.sleep(delayTime);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Evaluate the chances of throwing the current throw and higher and set these values to the 
	 */
	protected void evaluateChances() {
		// Calculate chances before going on UI thread
		final float throwChance = determineThrowChance();
		final float higherChance = determineHigherChance();
		activity.runOnUiThread(new Runnable() {
			public void run() {
				setChances(throwChance, higherChance);
			}
		});
	}

	protected abstract float determineThrowChance();

	protected abstract float determineHigherChance();

	private String floatToPercentage(float x) {
		return String.format(Locale.getDefault(), "%.2f%%", x * 100);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			onExitingGame(false, true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void setChances(float diceThrough, float diceHigher) {
		chanceTextView.setText(floatToPercentage(diceThrough));
		higherChanceTextView.setText(floatToPercentage(diceHigher));		
	}

	protected int getRoll(int index) {
		return roll[index];
	}

	protected int getNumber(int index) {
		return roll[index] + 1;
	}

	protected boolean getVast(int index) {
		return vast[index];
	}

	protected float determineChance(int d1, boolean v1, int d2, boolean v2) {
		if (v1 || v2)
			return 1 / 6f;
		if (d1 == d2)
			return 1 / 6f / 6f;
		return 1 / 6f / 6f * 2;
	}

	/**
	 * Determines which dice are allowed to be vast
	 * @return num between 1 and 6
	 */
	protected abstract int getHighestVastNumber();

	protected boolean isMex(int d1, int d2) {
		return (d1 == 1 && d2 == 2 || d1 == 2 && d2 == 1); // 12 or 21
	}

	protected boolean isLow(int d1, int d2) {
		return (d1 == 3 && d2 == 2 || d1 == 2 && d2 == 3); // 32 or 23
	}

	protected boolean isPoint(int d1, int d2) {
		return (d1 == 3 && d2 == 1 || d1 == 1 && d2 == 3); // 31 or 13
	}

	/**
	 * @return number of dice
	 */
	protected abstract int numDice();

	/**
	 * Is called after the dice is rolled
	 */
	protected abstract void afterRollDice();

	/**
	 * Set die vast / unvast
	 * @param i
	 */
	protected void setVast(final int i, final boolean v) {
		vast[i] = v;
		vastImages[i].post(new Runnable() {
			public void run() {
				vastImages[i].setVisibility(v ? View.VISIBLE : View.INVISIBLE);
			} 
		});
	}

	protected void setThrowButtonLabel(final int labelString) {
		throwButton.post(new Runnable() {
			public void run() {
				String label = getResources().getString(labelString);
				throwButton.setText(label);
			} 
		});
	}

	protected boolean changeDiceValueAllowed() {
		return true;
	}

	protected boolean setDie(int position, int number, boolean forceChange) {
		if (position >= roll.length || (!changeDiceValueAllowed() && !forceChange))
			return false;
		roll[position] = number - 1;
		animationHandler.sendEmptyMessage(0);
		evaluateChances();
		return true;
	}

	protected void storeTurn(boolean finished) {
		if (finished)
			currentTurn.setFinished();
		currentGame.addTurn(currentTurn);
		if (finished) // Only if the previous turn is finished should a new one be started
			currentTurn = new Turn(localPlayer()); // TODO: Is this always the local player?
	}
	
	protected void gameFinished() {
		storeTurn(true);
		currentGame.setFinished();
		writeCurrentGameToDb(currentGame);
	}

	protected void addToThrows(Throw t) {
		currentTurn.addThrow(t);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.i("RollDice", "onStart");
		currentTurn = new Turn(localPlayer());
		retrieveCurrentGameFromDb();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("RollDice", "onResume");
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		throwOnShake  = sp.getBoolean("pref_shake_throw", false);
		if (throwOnShake)
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i("RollDice", "onPause");
		sensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i("RollDice", "onStop");
		currentGame.addPlayer(localPlayer());
		writeCurrentGameToDb(currentGame);
	}

	protected void writeCurrentGameToDb(Game game) {
		new CommitGameToDbAsyncTask().execute(game);
	}

	protected void retrieveCurrentGameFromDb() {
		if (currentGame == null || currentGame.getTurns().size() == 0)
			new RetrieveGameFromDbAsyncTask().execute();		
	}

	private class CommitGameToDbAsyncTask extends AsyncTask<Game, Void, String> {
		@Override
		protected String doInBackground(Game... games) {
			MGDbHelper.addGameToDatabase(games[0]);
			Log.i("RollDice", "Added " + games[0]);
			return null;
		}
	}

	private class RetrieveGameFromDbAsyncTask extends AsyncTask<String, Void, Game> {
		@Override
		protected Game doInBackground(String... urls) {
			return MGDbHelper.retrieveLatestGame();
		}

		@Override
		protected void onPostExecute(Game game) {
			super.onPostExecute(game);
			Log.i("RollDice", game != null ? game.toString() : "");
			// Ensure game is not null and we're playing the same number of dice
			if (game != null && !game.isFinished() && (game.numDice() == -1 || game.numDice() == numDice())) {
				currentGame = game;
				currentGame.addPlayer(localPlayer());
				if (game.getTurns().size() > 0) {
					Turn latest = game.getTurns().get(game.getTurns().size() - 1);
					if (!latest.isFinished()) {
						currentTurn = latest;
					}
				}
				currentMode = currentGame.getGameMode();
				onGameRetrieved(game);
			}
		}
	}
	
	protected abstract void onGameRetrieved(Game game);

	@Override
	public void onBackPressed() {
		onExitingGame(true, false);
	}

	private void onExitingGame(final boolean backPressed, final boolean homePressed) {
		if (currentMode == GameMode.FREEPLAY) {
			gameFinished();
			if (backPressed)
				RollDice.super.onBackPressed();
			else if (homePressed)
				NavUtils.navigateUpFromSameTask(activity);		
			return;
		}
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					gameFinished();
					if (backPressed)
						activity.onBackPressed();
					else if (homePressed)
						NavUtils.navigateUpFromSameTask(activity);
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// Doing nothing..
					break;
				}
			}
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getResources().getString(R.string.stop_game))
		.setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
		.setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
	}

	protected void resetPlay() {
		gameFinished();
		currentGame = new Game(currentMode);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			getAccelerometer(event);
		}
	}

	private void getAccelerometer(SensorEvent event) {
		float[] values = event.values;
		// Movement
		float x = values[0];
		float y = values[1];
		float z = values[2];

		float accelationSquareRoot = (x * x + y * y + z * z) 
				/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
		if (accelationSquareRoot >= MINIMUM_ACCELARATION ) {
			if (throwButton.isEnabled()) {
				throwButton.performClick();
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}