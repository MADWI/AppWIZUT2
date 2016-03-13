package pl.edu.zut.mad.appwizut2.network;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;
import pl.edu.zut.mad.appwizut2.utils.Constants;

/**
 * Helper loader class that delegates to {@link ScheduleLoader} or {@link ScheduleEdzLoader}
 */
public class ScheduleCommonLoader extends BaseDataLoader<Timetable, Serializable> implements SharedPreferences.OnSharedPreferenceChangeListener {

    private BaseDataLoader<Timetable, ?> mUnderlyingLoader;

    private List<DataLoadedListener<Timetable>> mCallbacks = new LinkedList<>();
    private SharedPreferences mPreferences;
    private DataLoadingManager mLoadingManager;


    public ScheduleCommonLoader(DataLoadingManager loadingManager) {
        super(loadingManager);
        mLoadingManager = loadingManager;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mUnderlyingLoader = chooseLoaderFromSettings();
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void registerAndLoad(DataLoadedListener<Timetable> callback) {
        mCallbacks.add(callback);
        mUnderlyingLoader.registerAndLoad(callback);
    }

    @Override
    public void unregister(DataLoadedListener<Timetable> callback) {
        mUnderlyingLoader.unregister(callback);
        mCallbacks.remove(callback);
    }

    private BaseDataLoader<Timetable, ?> chooseLoaderFromSettings() {
        if ("edziekanat".equals(mPreferences.getString(Constants.PREF_TIMETABLE_DATA_SOURCE, ""))) {
            return mLoadingManager.getLoader(ScheduleEdzLoader.class);
        } else {
            return mLoadingManager.getLoader(ScheduleLoader.class);
        }
    }

    private void switchLoader(BaseDataLoader<Timetable, ?> loader) {
        // Nothing to do if no loader
        if (loader == mUnderlyingLoader) {
            return;
        }

        // Unregister all
        for (DataLoadedListener<Timetable> callback : mCallbacks) {
            mUnderlyingLoader.unregister(callback);
        }

        // Switch loader
        mUnderlyingLoader = loader;

        // Re-register all
        for (DataLoadedListener<Timetable> callback : mCallbacks) {
            mUnderlyingLoader.registerAndLoad(callback);
        }
    }

    @Override
    public void requestRefresh() {
        // Unsupported
        throw new UnsupportedOperationException();
    }

    /**
     * Check if loader is configured, that is message about choosing group should not be shown
     */
    public boolean isConfigured() {
        return mUnderlyingLoader instanceof ScheduleLoader && ((ScheduleLoader) mUnderlyingLoader).isConfigured();
    }

    // Handle preference change
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switchLoader(chooseLoaderFromSettings());
    }

    // TODO: Extract network-related logic to BaseDataLoader subclass so we won't have to implement these
    // These are not used because we don't call superclass implementation
    @Override
    protected String getCacheName() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Timetable parseData(Serializable serializable) throws JSONException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Serializable doDownload(Serializable cachedData) throws IOException {
        throw new UnsupportedOperationException();
    }
}
