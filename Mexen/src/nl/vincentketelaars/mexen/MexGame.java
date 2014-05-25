package nl.vincentketelaars.mexen;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MexGame extends Activity implements OnClickListener {
	
	ImageView mexButton; 
	ImageView blindMexButton;
	private TextView chooseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mex_game);
        
		chooseTextView = (TextView) findViewById(R.id.choose_dice_textview);
		
        mexButton = (ImageView) findViewById(R.id.mex_2_button);
        mexButton.setOnClickListener(this);
        blindMexButton = (ImageView) findViewById(R.id.mex_3_button);
        blindMexButton.setOnClickListener(this);
        
		Point size = getSize();
		int width = size.x;
		int height = size.y;
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mexButton.getLayoutParams();
		int dieWidth = (int) Math.min(width * 0.5, height * 0.4); // Each dice 40%
		params.width = dieWidth;
		params.height = dieWidth; // Square, so width equals height
		params.gravity = Gravity.CENTER_HORIZONTAL;
		chooseTextView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		// Measured height is only one line, so guess the number of lines by taking the measured width
		int textViewHeight = chooseTextView.getMeasuredHeight() * (chooseTextView.getMeasuredWidth() / width + 1);
		int frameWidthMargin = width / 20;  // Make sure the dice have no odd edges
		int bottomMargin = height / 8; // Little bump at the bottom to ensure the dice fit
		int frameHeightMargin = (height - textViewHeight - bottomMargin - dieWidth * 2) / 4;
		params.setMargins(frameWidthMargin, frameHeightMargin, frameWidthMargin, frameHeightMargin);
		mexButton.setLayoutParams(params);
		blindMexButton.setLayoutParams(params);
    }
	
	private Point getSize() {
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mex_2_button:
			Intent intent = new Intent(this, Roll2Dice.class);
			startActivity(intent);
			break;
		case R.id.mex_3_button:
			intent = new Intent(this, Roll3Dice.class);
			startActivity(intent);
			break;
		}
	}

}
