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

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Roll3Dice extends RollDice {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_roll_dice_3);
		setupViews();
		
		FrameLayout[] frames = new FrameLayout[] {(FrameLayout) findViewById(R.id.die_frame_1),
				(FrameLayout) findViewById(R.id.die_frame_2),
				(FrameLayout) findViewById(R.id.die_frame_3)};
		Point size = getSize();
		int width = size.x;
		int height = size.y;
		// Width will be restricted either by height or width. Dice should take max 50% of screen height
		int frameWidth = (int) Math.min(width * 0.46, height * 0.5 * 0.46); // Each dice 46%
		// Ensure that the space between each dice is equal
		int frameHeightMargin = (int) (height * 0.5 - frameWidth * 2) / 3; // Divide the remainder evenly
		int frameWidthMarginOuter = (int) (width - 2 * frameWidth - frameHeightMargin) / 2; // Calculate outer
		int frameWidthMarginCenter = (int) frameHeightMargin / 2; // Same spacing between dice
		int frameWidthMarginUpper = (int) (width - frameWidth) / 2; // Upper only
		for (int i = 0; i < frames.length; i++) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.width = frameWidth;
			params.height = frameWidth; // Square, so width equals height
			if (i == 0) {
				params.setMargins(frameWidthMarginUpper, frameHeightMargin, frameWidthMarginUpper, frameWidthMarginCenter); // Gravity is centered
			} else if (i == 1) { // Left dice
				params.setMargins(frameWidthMarginOuter, frameWidthMarginCenter, frameWidthMarginCenter, frameHeightMargin);
			} else if (i == 2) {// Right dice
				params.setMargins(frameWidthMarginCenter, frameWidthMarginCenter, frameWidthMarginOuter, frameHeightMargin);
			}
			frames[i].setLayoutParams(params);
		}

		ImageView[] dies = new ImageView[] {(ImageView) findViewById(R.id.die1), 
				(ImageView) findViewById(R.id.die2), (ImageView) findViewById(R.id.die3)}; // Set the number of dice
		ImageView[] vastImages = new ImageView[] {(ImageView) findViewById(R.id.die_overlay_1), 
				(ImageView) findViewById(R.id.die_overlay_2),
				(ImageView) findViewById(R.id.die_overlay_3)}; // Set the number of vast
		int[] roll = new int[] {5, 1, 0}; // Initialize to mex
		boolean[] vast = new boolean[3]; // false by default
		setupDice(dies, vastImages, roll, vast);
	}
	
	@Override
	protected float determineThrowChance() {
		return determineChance(getNumber(0), getVast(0), getNumber(1), getVast(1), getNumber(2), getVast(2));
	}
	
	@Override
	protected float determineHigherChance() {
		return determineChanceHigher(getNumber(0), getVast(0), getNumber(1), getVast(1), getNumber(2), getVast(2));
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
	 * Determines whether it is a Mex combination
	 * @return mex number if mex, otherwise -1
	 */
	private int isMex(int n1, int n2, int n3) {
		int[] nums = new int[7];
		nums[n1]++;
		nums[n2]++;
		nums[n3]++;
		if (--nums[1] >= 0 && --nums[2] >= 0)
			for (int i = 0; i < nums.length; i++)
				if (nums[i] == 1)
					return i;
		return -1;
	}
	
	/**
	 * Determine the number of higher throws with one vast die and the mex number available
	 * @param vast: Vast number
	 * @param mex: Current mex number
	 * @return number of higher throws
	 */
	private int mexVastOne(int vast, int mex) {
		// We need to beat 21 'mex'
		if (vast == mex) { // Mex number is held vast
			if (vast > 2) // 12 3-6, no chance at all, because the rest is already mex
				return 0;
			if (vast == 2) return 8; // 122, Chance of a 1 with 3 or higher
			return 9; // 121, Chance of 2 with 2 or higher
		}
		// vast == 2, we need a 1 with mex + 1 or higher
		// vast == 1, need a 2 with mex + 1 or higher
		// 112, 122 is already done
		return (6 - mex) * 2;
	}
	
	/**
	 * Two dice are vast, determine the number of higher possible throws.
	 * @param notVast: Die that is free
	 * @param mex: The current mex number
	 * @return number of higher throws
	 */
	private int mexVastTwo(int notVast, int mex) {
		if (notVast == mex)
			return 6 - mex; // We have 21, so mex + 1 or higher
		return 0; // You hold a 1 or 2 with a 3-6
	}
	
	/**
	 * Decrease numThrows with throws for die combinations that are lower than the current throw
	 * Only remove those where d2 == num
	 * The dice numbers are ordered from high to low, for d1 through d3 respectively.
	 * EXAMPLE: 
	 * @param numThrows
	 * @param d1: Highest die number
	 * @param d2: Middle die number
	 * @param d3: Lowest die number
	 * @param num: Which level we are looking (3 - 5)
	 * @return numThrows
	 */
	private int hundredReduce(int numThrows, int d1, int d2, int d3, int num) {
		if (d1 > num) {
			if (d2 == num) {
				numThrows -= d3 * 6; // 6x: {d1}{num}{1 - num}
				if (d3 == num) numThrows += 3; // 3x: {d1}{num}{num}
			} else if (d2 > num) {
				numThrows -= (num - 1) * 6 + 3; // 6x: {d1}{num}{1 - {num-1}}, 3x: {d1}{num}{num}
			}
		}
		return numThrows;
	}
	
	private int nonMexVastOne(int v, int d2, int d3) {
		int numThrows = 2 * 1 + 5 * 2; // 1x: {v}{v}{v} 12{v}, 2x: 121 123 124 125 126
		for (int i = 3; i < 7; i++) {
			if (d2 <= i)
				numThrows += 1 + (i - 2) * 2; // {d2}{d2}{v} {d2}{v}{v} {d2}{3-{d2-1}}{v}
		}
		if (d3 == v) // {d2}{v}{v}
			numThrows -= 2; 
		else if (d3 < d2) // {d2}{d3}{v}
			numThrows -= (d3 - 1) * 2; // {d2}{v}{v} {d2}{3-{d2-1}}{v}
		else // {d2}{d2}{v}
			numThrows -= 1 + (d2 - 2) * 2; 
		return numThrows;
	}

	/**
	 * Determine the chance of throwing higher than the supplied dice results.
	 * Take in account that one or two of the dice may be held 'vast'.
	 */
	private float determineChanceHigher(int d1, boolean v1, int d2, boolean v2, int d3, boolean v3) {
		// Mexes
		int mex = isMex(d1, d2, d3); 
		if (mex > 0) {
			// Two vast
			if (v1 && v2) return mexVastTwo(d3, mex) / 6f;
			if (v2 && v3) return mexVastTwo(d1, mex) / 6f;
			if (v1 && v3) return mexVastTwo(d2, mex) / 6f;
			
			// One vast
			if (v1) return mexVastOne(d1, mex) / 36f;
			if (v2) return mexVastOne(d2, mex) / 36f;
			if (v3) return mexVastOne(d3, mex) / 36f;
			
			// Mexes 3 - 6 have 6 possible throws, 1 and 2 have 3 possible throws
			int numThrows = 0; // Mex has nothing higher
			if (mex == 1) {
				numThrows += 3;
				mex++; // We have taken in account mex 2
			}
			numThrows += (6 - mex) * 6; // Add for each mex 6
			return numThrows / 216f;
		}
		
		// Thousands
		if (d1 == d2 && d2 == d3) {
			if (v1 && v2 || v2 && v3 || v1 && v3) { // Two vast
				if (d1 == 1 || d1 == 2)
					return 1 / 6f; // 111 -> 112, 222 -> 221 
				return 0f; // 3 - 6 have no chance
			}
			if (v1 || v2 || v3) { // One vast
				if (d1 == 1 || d1 == 2)
					return 11 / 36f; // 1 needs only a 2, 2 needs only a 1
				return 2 / 36f; // 3 - 6 can be owned with 21
			}
			int numThrows = 2 * 3 + 4 * 6; // Mex 1,2 and Mex 3 - 6
			numThrows += (6 - d1); // Count each higher thousand
			return numThrows / 216f;
		}
		
		// Hundreds
		// Order the numbers from first vast, then high to low
		// make sure the vast correspond to the numbers
		int nums[] = new int[7];
		int bools[] = new int[7];
		nums[d1]++; nums[d2]++; nums[d3]++;
		if (v1) bools[d1]++; if (v2) bools[d2]++;  if (v3) bools[d3]++;
		int counter = 0;
		for (int i = 6; i > 0; i--) {
			if (bools[i]-- > 0) { // Get the vast first
				switch (counter++) { // if bools[i] > 0 then nums[i] > 0
				case 0: d1 = i; v1 = (nums[i]-- > 0); break;
				case 1: d2 = i; v2 = (nums[i]-- > 0); break;
				}
				i++;
			}
		}
		for (int i = 6; i > 0; i--) {
			if (nums[i]-- > 0) {
				switch (counter++) { // Get the nums
				case 0: d1 = i; v1 = (bools[i]-- > 0); break;
				case 1: d2 = i; v2 = (bools[i]-- > 0); break;
				case 2: d3 = i; v3 = (bools[i]-- > 0); break;
				}
				i++;
			}
		}
		
		int numThrows = 0;
		// Two vast
		if (v1 && v2) {
			// Higher is always better
			numThrows += 6 - d3; //{d1}{d2}{{d3+1}-6}
			// Not already counted
			if (d1 == 2 || d2 == 2) // d3 >= 2
				numThrows++; // 1 is mex
			if (d1 == 1 || d2 == 1 && d3 >= 3)
				numThrows++; // 2 is mex
			if (d1 == d2 && d3 > d1) // Thousands
				numThrows++;
			return numThrows / 6f;
		}
		
		// One vast
		if (v1) {
			if (d1 <= 2) // d1 == 1 or d1 == 2
				// 3{d1}{d1} 33{d1} 4{d1}{d1} 43{d1} 44{d1} 5{d1}{d1} 53{d1} 54{d1} 55{d1} 
				// 6{d1}{d1} 63{d1} 64{d1} 65{d1} 66{d1}
				return nonMexVastOne(d1, d2, d3) / 36f;
			numThrows = 1 + 2; // {d1}{d1}{d1} 21{d1}
			// 311 322 331 332 431 432 433 443 531 532 533 543 553 631 632 633 643 653 663
			// 411 422 431 432 433 441 442 443 541 542 543 544 554 641 642 643 644 654 664 
			// 511 522 531 532 533 541 542 543 544 551 552 553 554 651 652 653 654 655 665
			// 611 622 631 632 633 641 642 643 644 651 652 653 654 655 661 662 663 664 665 
			for (int i = d2; i < 7; i++) {
				numThrows += (i - 1) * 2; // 2x: {d1}{i}{1-{i-1}}
				if (d1 != i)
					numThrows += 1; // 1x: {d}{i}{i}
			}
			if (d2 <= 2) {
				numThrows += 2 - d2; // {d1}11 {d1}22
			} else {
				numThrows -= d3 * 2; // {d1}{d2}{1-{d3}}
				if (d2 == d3)
					numThrows++; // Compensate for d3 * 2
			}
			return numThrows / 36f;
		}
		
		// No vast
		numThrows = 6 + 2 * 3 + 4 * 6; // Thousands, 21{1,2}, 21{3-6}
		// d1 != 1 or 2
		
		// 3x: 311 322 331 332
		// 6x: 431 432, 3x: 411 422 433 441 442 443
		// 6x: 531 532 541 542 543, 3x: 511 522 533 544 551 552 553 554
		// 6x: 631 632 641 642 643 651 652 653 654, 3x: 611 622 633 644 655 661 662 663 664 665
		int inc = 0;
		for (int i = 3; i < 7; i++) {
			if (d1 <= i)
				numThrows += inc * 6 + (i - 1) * 2 * 3; // Ex 6: 9 * 6 + 10 * 3
			inc += i - 1;
		}
		
		if (d2 == 1) 
			numThrows -= 3; // 3x: {d1}11
		else // d2 > 1 
			numThrows -= 6; // 3x: {d1}11 {d1}22
		
		for (int i = 3; i < 6; i++)
			numThrows = hundredReduce(numThrows, d1, d2, d3, i); // reduce for lower d2
		
		if (d1 == d2)
			numThrows -= d3 * 3; // {d1}{d1}{1-d3}
		
		return numThrows / 216f;
	}

	@Override
	protected boolean isSpecialVastCase() {
		return (isMex(getNumber(0), getNumber(1), getNumber(2)) == 6); // Cannot call vast on Mex 6
	}

	@Override
	protected int getHighestVastNumber() {
		return 6;
	}

	@Override
	protected int numDice() {
		return 3;
	}
}