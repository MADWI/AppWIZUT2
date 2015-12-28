package pl.edu.zut.mad.appwizut2.models;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author Sebastian Swierczek
 * @version 1.0.0
 */
public class DayParity implements Comparable<DayParity> {

	private final Parity parity;
	private final GregorianCalendar gregorianCal;

	public DayParity(GregorianCalendar gregorianCal, Parity parity) {
		this.gregorianCal = gregorianCal;
        this.parity = parity;
    }

	public Parity getParity() {
		return parity;
	}

	public Date getDate() {
		return new Date(gregorianCal.getTimeInMillis());
	}

	public GregorianCalendar getGregorianCal() {
		return gregorianCal;
	}

	@Override
	public int compareTo(@NonNull DayParity another) {
		return gregorianCal.compareTo(another.gregorianCal);
	}

    public enum Parity {
        EVEN, // Parzysty
        ODD // Nieparzysty
    }
}