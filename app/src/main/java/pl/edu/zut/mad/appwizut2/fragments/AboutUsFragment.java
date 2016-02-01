package pl.edu.zut.mad.appwizut2.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.views.PuzzleImageView;

/**
 * Created by dawid on 02.12.15.
 */
public class AboutUsFragment extends Fragment implements PuzzleImageView.OnSolvedListener {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.about_us_layout, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_about_us);

        ((PuzzleImageView) v.findViewById(R.id.mad_team_photo)).setOnSolvedListener(this);

        return v;
    }

    @Override
    public void onPuzzleSolved(int difficulty) {
        // TODO
        Log.v("AboutUsFragment", "Solved at difficulty " + difficulty);
    }
}
