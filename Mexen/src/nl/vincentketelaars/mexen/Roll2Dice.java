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

import java.util.ArrayList;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class Roll2Dice extends RollDice implements OnMenuItemClickListener {

	private enum GameMode { FREEPLAY, PLAYER }
	private GameMode currentMode = GameMode.FREEPLAY;
	private int currentThrows = -1;
	private int defaultVastHighestNumber = 3;
	private HorizontalListView previousTurnsView;
	private TwoDiceVerticleAdapter throwsAdapter;
	private ArrayList<Turn> turns;
	private ArrayList<Throw> currentTurn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roll_dice_2);
		setupViews();

		turns = new ArrayList<Turn>();
		currentTurn = new ArrayList<Throw>();

		previousTurnsView = (HorizontalListView) findViewById(R.id.previous_throws_scroll_view);
		throwsAdapter = new TwoDiceVerticleAdapter(this, new ArrayList<Throw>());
		previousTurnsView.setAdapter(throwsAdapter);
		previousTurnsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Throw t = throwsAdapter.getItem(position);
				setDie(0, t.getNumberOne());
				setDie(1, t.getNumberTwo());
			}
		});

		Point size = getSize();
		int width = size.x;
		int height = size.y;

		LinearLayout.LayoutParams gridViewParams = (LinearLayout.LayoutParams) previousTurnsView.getLayoutParams();
		int diceHistoryHeight = height / 8;
		gridViewParams.height = diceHistoryHeight;
		previousTurnsView.setLayoutParams(gridViewParams);

		height = height - diceHistoryHeight;

		FrameLayout[] frames = new FrameLayout[] {(FrameLayout) findViewById(R.id.die_frame_1),
				(FrameLayout) findViewById(R.id.die_frame_2)};
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frames[0].getLayoutParams();
		int frameWidth = (int) (width * 0.4); // Each dice 40%
		params.width = frameWidth;
		params.height = frameWidth; // Square, so width equals height
		int frameWidthMargin = (int) (width * 0.05);
		int frameHeightMargin = (int) (height / 2 - frameWidth) / 4; // Evenly divide the upper half
		params.setMargins(frameWidthMargin, frameHeightMargin, frameWidthMargin, frameHeightMargin);
		for (FrameLayout f : frames) {
			f.setLayoutParams(params);
		}

		ImageView[] dies = new ImageView[] {(ImageView) findViewById(R.id.die1), 
				(ImageView) findViewById(R.id.die2)}; // Set the number of dice
		ImageView[] vastImages = new ImageView[] {(ImageView) findViewById(R.id.die_overlay_1), 
				(ImageView) findViewById(R.id.die_overlay_2)}; // Set the number of vast
		int[] roll = new int[] { 1, 0 }; // Initialize to mex
		boolean[] vast = new boolean[2]; // false by default
		setupDice(dies, vastImages, roll, vast);
	}

	@Override
	protected float determineThrowChance() {
		return determineChance(getNumber(0), getVast(0), getNumber(1), getVast(1));
	}

	@Override
	protected float determineHigherChance() {
		return determineChanceHigher(getNumber(0), getVast(0), getNumber(1), getVast(1));
	}	

	/**
	 * Determine the chance of throwing higher than the supplied dice results.
	 * Take in account that one of the dice may be held 'vast'.
	 * 21, Mex, is the highest. 31, Dispense, is not counted as higher, but also has no higher.
	 * Same numbers are hundreds.
	 */
	private float determineChanceHigher(int d1, boolean v1, int d2, boolean v2) {
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
				Log.e("RollDice", String.format("The value of dice one, %d, should not be able to be vast (%b). Die two is %d and vast is %b", d1, v1, d2, v2));
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

	@Override
	protected boolean isSpecialVastCase() {
		// Note that the no mex vast rule should not affect 'devasting'
		return isMex(getNumber(0), getNumber(1));
	}

	@Override
	protected int getHighestVastNumber() {
		return 3;
	}

	@Override
	protected int numDice() {
		return 2;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.roll2_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.game_mode:
			showPopup(findViewById(R.id.game_mode));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showPopup(View v) {
		if (v == null)
			return;
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(this);
		popup.inflate(R.menu.mode_menu);
		popup.show();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_mode_free:
			currentMode = GameMode.FREEPLAY;
			resetPlay();
			break;
		case R.id.menu_mode_player:
			currentMode = GameMode.PLAYER;
			resetPlay();
			break;
		default:
			Log.i("Roll2Dice", "OnMenuItemClick, no recognized item!");
			return false;
		}
		updateView();
		return true;
	}

	/**
	 * Update the entire view
	 * GameMode changes assume to start over
	 */
	private void updateView() {
		if (currentMode == GameMode.FREEPLAY) {
			super.throw_button.setText(getResources().getString(R.string.throw_dice));
			currentThrows = -1;
		} else if (currentMode == GameMode.PLAYER) {
			super.throw_button.setText(getResources().getString(R.string.throw_one));
			currentThrows = 0;
		}
	}

	@Override
	protected void afterRollDice() {
		currentTurn.add(new Throw(getNumber(0), getNumber(1)));
		switch(currentMode) {
		case FREEPLAY:
			addDiceToPrevious(currentTurn.get(currentTurn.size() - 1));
			break;
		case PLAYER:
			boolean v0 = getVast(0);
			boolean v1 = getVast(1);
			setUnvast(0);
			setUnvast(1);
			if (isMex(getNumber(0), getNumber(1)) || isLow(getNumber(0), getNumber(1))) {
				currentThrows = -1; // Becomes zero later
			} else if (isPoint(getNumber(0), getNumber(1))) {
				currentThrows--; // Becomes the same later
			} else if (currentThrows < 2) { // Throw is added later, so 2 instead of 3
				if (v0 || v1) {
					updateVastAlready(v0 ? 0 : 1);
				} else {
					updateVastAlready(-1);			
				}
			}
			currentThrows++;
			if (currentThrows > 2)
				currentThrows = 0;
			if (currentThrows == 0 && !isPoint(getNumber(0), getNumber(1)))
				turnFinished();
			setThrowLabel();
			break;			
		}
	}

	private void turnFinished() {
		Turn t = new Turn(currentTurn);
		turns.add(t);
		addDiceToPrevious(t.finalThrow());
		currentTurn = new ArrayList<Throw>();
	}

	private void updateVastAlready(int already) {
		for (int i = 0; i < numDice(); i++) {
			if (already != i) {
				if ( getNumber(i) <= defaultVastHighestNumber) {
					setVast(i);
					break;
				}
			}
		}
	}

	private void setThrowLabel() {
		switch(currentThrows) {
		case 0:
			setThrowButtonLabel(R.string.throw_one);
			break;
		case 1:
			setThrowButtonLabel(R.string.throw_two);
			break;
		case 2:
			setThrowButtonLabel(R.string.throw_three);
			break;
		default:
			setThrowButtonLabel(R.string.throw_again);
		}
	}

	private void addDiceToPrevious(final Throw t) {
		previousTurnsView.post(new Runnable() {
			public void run() {
				throwsAdapter.addThrow(t);
			} 
		});
	}
	
	protected boolean changeDiceValueAllowed() {
		return currentMode != GameMode.PLAYER;
	}

	private void resetPlay() {
		throwsAdapter.clearThrows();
	}
}