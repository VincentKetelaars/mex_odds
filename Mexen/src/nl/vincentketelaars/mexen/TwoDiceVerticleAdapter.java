package nl.vincentketelaars.mexen;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TwoDiceVerticleAdapter extends BaseAdapter {
	private final int[] diceImages = new int[] { R.drawable.d1, R.drawable.d2, R.drawable.d3, R.drawable.d4, R.drawable.d5, R.drawable.d6 };
	private Context context;
	private ArrayList<Throw> previousThrows;
 
	public TwoDiceVerticleAdapter(Context context, ArrayList<Throw> previousThrows) {
		this.context = context;
		this.previousThrows = previousThrows;
	}
 
	public View getView(int position, View convertView, ViewGroup parent) { 
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) { 			
			convertView = inflater.inflate(R.layout.vertical_dice, parent, false);
		}
		Throw previous = getItem(position);
		ImageView die1 = (ImageView) convertView.findViewById(R.id.vertical_die1);
		die1.setImageDrawable(context.getResources().getDrawable(diceImages[previous.getNumberOne() - 1]));
		ImageView die2 = (ImageView) convertView.findViewById(R.id.vertical_die2);
		die2.setImageDrawable(context.getResources().getDrawable(diceImages[previous.getNumberTwo() - 1]));
		
		int width = parent.getWidth();
		int height = parent.getHeight();
		int frameWidth = (int) Math.min(width / 10, height * 0.45);
		int frameMargin = (int) Math.round(frameWidth * 0.05);
		LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) die1.getLayoutParams();
		params1.width = frameWidth;
		params1.height = frameWidth; // Square, so width equals height
		params1.setMargins(frameMargin, frameMargin, frameMargin, frameMargin);
		LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) die2.getLayoutParams();
		params2.width = frameWidth;
		params2.height = frameWidth; // Square, so width equals height
		params2.setMargins(frameMargin, frameMargin, frameMargin, frameMargin);
		die1.setLayoutParams(params1);
		die2.setLayoutParams(params2);
 
		return convertView;
	}
 
	@Override
	public int getCount() {
		return previousThrows.size();
	}
 
	@Override
	public Throw getItem(int position) {
		return previousThrows.get(position);
	}
 
	@Override
	public long getItemId(int position) {
		return position;
	} 
	
	public void addThrow(Throw t) {
		previousThrows.add(t);
		this.notifyDataSetChanged();
	}
	
	public void clearThrows() {
		previousThrows = new ArrayList<Throw>();
		this.notifyDataSetChanged();
	}
}
