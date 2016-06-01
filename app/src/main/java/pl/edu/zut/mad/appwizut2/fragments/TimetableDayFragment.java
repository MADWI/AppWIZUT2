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
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.activities.WebPlanActivity;
import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.ScheduleCommonLoader;
import pl.edu.zut.mad.appwizut2.utils.Constants;

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

    private Date mDate;
    private RecyclerView mRecyclerView;
    private List<Timetable.Hour> mHoursInDay = Collections.emptyList();
    private Adapter mAdapter;
    private Timetable mTimetable;
    private BaseDataLoader<Timetable, ?> mLoader;
    private TextView mNoClassesMessage;
    private Button mImportFromEdziekanatButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle retainableArguments = savedInstanceState != null ? savedInstanceState : getArguments();
        mDate = new Date(retainableArguments.getLong(Constants.ARG_DATE));

        mAdapter = new Adapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.ARG_DATE, mDate.getTime());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // TODO: Show this in UI?
            Log.v(TAG, "Showing schedule for day: " + Constants.FORMATTER.format(mDate));
        }
    }

    private void putDataInView() {
        if (mTimetable == null) {
            return;
        }

        Timetable.Day scheduleDay = mTimetable.getScheduleForDate(mDate);
        if (scheduleDay == null) {
            Log.w(TAG, "Day missing in schedule: " + Constants.FORMATTER.format(mDate));
            mHoursInDay = Collections.emptyList();
            mNoClassesMessage.setVisibility(View.VISIBLE);
        } else {
            mHoursInDay = Arrays.asList(scheduleDay.getTasks());
            mNoClassesMessage.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Set Date for which schedule is to be displayed
     */
    public void setDate(Date date) {
        mDate = date;
        putDataInView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.timetable_page, container, false);

        mNoClassesMessage = (TextView) view.findViewById(R.id.no_classes);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.scheduleList);
        mRecyclerView.setAdapter(mAdapter);

        mImportFromEdziekanatButton = (Button) view.findViewById(R.id.import_from_edziekanat);
        mImportFromEdziekanatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), WebPlanActivity.class));
            }
        });

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
    public static TimetableDayFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putLong(Constants.ARG_DATE, date.getTime());

        TimetableDayFragment fragment = new TimetableDayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDataLoaded(Timetable timetable) {
        mTimetable = timetable;
        if (timetable == null) {
            mImportFromEdziekanatButton.setVisibility(View.VISIBLE);
        } else {
            mImportFromEdziekanatButton.setVisibility(View.GONE);
            putDataInView();
        }
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
            holder.timeTextView.setText(String.format("%d:%02d", hour.getTime().fromHour, hour.getTime().fromMinute));


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
