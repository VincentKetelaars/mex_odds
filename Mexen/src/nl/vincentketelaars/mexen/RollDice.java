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
import android.util.Log;
import android.util.SparseIntArray;
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
		
		Log.i("RollDice", String.format("Width: %f\nHeight: %f", 
				getWindowManager().getDefaultDisplay().getWidth() / getResources().getDisplayMetrics().density,
				getWindowManager().getDefaultDisplay().getHeight() / getResources().getDisplayMetrics().density));
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
			public boolean onLongClick(View v) {
				for (int i = 0; i  < dies.length; i++) {
					if (dies[i].getId() == v.getId()) {
						if (vastImages[i].getVisibility() == View.VISIBLE && !vast[vast.length - 1 - i] &&
							roll[i] <= 2) { // Only vast on 1, 2 or 3
							vastImages[i].setVisibility(View.INVISIBLE);
							vast[i] = false;
							break;
						}
						// Note that the no mex vast rule should not affect 'devasting'
						if (roll[0] == 0 && roll[1] == 1 || roll[0] == 1 && roll[1] == 0) // 12 or 21
							break; // No vast with mex
						if (vastImages[i].getVisibility() == View.INVISIBLE && !vast[vast.length - 1 - i] &&
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
}