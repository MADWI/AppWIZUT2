package pl.edu.zut.mad.appwizut2.models;

import java.io.Serializable;
import java.util.GregorianCalendar;

/**
 *
 * @author Sebastian Swierczek
 * @version 1.0.0
 */

/* Serializable - aby lepiej zapisywać dane w plikach */
public class DayParity implements Serializable {

	private String date;
	private String parity;
	private String dayName;
	private int mEventsCount;
	private GregorianCalendar gregorianCal;

	public DayParity() {

		setDate("");
		setParity("");
		setDayName("");
        setEventsCount(0);
		setGregorianCal(null);
	}

	public DayParity(String date, String parity, String dayName,
					 int eventsCount, GregorianCalendar gregorianCal) {

		this.setDate(date);
		this.setParity(parity);
		this.setDayName(dayName);
        this.setEventsCount(eventsCount);
		this.setGregorianCal(gregorianCal);
	}


	public int getEventsCount() {
		return mEventsCount;
	}

	public void setEventsCount(int eventsCount) {
		mEventsCount = eventsCount;
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



}