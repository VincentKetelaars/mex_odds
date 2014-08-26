package nl.vincentketelaars.mexen.general;

import java.util.Calendar;

public class StaticOperations {

	public static Calendar max(Calendar a, Calendar b) {
        return a.after(b) ? a : b;
	}
}
