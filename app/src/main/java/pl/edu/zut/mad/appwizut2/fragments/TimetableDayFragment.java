package pl.edu.zut.mad.appwizut2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleCommonLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;
import pl.edu.zut.mad.appwizut2.utils.DateUtils;

/**
 * Schedule for a particular day
 *
 * Nested in {@link TimetableFragment}
 *
 * Note: this is child fragment
 *       (so e.g. {@link #onActivityResult(int, int, Intent)} won't work)
 */
public class TimetableDayFragment extends Fragment implements BaseDataLoader.DataLoadedListener<Timetable> {
    private static final String TAG = "TimetableDayFragment";

    public static final String ARG_DATE = "TDF.Day";

    private Date mDate;
    private RecyclerView mRecyclerView;
    private List<Timetable.Hour> mHoursInDay = Collections.emptyList();
    private Adapter mAdapter;
    private Timetable mTimetable;
    private BaseDataLoader<Timetable, ?> mLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle retainableArguments = savedInstanceState != null ? savedInstanceState : getArguments();
        mDate = new Date(retainableArguments.getLong(ARG_DATE));

        mAdapter = new Adapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_DATE, mDate.getTime());
    }

    private void putDataInView() {
        if (mTimetable == null) {
            return;
        }

        Timetable.Day scheduleDay = mTimetable.getScheduleForDate(mDate);
        if (scheduleDay == null) {
            Log.w(TAG, "Day missing in schedule: " + Constants.FORMATTER.format(mDate));
            mHoursInDay = Collections.emptyList();
            return;
        }
        // TODO: Show this in UI?
        Log.v(TAG, "About to display schedule for day: " + Constants.FORMATTER.format(mDate));
        mHoursInDay = Arrays.asList(scheduleDay.getTasks());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Set Date for which schedule is to be displayed
     */
    public void setDate(Date date) {
        mDate = date;
        putDataInView();
    }

    /**
     * Use {@link #setDate(Date)} instead
     * TODO: Remove this method
     */
    @Deprecated
    void onScheduleAvailable(Timetable timetable, Date date) {
        setDate(date);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_page, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.scheduleList);
        mRecyclerView.setAdapter(mAdapter);

        mLoader = DataLoadingManager
                .getInstance(getContext())
                .getLoader(ScheduleCommonLoader.class);
        mLoader.registerAndLoad(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        mRecyclerView.setAdapter(null);
        mLoader.unregister(this);
        super.onDestroyView();
    }

    /**
     * Create instance of this fragment to show schedule for specified Date
     */
    public static TimetableDayFragment newInstance(Date day) {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, day.getTime());

        TimetableDayFragment fragment = new TimetableDayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create instance of this fragment to show schedule for specified day of week
     */
    public static TimetableDayFragment newInstance(int dayOfWeek) {
        GregorianCalendar calendar = new GregorianCalendar();
        DateUtils.stripTime(calendar);

        int todayWeekDay = calendar.get(Calendar.DAY_OF_WEEK);

        // Rewind calendar to last monday
        int daysFromMonday = todayWeekDay - Calendar.MONDAY;
        if (daysFromMonday < 0) {
            daysFromMonday += 7;
        }
        calendar.add(Calendar.DATE, -daysFromMonday);

        // If we're in weekend skip to next week
        if (todayWeekDay == Calendar.SATURDAY || todayWeekDay == Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, 7);
        }

        // Now move forward to requested day
        calendar.add(Calendar.DATE, dayOfWeek - Calendar.MONDAY);

        return newInstance(calendar.getTime());
    }

    @Override
    public void onDataLoaded(Timetable timetable) {
        mTimetable = timetable;
        putDataInView();
    }

    private static class VH extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView nameTypeTextView;
        TextView roomLecturerTextView;

        public VH(View itemView) {
            super(itemView);
            timeTextView = (TextView) itemView.findViewById(R.id.time);
            nameTypeTextView = (TextView) itemView.findViewById(R.id.name_type);
            roomLecturerTextView = (TextView) itemView.findViewById(R.id.room_lecturer);
        }
    }

    private class Adapter extends RecyclerView.Adapter<VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView =
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.schedule_item, parent, false);
            ((CardView) itemView).setPreventCornerOverlap(false);
            return new VH(itemView);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Timetable.Hour hour = mHoursInDay.get(position);
            holder.timeTextView.setText(hour.getTime().fromHour + ":" + hour.getTime().fromMinute);


            String subjectName = hour.getSubjectName();
            String type = hour.getType();
            if (type == null) {
                holder.nameTypeTextView.setText(subjectName);
            } else {
                holder.nameTypeTextView.setText(subjectName + " (" + type + ")");
            }

            String lecturer = hour.getLecturer();
            String room = hour.getRoom();
            String wg = hour.getRawWG();
            String text = "";
            if (lecturer != null) {
                text += lecturer;
            }
            if (room != null) {
                text += " " + room;
            }
            if (wg != null) {
                text += "\n" + wg;
            }
            holder.roomLecturerTextView.setText(text);
        }

        @Override
        public int getItemCount() {
            return mHoursInDay.size();
        }
    }
}
