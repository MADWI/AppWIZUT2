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

import pl.edu.zut.mad.appwizut2.utils.Interfaces.CompletitionCallback;

/**
 * @author Damian Malarczyk
 */
public class OfflineHandler<T extends Serializable> {

    private Context ctx;

    /**
     * Klucz danych offline
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
     * obecnie posiadane dane
     * uaktalniane wraz wywołaniem metod
     * {@link #getCurrentData(OfflineDataCallback, boolean)}
     *
     */
    private ArrayList<T> currentData;

    /**
     * interfejs do odczytu asynchronicznie odczytanych danych
     *
     */
    public interface OfflineDataCallback<T> {
        void foundData(ArrayList<T> data);
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

    public void setCurrentOfflineData(ArrayList<T> changes){
        this.currentData = changes;

    }

    /**
     *
     * Przeładowana metoda {@link #getCurrentData(OfflineDataCallback, boolean)}
     * w celu synchronicznego odczytania danych
     * @return
     */
    public ArrayList<T> getCurrentData(boolean reload){
        if (!reload && currentData != null){
            return currentData;
        }
        return readCurrentData();

    }

    /**
     * metoda pomocnicza do odczytu danych z plików
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
     * metoda służąca do zapisania posiadanych danych do pliku
     * @param completition
     */
    public  void saveCurrentData(final CompletitionCallback completition){
        final List<T> toSave = currentData;
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                        if (completition != null)
                            completition.finished(true);


                    } catch (Exception e) {
                        if (completition != null)
                            completition.finished(false);
                    }
                }else {
                    completition.finished(true);
                }
            }
        }).start();
    }

    /**
     * ścieżki do danych z plikami na podstawie kluczy
     * @param value
     * @return
     */
    private static String OfflineDataHandlerToPath(OfflineDataHandlerKeys value){
        switch (value) {
            case PLAN_CHANGES:
                return Constans.OFFLINE_DATA_FOLDER + "/zmiany_w_planie";

            case ANNOUNCEMENTS:
                return Constans.OFFLINE_DATA_FOLDER + "/ogloszenia";

            default:
                return "no_path";

        }

    }
    /**
     *Metoda tworząca podstawowe foldery niezbędne do obsługi zarówno parzystości jak i zmian w planie w trybie offline
     *
     *
     */
    public static void folderSetup(Context context){

        File documents = context.getFilesDir();
        new File(documents,Constans.OFFLINE_DATA_FOLDER).mkdirs();
    }

    /**
     * klucze danych offline
     */
    public enum OfflineDataHandlerKeys{
        PLAN_CHANGES,ANNOUNCEMENTS
    }
}
