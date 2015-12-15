package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.ListItemContainer;
import pl.edu.zut.mad.appwizut2.utils.Interfaces.CompletitionCallback;

/**
 * @author Damian Malarczyk
 */
public class OfflineHandler<T extends Serializable> {

    private Context ctx;

    /**
     * offline data key
     */
    private final OfflineDataHandlerKeys value;


    public OfflineHandler(Context ctx,OfflineDataHandlerKeys value){
        this.ctx = ctx;
        this.value = value;
        this.ctx = ctx;
        folderSetup(ctx);
    }

    void setContext(Context ctx){
        this.ctx = ctx;
    }

    /**
     * current data stored by the handler
     * updated with call of
     * {@link #getCurrentData(OfflineDataCallback, boolean)} method
     *
     */
    private ArrayList<T> currentData;

    /**
     * interface to provide access for asynchronously loaded data
     *
     */
    public interface OfflineDataCallback<T> {
        void foundData(List<T> data);
    }

    public void setCurrentOfflineData(ArrayList<T> changes){
        this.currentData = changes;

    }


    /**
     *
     *
     * @param reload - reload == false, oznacza zwrócenie obecnie posiadanych danych, o ile już je posiadamy
     *               reload == true, ponowne odczytanie danych offline
     *
     */
    public void getCurrentData(final OfflineDataCallback callback,boolean reload){
        if(!reload && currentData != null){
            callback.foundData(currentData);
        }else {
            callback.foundData(readCurrentData());

        }

    }



    /**
     *
     * overloaded method {@link #getCurrentData(OfflineDataCallback, boolean)}
     *
     * @return
     */
    public ArrayList<T> getCurrentData(boolean reload){
        if (!reload && currentData != null){
            return currentData;
        }
        return readCurrentData();

    }

    /**
     * helper method to read current offline data
     * @return
     */
    private ArrayList<T> readCurrentData(){
        File documents = ctx.getFilesDir();

        File offlineMessagesFile = new File(documents, OfflineDataHandlerToPath(value));

        try {
            FileInputStream fileInputStream = new FileInputStream(offlineMessagesFile);

            ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
            currentData = (ArrayList<T>)inputStream.readObject();
            inputStream.close();
            fileInputStream.close();
            return currentData;

        }catch (Exception e){
            Log.e("read offline error","reading current data error");
            e.printStackTrace();
        }

        return null;

    }


    /**
     * asynchronous use of {@link #saveCurrentData()} method
     * @param completitionCallback
     */
    public void saveCurrentDataAsynchronously(final CompletitionCallback completitionCallback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<T> result = saveCurrentData();
                if (result != null){
                    completitionCallback.finished(true);
                }else {
                    completitionCallback.finished(false);
                }
            }
        }).start();
    }
    /**
     * saving current data
     * (it has to be earlier set if not created earlier {@link #setCurrentOfflineData(ArrayList)}
     */
    public  List<T> saveCurrentData(){
        List<T> toSave = currentData;

        if (toSave != null) {
            File documents = ctx.getFilesDir();

            File offlineMessagesFile = new File(documents,  OfflineDataHandlerToPath(value));
            FileOutputStream fileOutputStream;
            ObjectOutputStream objectOutputStream;
            try {
                fileOutputStream = new FileOutputStream(offlineMessagesFile);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(toSave);
                objectOutputStream.close();
                fileOutputStream.close();
                return toSave;


            } catch (Exception e) {
                 e.printStackTrace();
            }
        }

        return null;

    }

    /**
     * returns path to data files given sepcific data key
     * @param value
     * @return
     */
    private static String OfflineDataHandlerToPath(OfflineDataHandlerKeys value){
        switch (value) {
            case PLAN_CHANGES:
                return Constans.OFFLINE_DATA_FOLDER + "/zmiany_w_planie";

            case ANNOUNCEMENTS:
                return Constans.OFFLINE_DATA_FOLDER + "/ogloszenia";
            case BUS_TIMETABLE:
                return Constans.OFFLINE_DATA_FOLDER + "/bus_info";
            default:
                return "no_path";

        }

    }
    /**
     *Function that sets up folders for offline data
     *
     */
    public static void folderSetup(Context context){

        File documents = context.getFilesDir();
        new File(documents,Constans.OFFLINE_DATA_FOLDER).mkdirs();
    }

    /**
     * offline data key values
     */
    public enum OfflineDataHandlerKeys{
        PLAN_CHANGES,ANNOUNCEMENTS,BUS_TIMETABLE
    }
}
