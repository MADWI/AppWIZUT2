package pl.edu.zut.mad.appwizut2.models;


// TODO: Better names (especially for 'Hour')
/**
 * The timetable for a (exercise, not laboratory) group
 */
public class Timetable {

    private final Hour[][] mHours;

    public Timetable(Hour[][] hours) {
        mHours = hours;
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

        public TimeRange getTime() {
            return time;
        }
    }

    public Hour[] getScheduleForDay(int day) {
        return mHours[day];
    }

}
