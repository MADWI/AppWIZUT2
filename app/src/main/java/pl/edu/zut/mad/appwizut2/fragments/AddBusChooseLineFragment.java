package pl.edu.zut.mad.appwizut2.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.BusTimetableLoader;

/**
 * Dialog for selecting line to add, will continue to {@link AddBusChooseStopFragment}
 */
// TODO: Don't reload on rotation
public class AddBusChooseLineFragment extends DialogFragment {

    private List<BusLine> mLines;
    private final Adapter mAdapter = new Adapter();
    private boolean mFailed;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new LoadLinesTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mFailed) {
            dismissAllowingStateLoss();
            return null;
        }
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
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
        dialog.setTitle("[TODO: Extract string] Wybierz linie");
        return dialog;
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(new Button(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BusLine line = mLines.get(position);
            ((Button) holder.itemView).setText(line.name);
            ((VH) holder).mLine = line;
        }

        @Override
        public int getItemCount() {
            return mLines == null ? 0 : mLines.size();
        }
    }

    private class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        BusLine mLine;

        public VH(Button lineButton) {
            super(lineButton);
            lineButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Create arguments
            Bundle args = new Bundle();
            args.putInt(AddBusChooseStopFragment.ARG_LINE_ID, mLine.id);
            args.putString(AddBusChooseStopFragment.ARG_LINE_NAME, mLine.name);

            // Create AddBusChooseStopFragment
            AddBusChooseStopFragment fragment = new AddBusChooseStopFragment();
            fragment.setArguments(args);

            // Replace this fragment
            getFragmentManager()
                    .beginTransaction()
                    .remove(AddBusChooseLineFragment.this)
                    .add(fragment, "AddBusChoStop")
                    .commit();
        }
    }

    private static class BusLine {
        int id;
        String name;

        BusLine(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // TODO: Should we use loader here?
    private class LoadLinesTask extends AsyncTask<Void, Void, List<BusLine>> {

        @Override
        protected List<BusLine> doInBackground(Void... params) {
            try {
                String jsonStr = BusTimetableLoader.downloadFromAvrisApi("http://bus.avris.it/api/linie");
                JSONArray categories = new JSONObject(jsonStr).getJSONArray("categories");

                List<BusLine> result = new ArrayList<>();

                // Iterate over categories
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    String categoryName = category.getString("name");
                    if (!( // Excluding "zm" (Changed lines) and "busn" (Night lines)
                            "tram".equals(categoryName) ||
                            "bus".equals(categoryName) ||
                            "busp".equals(categoryName)
                    )) {
                        continue;
                    }

                    // Add all lines from category to result
                    JSONArray lines = category.getJSONArray("lines");
                    for (int j = 0; j < lines.length(); j++) {
                        JSONObject lineJson = lines.getJSONObject(j);
                        result.add(new BusLine(lineJson.getInt("id"), lineJson.getString("line")));
                    }

                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<BusLine> lines) {
            if (lines == null) {
                mFailed = true;
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, R.string.no_Internet, Toast.LENGTH_SHORT).show();
                }
                dismissAllowingStateLoss();
            } else {
                mLines = lines;
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
