package pl.edu.zut.mad.appwizut2.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import pl.edu.zut.mad.appwizut2.R;
import pl.edu.zut.mad.appwizut2.network.BaseDataLoader;
import pl.edu.zut.mad.appwizut2.network.DataLoadingManager;
import pl.edu.zut.mad.appwizut2.network.SchedulePdfLoader;

/**
 * Dialog for loading and opening schedule pdf
 */
public class OpenSchedulePdfDialogFragment extends DialogFragment implements BaseDataLoader.DataLoadedListener<Uri> {


    private SchedulePdfLoader mLoader;
    private boolean mAlreadyOpenedPdf;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            dismiss();
            return;
        }

        mLoader = DataLoadingManager.getInstance(getActivity()).getLoader(SchedulePdfLoader.class);
        mLoader.registerAndLoad(this);
    }

    @Override
    public void onDestroy() {
        mLoader.unregister(this);
        super.onDestroy();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ProgressDialog(getActivity());
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onDataLoaded(Uri pdfUri) {
        // If we have already opened PDF during this fragment lifetime, don't reopen
        if (mAlreadyOpenedPdf) {
            return;
        }

        // Open in external application
        mAlreadyOpenedPdf = true;
        if (pdfUri == null) {
            Toast.makeText(getActivity(), R.string.cannot_download_pdf, Toast.LENGTH_SHORT).show();
        } else {
            try {
                startActivity(
                        new Intent(Intent.ACTION_VIEW, pdfUri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                );
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), R.string.cannot_open_pdf, Toast.LENGTH_SHORT).show();
            }
        }

        // Dismiss this dialog (we already dismiss ourselves if there's any saved state so loss here is ok)
        dismissAllowingStateLoss();
    }
}
