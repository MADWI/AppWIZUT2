package pl.edu.zut.mad.appwizut2.fragments;

import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.BusHours;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.utils.Constants;
import pl.edu.zut.mad.appwizut2.utils.SelectedBuses;

/**
 * Created by barto on 23/11/2015.
 */
public class BusTimetableFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, BaseDataLoader.DataLoadedListener<List<BusHours>> {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    private View mDataNotAvailableView;
    private RecyclerView mRecyclerView;

    private BusTimetableLoader mLoader;
    private final BusAdapter mAdapter = new BusAdapter();
    private Snackbar mSnackbar;
    private FloatingActionButton mFab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set activity title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_public_transport);

        // Inflate view and find views
        View view = inflater.inflate(R.layout.bus_list_layout, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = (ProgressBar) view.findViewById(R.id.item_list_progress_bar);
        mDataNotAvailableView = view.findViewById(R.id.data_not_available);
        mRecyclerView = (RecyclerView) view.findViewById(android.R.id.list);

        // Configure RecyclerView
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new TouchHelperCallback());
        touchHelper.attachToRecyclerView(mRecyclerView);

        // Add FAB
        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AddBusChooseLineFragment().show(getFragmentManager(), "AddBusChoLine");
            }
        });
        mFab.show();

        // Create and register loader
        mLoader = DataLoadingManager.getInstance(getContext()).getLoader(BusTimetableLoader.class);
        mLoader.registerAndLoad(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        // Remove FAB
        if (mFab != null) {
            mFab.setOnClickListener(null);
            mFab.hide();
            mFab = null;
        }

        // Unregister from loader
        mLoader.unregister(this);

        // Dismiss Snackbar
        if (mSnackbar != null && isRemoving()) {
            mSnackbar.dismiss();
        }
        super.onDestroyView();
    }

    public void onRefresh(){
        mSwipeRefreshLayout.setRefreshing(true);
        mLoader.requestRefresh();
    }

    private void clearProgressBar(){
        mDataNotAvailableView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDataLoaded(List<BusHours> buses) {
        if (buses == null) {
            clearProgressBar();
            mDataNotAvailableView.setVisibility(View.VISIBLE);
            return;
        }

        GregorianCalendar today = new GregorianCalendar();
        int currentMinute = today.get(Calendar.HOUR_OF_DAY) * 60 + today.get(Calendar.MINUTE);


        List<BusInfo> departuresForDisplay = new ArrayList<>();
        for (BusHours busHours : buses) {
            int[] rawDepartures = busHours.getHoursForDay(today);
            if (rawDepartures == null) {
                continue;
            }

            int numberOfTimesToDisplay = Constants.DISPLAYED_DEPARTURES_COUNT;

            // Find first departure after now
            int i = Arrays.binarySearch(rawDepartures, currentMinute);
            if (i < 0) {
                i = ~i;
            }

            StringBuilder departureHours = new StringBuilder();
            boolean firstHour = true;

            // Convert data to human readable h:mm
            for (; i < rawDepartures.length && numberOfTimesToDisplay > 0; i++, numberOfTimesToDisplay--) {
                if (firstHour) {
                    firstHour = false;
                } else {
                    departureHours.append(' ');
                }
                departureHours.append(BusHours.minuteNrToHhMmString(rawDepartures[i]));
            }

            // TODO: Do we want to show next day upcoming departures here?

            // Wrap results for display
            BusInfo busForDisplay = new BusInfo();
            busForDisplay.idInApi = busHours.getStop().getIdInApi();
            busForDisplay.line = busHours.getStop().getLineName();
            busForDisplay.fromTo = busHours.getStop().getFromTo();
            busForDisplay.hourInfo = departureHours.toString();
            departuresForDisplay.add(busForDisplay);
        }

        // Put results in ListView
        mAdapter.mItems = departuresForDisplay;
        mAdapter.notifyDataSetChanged();

        clearProgressBar();

        // Dismiss Snackbar
        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }
    }

    private static class BusInfo {
        int idInApi;
        String line;
        String fromTo;
        String hourInfo;
    }

    private static class BusAdapter extends RecyclerView.Adapter<BusViewHolder> {

        List<BusInfo> mItems = Collections.emptyList();

        @Override
        public BusViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BusViewHolder(
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.bus_timetable_layout, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(BusViewHolder holder, int position) {
            BusInfo busInfo = mItems.get(position);
            holder.mBusIdInApi = busInfo.idInApi;
            holder.mLineTextView.setText(busInfo.line);
            holder.mFromToTextView.setText(busInfo.fromTo);
            holder.mHoursTextView.setText(busInfo.hourInfo);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private static class BusViewHolder extends RecyclerView.ViewHolder {

        int mBusIdInApi;
        TextView mLineTextView;
        TextView mFromToTextView;
        TextView mHoursTextView;

        BusViewHolder(View itemView) {
            super(itemView);
            mLineTextView = (TextView) itemView.findViewById(R.id.line);
            mFromToTextView = (TextView) itemView.findViewById(R.id.type);
            mHoursTextView = (TextView) itemView.findViewById(R.id.hour);
        }
    }

    private class TouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(
                    // Drag
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    // Swipe away (But only if there are at least two items, to not make list empty)
                    mAdapter.mItems.size() >= 2 ? ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT : 0
            );
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // Move in displayed list
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            Collections.swap(mAdapter.mItems, fromPosition, toPosition);
            mAdapter.notifyItemMoved(fromPosition, toPosition);

            // Move in underlying list used by loader
            SelectedBuses.moveBusInList(
                    getContext(),
                    ((BusViewHolder) viewHolder).mBusIdInApi,
                    ((BusViewHolder) target).mBusIdInApi
            );

            // Dismiss Snackbar
            if (mSnackbar != null) {
                mSnackbar.dismiss();
            }
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // Not currently supported
            final int position = viewHolder.getAdapterPosition();

            // Back-up for undo
            final ArrayList<BusInfo> oldList = new ArrayList<>(mAdapter.mItems);
            final SelectedBuses.Reverter reverter = new SelectedBuses.Reverter(getContext());

            // Remove
            mAdapter.mItems.remove(position);
            mAdapter.notifyItemRemoved(position);
            SelectedBuses.removeBusStop(getContext(), ((BusViewHolder) viewHolder).mBusIdInApi);

            // Show undo action
            mSnackbar = Snackbar
                    .make(mRecyclerView, R.string.remove_bus_message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.remove_bus_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Undo remove
                            mAdapter.mItems = oldList;
                            mAdapter.notifyItemInserted(position);
                            reverter.revert();
                        }
                    })
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (mSnackbar == snackbar) {
                                mSnackbar = null;
                            }
                        }
                    });
            mSnackbar.show();
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    boolean isRaised = isCurrentlyActive || dY != 0;
                    viewHolder.itemView.animate()
                            .setDuration(getResources().getInteger(R.integer.card_drag_raise_duration))
                            .translationZ(isRaised ? getResources().getDimension(R.dimen.card_drag_raise_translate_z) : 0f)
                            .start();
                }
            }
        }
    }
}
