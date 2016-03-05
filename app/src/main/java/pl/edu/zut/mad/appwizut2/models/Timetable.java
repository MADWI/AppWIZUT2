package pl.edu.zut.mad.appwizut2.models;


// TODO: Better names (especially for 'Hour')

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The timetable for a (exercise, not laboratory) group
 */
public class Timetable {

    private final Day[] mDays;

    public Timetable(Day[] days) {
        mDays = days;
    }

    /**
     * Time on which task starts and ends
     */
    public static class TimeRange {
        public final int fromHour;
        public final int fromMinute;
        public final int toHour;
        public final int toMinute;

        public TimeRange(int fromHour, int fromMinute, int toHour, int toMinute) {
            this.fromHour = fromHour;
            this.fromMinute = fromMinute;
            this.toHour = toHour;
            this.toMinute = toMinute;
        }
    }

    /**
     * Single task within hour and day
     */
    public static class Hour {
        private final String name;
        private final String rawWG;
        private final String room;
        private final String teacher;
        private final String type;
        private final TimeRange time;

        public Hour(String name, String type, String room, String teacher, String rawWG, TimeRange time) {
            this.name = name;
            this.type = type;
            this.room = room;
            this.teacher = teacher;
            this.rawWG = rawWG;
            this.time = time;
        }

        public String getSubjectName() { return name; }

        public String getRoom() {
            return room;
        }

        public String getLecturer() {
            return teacher;
        }

        public String getType() {
            return type;
        }

        public String getRawWG() {
            return rawWG;
        }

        public TimeRange getTime() {
            return time;
        }
    }

    public static class Day {
        private final GregorianCalendar mDate;
        private final Hour[] mTasks;

        public Day(GregorianCalendar date, Hour[] tasks) {
            mDate = date;
            mTasks = tasks;
        }

        public GregorianCalendar getDate() {
            return mDate;
        }

        public Hour[] getTasks() {
            return mTasks;
        }
    }

    public Day getScheduleForDate(Date date) {
        if (date != null) {
            for (Day day : mDays) {
             //   if (day.mDate.get(Calendar.DATE) == date.getDate()) {
                if (date.compareTo(day.mDate.getTime()) == 0) {
                    return day;
                }
            }
        }
        return null;
    }

    public Day getScheduleForDay(int day) {
        GregorianCalendar today = new GregorianCalendar();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        for (Day checkedDay : mDays) {
            if (checkedDay == null) {
                continue;
            }
            GregorianCalendar checkedDayDate = checkedDay.getDate();
            if (!checkedDayDate.before(today) && checkedDayDate.get(Calendar.DAY_OF_WEEK) == day) {
                return checkedDay;
            }
        }

        // Just check weekday if day is missing
        for (Day checkedDay : mDays) {
            if (checkedDay == null) {
                continue;
            }
            GregorianCalendar checkedDayDate = checkedDay.getDate();
            if (checkedDayDate.get(Calendar.DAY_OF_WEEK) == day) {
                return checkedDay;
            }
        }
        return null;
    }

}
