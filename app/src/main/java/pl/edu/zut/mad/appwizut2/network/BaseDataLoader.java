package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper class for loading data from network and caching it
 */
public abstract class BaseDataLoader<Data> {
    private final DataLoadingManager mLoadingManager;

    /**
     * Get name for file to be used for caching this data
     *
     * The name will be used for file passed
     * to {@link #loadFromCache(File)} and {@link #saveToCache(File)}
     */
    protected abstract String getCacheName();

    /**
     * Actually download data and store it in fields
     *
     * This will be called after {@link #loadFromCache(File)}.
     *
     * @param skipCache True if user requested refresh
     * @return True if data were modified and should be refreshed in UI and written to cache
     */
    @WorkerThread
    protected abstract boolean doDownload(boolean skipCache);

    /**
     * Load data from cache to class fields
     *
     * Called only once during class lifetime
     *
     * @param cacheFile File suggested for use as cache, not interpreted by base loader
     */
    @WorkerThread
    protected abstract boolean loadFromCache(File cacheFile);

    /**
     * Save data to cache
     *
     * Called only once during class lifetime
     *
     * @param cacheFile File suggested for use as cache, not interpreted by base loader
     */
    @WorkerThread
    protected abstract boolean saveToCache(File cacheFile);

    @WorkerThread
    protected abstract Data getData();

    /**
     * Called when settings have changed, reload them and check if data we have should be updated
     */
    @MainThread
    protected void onSettingsChanged() {}

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
        mForceRefresh.set(true);
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

    private boolean mTriedLoadingFromCache;
    private LoadTask mLoadTask = null;
    private final AtomicBoolean mForceRefresh = new AtomicBoolean();
    private List<DataLoadedListener<Data>> mCallbacks = new LinkedList<>();

    private class LoadTask extends AsyncTask<Void, Void, Data> {

        @Override
        protected Data doInBackground(Void... params) {
            // Load from cache
            File cacheFile = getCacheFile();
            if (!mTriedLoadingFromCache) {
                mTriedLoadingFromCache = true;
                loadFromCache(cacheFile);
            }

            // Download from network
            boolean downloaded = doDownload(mForceRefresh.getAndSet(false));

            // Save to cache
            if (downloaded) {
                saveToCache(cacheFile);
            }

            // Return results
            return getData();
        }
        @Override
        protected void onPostExecute(Data data) {
            mLoadTask = null;
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
