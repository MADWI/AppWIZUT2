package pl.edu.zut.mad.appwizut2.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.BusStop;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.HTTPLinks;
import pl.edu.zut.mad.appwizut2.utils.MyTextUtils;
import pl.edu.zut.mad.appwizut2.utils.SelectedBuses;

/**
 * Dialog for selecting bus stop on line, opened after user chooses line in {@link AddBusChooseLineFragment}
 */
public class AddBusChooseStopFragment extends DialogFragment {

    public static final String ARG_LINE_NAME = "ABC-SF.lineName";
    public static final String ARG_LINE_ID = "ABC-SF.lineId";


    private final Adapter mAdapter = new Adapter();
    private List<Object> mListItems;
    private boolean mFailed;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new LoadStopsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mFailed) {
            dismissAllowingStateLoss();
            return null;
        }
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        return mRecyclerView;
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerView != null) {
            // Unregister from observer to avoid memory leak
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        try {
            // Work around dismiss on rotation after setRetainInstance(true)
            // http://stackoverflow.com/a/13596466
            getDialog().setOnDismissListener(null);
        } catch (Exception ignored) {}

        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.add_bus_choose_stop_title, getArguments().getString(ARG_LINE_NAME)));
        return dialog;
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view;
            if (viewType == 1) {
                // Header
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.add_bus_stop_header, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            } else {
                // Button
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.add_bus_stop_item, parent, false);
                return new StopButtonVH((Button) view) {};
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Object item = mListItems.get(position);
            String text;
            if (item instanceof String) {
                // Header
                String direction = (String) item;
                text = getString(R.string.add_bus_direction, direction);
            } else {
                // Button
                BusStop busStop = (BusStop) item;
                text = busStop.getStopName();
                ((StopButtonVH) holder).mBusStop = busStop;
            }
            ((TextView) holder.itemView).setText(text);
        }

        @Override
        public int getItemViewType(int position) {
            // 0 - stop
            // 1 - header
            return mListItems.get(position) instanceof String ? 1 : 0;
        }


        @Override
        public int getItemCount() {
            return mListItems == null ? 0 : mListItems.size();
        }
    }

    private class StopButtonVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        BusStop mBusStop;

        public StopButtonVH(Button itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            boolean added = SelectedBuses.addBusStop(getContext(), mBusStop);
            if (added) {
                DataLoadingManager
                        .getInstance(getContext())
                        .getLoader(BusTimetableLoader.class)
                        .requestRefresh();
                Toast.makeText(getContext(), R.string.add_bus_added, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.add_bus_already_exist, Toast.LENGTH_SHORT).show();
            }
            dismiss();
        }
    }


    private class LoadStopsTask extends AsyncTask<Void, Void, List<Object>> {

        @Override
        protected List<Object> doInBackground(Void... params) {
            try {
                // Get arguments
                Bundle arguments = getArguments();
                int lineId = arguments.getInt(ARG_LINE_ID);
                String lineName = arguments.getString(ARG_LINE_NAME);

                // Download json
                String url = String.format(Locale.US, HTTPLinks.BUS_LINE_ID, lineId);
                String jsonStr = BusTimetableLoader.downloadFromAvrisApi(url);
                JSONArray directions = new JSONObject(jsonStr).getJSONArray("directions");

                // Parse
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < directions.length(); i++) {
                    JSONArray stops = directions.getJSONArray(i);

                    String destinationName =
                            MyTextUtils.capitalizeString(
                                    stops
                                            .getJSONObject(stops.length() - 1)
                                            .getString("name")
                            );

                    // Add header
                    result.add(destinationName);

                    // Add stops
                    for (int j = 0; j < stops.length(); j++) {
                        JSONObject stopJson = stops.getJSONObject(j);
                        result.add(new BusStop(
                                lineName,
                                MyTextUtils.capitalizeString(stopJson.getString("name")),
                                destinationName,
                                stopJson.getInt("id")
                        ));
                    }

                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Object> lines) {
            if (lines == null) {
                mFailed = true;
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, R.string.no_Internet, Toast.LENGTH_SHORT).show();
                }
                dismissAllowingStateLoss();
            } else {
                mListItems = lines;
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
