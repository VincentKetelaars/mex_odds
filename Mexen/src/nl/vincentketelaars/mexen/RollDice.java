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

package nl.vincentketelaars.mexen;

import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class RollDice extends Activity {
	private final int rollAnimations = 50;
	private final int delayTime = 15;
	private final int[] diceImages = new int[] { R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6 };
	private Drawable dice[] = new Drawable[6];
	private final Random randomGen = new Random();
	private int roll[];
	private ImageView[] dies;
	private boolean[] vast;
	private Handler animationHandler;
	private TextView chanceTextView;
	private TextView higherChanceTextView;
	private Activity activity;
	private Button throw_button;
	private SoundPool soundPool;
	private SparseIntArray soundMap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
		
		// Make title bar icon clickable, and go home
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new SparseIntArray();
		soundMap.put(R.raw.roll, soundPool.load(this, R.raw.roll, 1)); // Note: If load returns 0 it failed
		
		dies = new ImageView[2]; // Set the number of dice
		vast = new boolean[2]; // false by default
		roll = new int[] { 1, 0 }; // Initialize to mex
		
		// Get the dice
		for (int i = 0; i < diceImages.length; i++) {
			dice[i] = getResources().getDrawable(diceImages[i]);
		}
		
		// dice
		dies[0] = (ImageView) findViewById(R.id.die1);
		dies[1] = (ImageView) findViewById(R.id.die2);

		
		// Set drawable
		animationHandler = new Handler() {
			public void handleMessage(Message msg) {
				for (int i = 0; i < dies.length; i++) {
					dies[i].setImageDrawable(dice[roll[i]]);
				}
			}
		};
		
		// Listener that increments the number on the dice
		OnClickListener diceListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i  < dies.length; i++) {
					if (dies[i].getId() == v.getId()) {
						roll[i] = (roll[i] + 1) % diceImages.length;
					}
				}
				animationHandler.sendEmptyMessage(0);
				evaluateChances();
			}
		};	
		
		for (ImageView d : dies) {
			d.setOnClickListener(diceListener); // Set the listener for each dice
		}
		
		chanceTextView = (TextView) findViewById(R.id.throw_chance_result_textview);
		higherChanceTextView = (TextView) findViewById(R.id.throw_higher_chance_result_textview);
		activity = this;
		
		throw_button = (Button) findViewById(R.id.throw_button);
		throw_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rollDice();
			}
		});
		System.out.println("Width: " + getWindowManager().getDefaultDisplay().getWidth() / getResources().getDisplayMetrics().density);
	}

	private void rollDice() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < rollAnimations; i++) {
					doRoll();					
				}
				evaluateChances();
			}
		}).start();
		soundPool.play(soundMap.get(R.raw.roll), 1f, 1f, 0, 0, 1f); // Play once at current volume
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
	
	private void evaluateChances() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				chanceTextView.setText(floatToPercentage(determineChance(roll[0] + 1, vast[0], roll[1] + 1, vast[1])));
				higherChanceTextView.setText(floatToPercentage(determineChanceHigher(roll[0] + 1, vast[0], roll[1] + 1, vast[1])));
			}
		});
	}
	
	private float determineChance(int d1, boolean v1, int d2, boolean v2) {
		if (d1 == d2)
			return 1 / 6f / 6f;
		return 1 / 6f / 6f * 2;
	}
	
	private float determineChanceHigher(int d1, boolean v1, int d2, boolean v2) {
		if (d2 > d1) {
			int x = d2;
			d2 = d1;
			d1 = x;
		}
		if (d1 == d2) // Hundreds
			return (6 - d1 + 2) / 36f; // Add 2 for the mex
		// d1 > d2
		int num_throws = 8; // Hundreds + mex
		switch (d1) {
		case 2: // mex
			return 0f;
		case 3:
			if (d2 == 1)
				return 0f; // 31,There is nothing higher, TODO: add string
			return 34 / 36f; // 32, everything is higher
		case 4:
			num_throws += 3 * 2;
		case 5:
			num_throws += 4 * 2;
		case 6:
			num_throws += 5 * 2;
			break;
		}
		num_throws -= d2 * 2; // Minus the ones you beat with the same d1
		return num_throws / 36f;
	}
	
	private String floatToPercentage(float x) {
		int percentage = Math.round(x * 100);
		return String.format(Locale.getDefault(), "%d%%", percentage);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}