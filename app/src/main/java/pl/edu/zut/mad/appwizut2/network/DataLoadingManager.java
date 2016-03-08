package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Class responsible for managing all {@link BaseDataLoader} instances
 * and dispatching broadcasts to them
 */
// TODO: We currently don't handle network broadcasts
@MainThread
public class DataLoadingManager {

    /**
     * Get instance of this singleton
     */
    public static DataLoadingManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataLoadingManager(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Get loader managed by this class
     */
    @SuppressWarnings("unchecked")
    public <L extends BaseDataLoader> L getLoader(Class<L> loaderClass) {
        // Get from map
        L loader = (L) mRegisteredLoaders.get(loaderClass);

        // Create new if not in map
        if (loader == null) {
            try {
                loader = loaderClass.getDeclaredConstructor(DataLoadingManager.class).newInstance(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            mRegisteredLoaders.put(loaderClass, loader);
        }
        return loader;
    }

    /**
     * Constructor for this singleton
     *
     * @param applicationContext The context of application (this must not be activity context)
     */
    private DataLoadingManager(Context applicationContext) {
        mContext = applicationContext;
    }

    private static DataLoadingManager sInstance;

    /**
     * Map storing registered loaders; loaders are registered here when they are first used
     */
    private final Map<Class<? extends BaseDataLoader>, BaseDataLoader> mRegisteredLoaders = new ArrayMap<>();

    /**
     * The application context
     *
     * (not activity Context, don't try creating views using this one)
     */
    final Context mContext;

    /**
     * Notify all loaders that settings have changed
     */
    public void dispatchSettingsChanged() {
        for (BaseDataLoader loader : mRegisteredLoaders.values()) {
            loader.onSettingsChanged();
        }
    }

    public interface MultipleOneshotLoadCallback {
        void onLoaded(Object[] results);
    }

    /**
     * Load asynchronously data from multiple loaders
     *
     * Results will be passed to {@link MultipleOneshotLoadCallback#onLoaded(Object[])},
     * with every array item corresponding to loader specified here in loaderClasses param.
     */
    public void multipleOneshotLoad(Class<? extends BaseDataLoader>[] loaderClasses, final MultipleOneshotLoadCallback callback) {
        final int loadersCount = loaderClasses.length;

        // The collected results from loaders
        final Object[] results = new Object[loadersCount];

        // Single element array containing in only element
        // a number of loaders that returned result
        // when this value reached loadersCount we're done
        final int[] sharedLoadedValue = new int[1];

        for (int i = 0; i < loadersCount; i++) {
            //
            final BaseDataLoader loader = getLoader(loaderClasses[i]);
            final int loaderIndex = i;
            loader.registerAndLoad(new BaseDataLoader.DataLoadedListener() {
                boolean mSeenResult;

                @Override
                public void onDataLoaded(Object o) {
                    // Run handler only once
                    if (mSeenResult) {
                        return;
                    }
                    mSeenResult = true;

                    // Unregister from this loader
                    // Post because this is called from iteration over callbacks list,
                    // so modifying it right now would cause ConcurrentModificationException
                    // (All modifications are done from main thread, so no "synchronized")
                    final BaseDataLoader.DataLoadedListener thisCallback = this;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            loader.unregister(thisCallback);
                        }
                    });


                    // Put result in array
                    results[loaderIndex] = o;

                    // Increment loaded counter
                    int loaded = ++sharedLoadedValue[0];
                    if (loaded == loadersCount) {
                        callback.onLoaded(results);
                    }
                }
            });
        }
    }
}
