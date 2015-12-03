package pl.edu.zut.mad.appwizut2.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import pl.edu.zut.mad.appwizut2.R;


/**
 * Placeholder fragment for use when we don't have appropriate fragment written
 */
public class PlaceholderFragment extends Fragment {
    // Text at top of placeholder fragment
    private static final String ARG_PLACEHOLDER_TITLE = "placeholder_title";
    private String mPlaceholderTitle;

    private static final String FILLER_TEXT = new String(new byte[100]).replace("\0", "\nABCD");

    public PlaceholderFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static Bundle makeArguments(String placeholderTitle) {
        Bundle args = new Bundle();
        args.putString(ARG_PLACEHOLDER_TITLE, placeholderTitle);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlaceholderTitle = getArguments().getString(ARG_PLACEHOLDER_TITLE);
        }

        //caldroidFragment = new CaldroidFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        ((TextView) view.findViewById(R.id.placeholder_text)).setText(mPlaceholderTitle + FILLER_TEXT);
        return view;
    }
}
