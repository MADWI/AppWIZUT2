package pl.edu.zut.mad.appwizut2.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import pl.edu.zut.mad.appwizut2.R;

/**
 * Created by dawid on 02.12.15.
 */
public class AboutUsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.about_us_layout, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.nav_about_us);

        return v;
    }
}
