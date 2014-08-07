package nl.vincentketelaars.mexen;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class Description extends Activity {
	
	private final String descriptionFileName = "explanation_%s.html";
	TextView descriptionTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_description);
		
		// Make title bar icon clickable, and go home
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		descriptionTextView = (TextView) findViewById(R.id.description_text);
		String description = getDescription();
		descriptionTextView.setText(Html.fromHtml(description));
		descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
	}
	
	private String getDescription() {
		DataInputStream in = null;
		char[] text = new char[10000];
		try {
			InputStream input = getAssets().open(getDescriptionFile());
			in = new DataInputStream(input);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			br.read(text);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return String.copyValueOf(text);
	}
	
	private String getDescriptionFile() {
		if (Locale.getDefault().getLanguage().equals("nl")) {
			return String.format(descriptionFileName, "nl");
		}
		Log.i("Description", "The user prefers this language: " + Locale.getDefault().getLanguage());
		return String.format(descriptionFileName, "eng");
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
