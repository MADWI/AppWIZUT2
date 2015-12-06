package pl.edu.zut.mad.appwizut2.network;

import android.content.Context;
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
}
