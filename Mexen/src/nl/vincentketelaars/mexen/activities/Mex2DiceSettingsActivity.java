package nl.vincentketelaars.mexen.activities;

import nl.vincentketelaars.mexen.fragments.Mex2DiceSettingsFragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class Mex2DiceSettingsActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// Make title bar icon clickable, and go home
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, 
        		new Mex2DiceSettingsFragment()).commit();
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
