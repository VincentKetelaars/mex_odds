package nl.vincentketelaars.mexen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class MexGame extends Activity implements OnClickListener {
	
	ImageView mexButton; 
	ImageView blindMexButton; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mex_game);
        
        mexButton = (ImageView) findViewById(R.id.mex_2_button);
        mexButton.setOnClickListener(this);
        blindMexButton = (ImageView) findViewById(R.id.mex_3_button);
        blindMexButton.setOnClickListener(this);
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
