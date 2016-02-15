package pl.edu.zut.mad.appwizut2.fragments;

import android.app.Dialog;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.models.BusStop;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.utils.SelectedBuses;

/**
 * Dialog for selecting bus stop on line, opened after user chooses line in {@link AddBusChooseLineFragment}
 */
// TODO: Don't reload on rotation
public class AddBusChooseStopFragment extends DialogFragment {

    public static final String ARG_LINE_NAME = "ABC-SF.lineName";
    public static final String ARG_LINE_ID = "ABC-SF.lineId";


    private final Adapter mAdapter = new Adapter();
    private List<Object> mListItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new LoadStopsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
        return recyclerView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // TODO: Extract string (with template)
        dialog.setTitle("[TODO: Extract string] Linia " + getArguments().getString(ARG_LINE_NAME));
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
                // TODO: wrap in "Direction %s" (and extract string)
                text = (String) item;
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
            SelectedBuses.addBusStop(getContext(), mBusStop);
            // TODO: Check if already exist (in addBusStop)
            DataLoadingManager
                    .getInstance(getContext())
                    .getLoader(BusTimetableLoader.class)
                    .requestRefresh();
            // TODO: Acknowledge (Toast?)
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
                String url = String.format(Locale.US, "http://bus.avris.it/api/linia-%d", lineId);
                String jsonStr = BusTimetableLoader.downloadFromAvrisApi(url);
                JSONArray directions = new JSONObject(jsonStr).getJSONArray("directions");

                // Parse
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < directions.length(); i++) {
                    JSONArray stops = directions.getJSONArray(i);

                    // TODO: Normalize case
                    String destinationName = stops.getJSONObject(stops.length() - 1).getString("name");

                    // Add header
                    result.add(destinationName);

                    // Add stops
                    for (int j = 0; j < stops.length(); j++) {
                        JSONObject stopJson = stops.getJSONObject(j);
                        // TODO: Normalize case
                        result.add(new BusStop(
                                lineName,
                                stopJson.getString("name"),
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
            mListItems = lines;
            mAdapter.notifyDataSetChanged();
        }
    }
}
