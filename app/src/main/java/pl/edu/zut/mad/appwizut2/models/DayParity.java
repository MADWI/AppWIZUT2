package pl.edu.zut.mad.appwizut2.models;

import java.util.Comparator;
import java.util.GregorianCalendar;

/**
 *
 * @author Sebastian Swierczek
 * @version 1.0.0
 */
public class DayParity {

	private String date;
	private String parity;
	private String dayName;
	private GregorianCalendar gregorianCal;

	public DayParity() {

		setDate("");
		setParity("");
		setDayName("");
		setGregorianCal(null);
	}

	public DayParity(String date, String parity, String dayName,
					 GregorianCalendar gregorianCal) {

		this.setDate(date);
		this.setParity(parity);
		this.setDayName(dayName);
		this.setGregorianCal(gregorianCal);
	}

	public String getParity() {
		return parity;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDayName() {
		return dayName;
	}

	public void setDayName(String dayName) {
		this.dayName = dayName;
	}

	public GregorianCalendar getGregorianCal() {
		return gregorianCal;
	}

	public void setGregorianCal(GregorianCalendar gregorianCal) {
		this.gregorianCal = gregorianCal;
	}


	// TODO: Make DayParity Comparable itself?
	public static class CustomComparator implements Comparator<DayParity> {
        @Override
        public int compare(DayParity o1, DayParity o2) {
            return o1.getDate().compareTo(o2.getDate());
        }
    }
}