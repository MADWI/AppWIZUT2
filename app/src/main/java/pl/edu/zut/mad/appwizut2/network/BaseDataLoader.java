package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper class for loading data from network and caching it
 *
 * This base class ensures that:
 *  - data will be reloaded when required so by settings change
 *     Override {@link #cacheIsValidForCurrentSettings(Object)} to check if cache
 *     is valid for current settings
 *  - no two concurrent tasks to load content will be running
 *  - checking if we are online before downloading data
 *
 *
 * Type parameters:
 *  Data - Parsed data as exposed to users of loader
 *  RawData - Raw data and metadata used for caching, used internally by loader
 */
public abstract class BaseDataLoader<Data, RawData> {
    private static final String TAG = "BaseDataLoader";
    private final DataLoadingManager mLoadingManager;

    /**
     * Get name for file to be used for caching this data
     *
     * The name will be used for file passed
     * to {@link #loadFromCache(File)} and {@link #saveToCache(RawData, File)}
     */
    protected abstract String getCacheName();

    /**
     * Actually download data and store it in fields
     *
     * This will be called after {@link #loadFromCache(File)}.
     *
     * @return True if data were modified and should be refreshed in UI and written to cache
     */
    @WorkerThread
    protected abstract RawData doDownload(RawData cachedData) throws IOException;

    /**
     * Load data from cache to class fields
     *
     * Called only once during class lifetime
     *
     * @param cacheFile File suggested for use as cache, not interpreted by base loader
     */
    @WorkerThread
    protected abstract RawData loadFromCache(File cacheFile) throws IOException;

    /**
     * Save data to cache
     *
     * Called only once during class lifetime
     *
     * @param cacheFile File suggested for use as cache, not interpreted by base loader
     */
    @WorkerThread
    protected abstract void saveToCache(RawData rawData, File cacheFile) throws IOException;

    @WorkerThread
    protected abstract Data parseData(RawData data) throws JSONException;

    protected boolean cacheIsValidForCurrentSettings(RawData cachedData) {
        return true;
    }

    /**
     * Called when settings have changed, check if data we have should be updated
     */
    @MainThread
    protected void onSettingsChanged() {
        if (!mCallbacks.isEmpty() && (mRawData == null || !cacheIsValidForCurrentSettings(mRawData))) {
            requestRefresh();
        }
    }

    protected BaseDataLoader(DataLoadingManager loadingManager) {
        mLoadingManager = loadingManager;
    }

    /**
     * Get the application context
     */
    protected Context getContext() {
        return mLoadingManager.mContext;
    }

    private File getCacheFile() {
        return getContext().getFileStreamPath(getCacheName());
    }

    /**
     * Asynchronously obtain data and invoke callback when they are ready
     *
     * Callback registered here must be later unregistered with {@link #unregister(DataLoadedListener)}
     */
    @MainThread
    public void registerAndLoad(DataLoadedListener<Data> callback) {
        mCallbacks.add(callback);
        startLoadTaskIfNotStarted();
    }

    /**
     * Refresh data and invoke callbacks when done
     */
    @MainThread
    public void requestRefresh() {
        if (!mCallbacks.isEmpty()) {
            startLoadTaskIfNotStarted();
        }
    }

    /**
     * Unregister from loader
     */
    @MainThread
    public void unregister(DataLoadedListener<Data> callback) {
        mCallbacks.remove(callback);
    }

    @MainThread
    private void startLoadTaskIfNotStarted() {
        if (mLoadTask == null) {
            mLoadTask = new LoadTask();
            mLoadTask.execute();
        }
    }

    /**
     * Not parsed data used by this loader, along with caching metadata
     *
     * Modified on worker thread
     */
    private RawData mRawData;

    /**
     * Parsed data returned by this loader
     *
     * Accessed from main thread
     */
    private Data mData;

    private boolean mTriedLoadingFromCache;
    private LoadTask mLoadTask = null;
    private List<DataLoadedListener<Data>> mCallbacks = new LinkedList<>();

    private class LoadTask extends AsyncTask<Void, Void, Data> {

        @Override
        protected Data doInBackground(Void... params) {
            // Load cache
            File cacheFile = getCacheFile();
            if (!mTriedLoadingFromCache) {
                mTriedLoadingFromCache = true;
                try {
                    mRawData = loadFromCache(cacheFile);
                } catch (FileNotFoundException ignored) {
                    // No cached version
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load cached data", e);
                }
            }

            // Ensure cache matches settings
            if (mRawData != null && !cacheIsValidForCurrentSettings(mRawData)) {
                mRawData = null;
            }

            // Download from network
            RawData downloadedData = null;

            if (HttpConnect.isOnline(getContext())) {
                try {
                    downloadedData = doDownload(mRawData);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to download data", e);
                }
            }

            boolean dataWasUpToDate = downloadedData != null && downloadedData == mRawData;

            // Try to parse downloaded data
            Data parsedData = null;
            if (downloadedData != null) {
                try {
                    parsedData = parseData(downloadedData);
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't parse just downloaded data", e);

                    // Discard downloaded data so we'll try from cache
                    downloadedData = null;
                }
            }

            // Try to parse cached data in case when parsing downloaded data failed
            if (parsedData == null && mRawData != null && !dataWasUpToDate) {
                try {
                    parsedData = parseData(mRawData);
                } catch (JSONException e) {
                    Log.e(TAG, "Couldn't parse just downloaded data", e);
                }
            }

            // Save cache
            if (downloadedData != null && downloadedData != mRawData) {
                mRawData = downloadedData;
                try {
                    saveToCache(mRawData, cacheFile);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save new data into cache", e);
                }
            }

            // Return results
            return parsedData;
        }
        @Override
        protected void onPostExecute(Data data) {
            mLoadTask = null;
            mData = data;
            for (DataLoadedListener<Data> callback : mCallbacks) {
                callback.onDataLoaded(data);
            }
        }

        @Override
        protected void onCancelled(Data data) {
            mLoadTask = null;
        }

    }

    @MainThread
    public interface DataLoadedListener<Data> {
        /**
         * Called when new data arrived
         */
        void onDataLoaded(Data data);
    }

}
