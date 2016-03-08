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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Schedule for a particular day
 *
 * Nested in {@link TimetableFragment}
 *
 * Note: this is child fragment
 *       (so e.g. {@link #onActivityResult(int, int, Intent)} won't work)
 */
public class TimetableDayFragment extends Fragment {
    private static final String TAG = "TimetableDayFragment";

    public static String ARG_DAY = "TDF.Day";

    private int mDay;
    private TimetableFragment mTimetableFragment;
    private RecyclerView mRecyclerView;
    private List<Timetable.Hour> mHoursInDay = Collections.emptyList();
    private Adapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments().getInt(ARG_DAY);
        mAdapter = new Adapter();

        if (getParentFragment() instanceof TimetableFragment) {
            mTimetableFragment = (TimetableFragment) getParentFragment();
            mTimetableFragment.registerDayFragment(this);
        }
    }

    @Override
    public void onDestroy() {
        if (mTimetableFragment != null) {
            mTimetableFragment.unregisterDayFragment(this);
        }
        super.onDestroy();
    }

    void onScheduleAvailable(Timetable timetable) {
        Timetable.Day scheduleDay = timetable.getScheduleForDay(mDay);
        if (scheduleDay == null) {
            Log.w(TAG, "Day missing in schedule");
            return;
        }
        // TODO: Show this in UI?
        Log.v(TAG, "About to display schedule for day: " + Constants.FORMATTER.format(scheduleDay.getDate().getTime()));
        mHoursInDay = Arrays.asList(scheduleDay.getTasks());
        mAdapter.notifyDataSetChanged();
    }

    void onScheduleAvailable(Timetable timetable, Date date) {
        Timetable.Day scheduleDay = timetable.getScheduleForDate(date);
        if (scheduleDay == null) {
            mHoursInDay = Collections.emptyList();
            mAdapter.notifyDataSetChanged();
            return;
        }

        mHoursInDay = Arrays.asList(scheduleDay.getTasks());
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_page, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.scheduleList);
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    public static TimetableDayFragment newInstance(int day) {
        Bundle args = new Bundle();
        args.putInt(ARG_DAY, day);

        TimetableDayFragment fragment = new TimetableDayFragment();
        fragment.setArguments(args);
        return fragment;
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
