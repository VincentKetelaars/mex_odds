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
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public abstract class RollDice extends Activity {
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
	private Button throw_button;
	private SoundPool soundPool;
	private SparseIntArray soundMap;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Make title bar icon clickable, and go home
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		soundMap = new SparseIntArray();
		soundMap.put(R.raw.roll, soundPool.load(this, R.raw.roll, 1)); // Note: If load returns 0 it failed
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
		
		throw_button = (Button) findViewById(R.id.throw_button);
		throw_button.setOnClickListener(new OnClickListener() {
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
							vastImages[i].setVisibility(View.INVISIBLE);
							vast[i] = false;
							break;
						}
						if (isSpecialVastCase()) break;
						if (vastImages[i].getVisibility() == View.INVISIBLE && numVast < dies.length - 1 &&
							roll[i] <= 2) { // Only vast on 1, 2 or 3
							vastImages[i].setVisibility(View.VISIBLE);
							vast[i] = true;
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
	}
	
	protected abstract boolean isSpecialVastCase();

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
	
	protected abstract void evaluateChances();
	
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
	
	protected void setChances(float diceThrough, float diceHigher) {
		chanceTextView.setText(floatToPercentage(diceThrough));
		higherChanceTextView.setText(floatToPercentage(diceHigher));		
	}
	
	protected int getRoll(int index) {
		return roll[index];
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
	 * Determine the chance of throwing higher than the supplied dice results.
	 * Take in account that one of the dice may be held 'vast'.
	 * 21, Mex, is the highest. 31, Dispense, is not counted as higher, but also has no higher.
	 * Same numbers are hundreds.
	 */
	protected float determineChanceHigher(int d1, boolean v1, int d2, boolean v2) {
		if ((d2 > d1 && !v1 && !v2) || v2) { // Switch dice without vast to highest first, vast should be first
			int x = d2;
			d2 = d1;
			d1 = x;
			boolean y = v2;
			v2 = v1;
			v1 = y;
		}
		// d1 >= d2
		int num_throws = 0;
		if (v1 || v2) { // vast
			switch (d1) {
			case 1: // 11
				if (d2 == 1)
					return 1 / 6f; // 21
				if (d2 == 2 || d2 == 3) // 21, 31
					return 0f; // no higher
				return (6 - d2 + 2) / 6f; // EXAMPLE: d2 == 4, (11, 21, 51, 61 are higher), (6 - 4 + 2)
			case 2:
				if (d2 == 1) // 21
					return 0f; // no higher
				if (d2 == 2)
					return 1 / 6f; // 22, so only 21
				return (6 - d2 + 2) / 6f; // EXAMPLE: d2 == 3, (21, 22, 42, 52, 62 are higher), (6 - 3 + 2)
			case 3:
				if (d2 == 1)
					return 0f; // 31, there is nothing higher!
				if (d2 == 2) {
					if (v1) // 3 is vast
						return 4 / 6f; // (33, 43, 53, 63 are higher)
					return 5 / 6f; // 2 is vast, others are higher 
				}					
				if (d2 == 3)
					return 0f; // 33, highest
				return (6 - d2 + 1) / 6f; // EXAMPLE: d2 == 4, (33, 53, 63 are higher), (6 - 4 + 1)
			default:
				Log.e("RollDice", String.format("The value of dice one, %d, should not be able to be vast", d1));
			}
		} else { // not vast
			if (d1 == d2) // Hundreds
				return (6 - d1 + 2) / 36f; // Count hundreds once, and add 2 for the mex
			// d1 > d2
			num_throws = 8; // Hundreds + mex
			switch (d1) {
			case 2: // 21
				return 0f; // mex
			case 3: // 31, 32
				if (d2 == 1)
					return 0f; // 31,There is nothing higher, TODO: add string
				return 34 / 36f; // 32, everything is higher
			// Add to the hundreds and mex
			case 4: // 41, 42, 43
				num_throws += 3 * 2; // Add all possibilities
			case 5: // 51, 52, 53, 54
				num_throws += 4 * 2; // Add all possibilities
			case 6: // 61, 62, 63, 64, 65
				num_throws += 5 * 2; // Add all possibilities
				break;
			default:
				Log.e("RollDice", String.format("The value of dice one, %d, should not be possible", d1));
			}
			// EXAMPLE: d1=5, d2=2; 8 (Hundreds and mex) + 4 * 2 (51 - 54) + 5 * 2 (61 - 65) - 2 * 2 (51 - 52)
			num_throws -= d2 * 2; // Minus the ones you beat with the same d1
		}
		return num_throws / 36f;
	}
}