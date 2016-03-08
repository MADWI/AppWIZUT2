package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.utils.IoUtils;

/**
 * Helper class for loading data from network and caching it
 *
 * This base class ensures that:
 *  - data will be reloaded when required so by settings change
 *     Override {@link #cacheIsValidForCurrentSettings(RawData)} to check if cache
 *     is valid for current settings
 *  - no two concurrent tasks to load content will be running
 *  - checking if we are online before downloading data
 *
 *
 * Type parameters:
 *  Data - Parsed data as exposed to users of loader
 *  RawData - Raw data and metadata used for caching, used internally by loader
 */
public abstract class BaseDataLoader<Data, RawData extends Serializable> {
    private static final String TAG = "BaseDataLoader";
    private final DataLoadingManager mLoadingManager;

    /**
     * Time for which this loader has to be without callbacks to don't reuse data from memory
     * In milliseconds
     */
    private static final int BACKGROUND_EXPIRY = 2000;

    /**
     * Time before which we'll use data from {@link #mData} instead of reloading it
     *
     * Time value as returned by {@link System#currentTimeMillis()}
     */
    private long mInMemoryDataExpiresOn;

    /**
     * Data from this loader should be next time persisted
     * even if they doesn't appear to have changed
     */
    private boolean mHasJustManuallySetData;

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
     * @param cacheFile File suggested for use as cache
     */
    @WorkerThread
    protected RawData loadFromCache(File cacheFile) throws IOException {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(cacheFile));
            try {
                return ((RawData) ois.readObject());
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new IOException(e);
            }
        } finally {
            IoUtils.closeQuietly(ois);
        }
    }

    /**
     * Save data to cache
     *
     * Called only once during class lifetime
     *
     * @param cacheFile File suggested for use as cache
     */
    @WorkerThread
    protected void saveToCache(RawData rawData, File cacheFile) throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(cacheFile));
            oos.writeObject(rawData);
        } finally {
            IoUtils.closeQuietly(oos);
        }
    }

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
     * Note that callback may be invoked immediately after or during this method call,
     * only call this when you're ready to receive it
     *
     * Callback registered here must be later unregistered with {@link #unregister(DataLoadedListener)}
     */
    @MainThread
    public void registerAndLoad(final DataLoadedListener<Data> callback) {
        registerAndLoad(callback, false);
    }

    /**
     * Asynchronously obtain data and invoke callback when they are ready
     *
     * Note that callback may be invoked immediately after or during this method call,
     * only call this when you're ready to receive it
     *
     * Callback registered here must be later unregistered with {@link #unregister(DataLoadedListener)}
     */
    @MainThread
    public void registerAndLoad(final DataLoadedListener<Data> callback, boolean requestRefresh) {
        boolean isFirstRegistered = mCallbacks.isEmpty();
        mCallbacks.add(callback);

        // Decide whenever we want to load from cache or start a new load task

        // Are we able to load results from cache?
        boolean loadFromCache = mData != null && mLoadTask == null;

        // If refresh was requested, don't load from cache
        if (requestRefresh) {
            loadFromCache = false;
        }

        // We also won't load from cache if this loader was inactive for some time
        loadFromCache &= !isFirstRegistered || mInMemoryDataExpiresOn > System.currentTimeMillis();

        if (loadFromCache) {
            callback.onDataLoaded(mData);
        } else {
            startLoadTaskIfNotStarted();
        }
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
        if (mCallbacks.isEmpty()) {
            mInMemoryDataExpiresOn = System.currentTimeMillis() + BACKGROUND_EXPIRY;
        }
    }

    @MainThread
    private void startLoadTaskIfNotStarted() {
        if (mLoadTask == null) {
            mLoadTask = new LoadTask();
            mLoadTask.execute();
        }
    }

    protected void manuallySetCachedData(RawData data) {
        mRawData = data;
        mData = null;
        mHasJustManuallySetData = true;
        startLoadTaskIfNotStarted();
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
                    Log.v(TAG, "Failed to load cached data", e);
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
                    Log.v(TAG, "Failed to download data", e);
                }
            }

            boolean dataWasUpToDate = downloadedData != null && downloadedData == mRawData;

            // Try to parse downloaded data
            Data parsedData = null;
            if (downloadedData != null) {
                try {
                    parsedData = parseData(downloadedData);
                } catch (JSONException e) {
                    Log.v(TAG, "Couldn't parse just downloaded data", e);

                    // Discard downloaded data so we'll try from cache
                    downloadedData = null;
                }
            }

            // Try to parse cached data in case when parsing downloaded data failed
            if (parsedData == null && mRawData != null && !dataWasUpToDate) {
                try {
                    parsedData = parseData(mRawData);
                } catch (JSONException e) {
                    Log.v(TAG, "Couldn't parse just downloaded data", e);
                }
            }

            // Save cache
            if (downloadedData != null && (mHasJustManuallySetData || downloadedData != mRawData)) {
                mRawData = downloadedData;
                try {
                    saveToCache(mRawData, cacheFile);
                } catch (IOException e) {
                    Log.v(TAG, "Failed to save new data into cache", e);
                }
                mHasJustManuallySetData = false;
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
