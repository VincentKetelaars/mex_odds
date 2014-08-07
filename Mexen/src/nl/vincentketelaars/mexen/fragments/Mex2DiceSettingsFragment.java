package nl.vincentketelaars.mexen.fragments;

import nl.vincentketelaars.mexen.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class Mex2DiceSettingsFragment extends PreferenceFragment {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.mex2preferences);
    }
}