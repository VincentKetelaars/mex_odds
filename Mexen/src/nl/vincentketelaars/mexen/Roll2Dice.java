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
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Roll2Dice extends RollDice {
	private Activity activity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roll_dice_2);
		setupViews();
		
		FrameLayout[] frames = new FrameLayout[] {(FrameLayout) findViewById(R.id.die_frame_1),
				(FrameLayout) findViewById(R.id.die_frame_2)};
		Point size = getSize();
		int width = size.x;
		int height = size.y;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) frames[0].getLayoutParams();
		int frameWidth = (int) (width * 0.4); // Each dice 40%
		params.width = frameWidth;
		params.height = frameWidth; // Square, so width equals height
		int frameWidthMargin = (int) (width * 0.05);
		int frameHeightMargin = (int) (height / 2 - frameWidth) / 2; // Evenly divide the upper half
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
		
		activity = this;
	}
	
	protected void evaluateChances() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				setChances(determineChance(getRoll(0) + 1, getVast(0), getRoll(1) + 1, getVast(1)), 
						determineChanceHigher(getRoll(0) + 1, getVast(0), getRoll(1) + 1, getVast(1)));
			}
		});
	}

	@Override
	protected boolean isSpecialVastCase() {
		// Note that the no mex vast rule should not affect 'devasting'
		if (getRoll(0) == 0 && getRoll(1) == 1 || getRoll(0) == 1 && getRoll(1) == 0) // 12 or 21
			return true; // No vast with mex
		return false;
	}
}