package nl.vincentketelaars.mexen.general;

import java.util.Calendar;

public class StaticOperations {

	public static Calendar max(Calendar a, Calendar b) {
        return a.after(b) ? a : b;
	}
	
	public static Calendar originTime() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(0);
		return c;
	}
}
