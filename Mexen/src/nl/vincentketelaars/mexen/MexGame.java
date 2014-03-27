package nl.vincentketelaars.mexen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MexGame extends Activity implements OnClickListener {
	
	Button mexButton; 
	Button blindMexButton; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mex_game);
        
        mexButton = (Button) findViewById(R.id.mex_button);
        mexButton.setOnClickListener(this);
        blindMexButton = (Button) findViewById(R.id.blind_mex_button);
        blindMexButton.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, RollDice.class);
		intent.putExtra("game", v.getId());
		startActivity(intent);
	}

}
