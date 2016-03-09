package pl.edu.zut.mad.appwizut2.models;


// TODO: Better names (especially for 'Hour')

import java.util.Date;
import java.util.GregorianCalendar;

import pl.edu.zut.mad.appwizut2.utils.DateUtils;

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
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        DateUtils.stripTime(calendar);

        for (Day day : mDays) {
            if (calendar.equals(day.mDate)) {
                return day;
            }
        }
        return null;
    }

}
