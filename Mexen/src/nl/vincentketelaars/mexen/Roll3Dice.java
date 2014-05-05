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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class Roll3Dice extends RollDice {
	private Activity activity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roll_dice_3);
		setupViews();

		ImageView[] dies = new ImageView[] {(ImageView) findViewById(R.id.die1), 
				(ImageView) findViewById(R.id.die2), (ImageView) findViewById(R.id.die3)}; // Set the number of dice
		ImageView[] vastImages = new ImageView[] {(ImageView) findViewById(R.id.die_overlay_1), 
				(ImageView) findViewById(R.id.die_overlay_2),
				(ImageView) findViewById(R.id.die_overlay_3)}; // Set the number of vast
		int[] roll = new int[] {5, 1, 0}; // Initialize to mex
		boolean[] vast = new boolean[3]; // false by default
		setupDice(dies, vastImages, roll, vast);

		activity = this;
	}

	protected void evaluateChances() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				setChances(determineChance(getRoll(0) + 1, getVast(0), getRoll(1) + 1, getVast(1), 
						getRoll(2) + 1, getVast(2)), 
						determineChanceHigher(getRoll(0) + 1, getVast(0), getRoll(1) + 1, getVast(1),
								getRoll(2) + 1, getVast(2)));
			}
		});
	}

	private float determineChance(int d1, boolean v1, int d2, boolean v2, int d3, boolean v3) {
		if (v1)
			return determineChance(d2, v2, d3, v3);
		else if (v2)
			return determineChance(d1, v1, d3, v3);
		else if (v3)
			return determineChance(d1, v1, d2, v2);
		if (d1 == d2 && d2 == d3) // All the same
			return 1 / 6f / 6f / 6f;
		if (d1 == d2 || d2 == d3 || d1 == d3) // Two equals
			return 1 / 6f / 6f / 6f * 3; // (1 / 6 / 6 / 6) * 3 
		return 1 / 6f / 6f; // No equals (1 / 6 / 6 / 6) * 6
	}

	/**
	 * Determine the chance of throwing higher than the supplied dice results.
	 * Take in account that one or two of the dice may be held 'vast'.
	 */
	private float determineChanceHigher(int d1, boolean v1, int d2, boolean v2, int d3, boolean v3) {
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

	@Override
	protected boolean isSpecialVastCase() {
		return false;
	}
}