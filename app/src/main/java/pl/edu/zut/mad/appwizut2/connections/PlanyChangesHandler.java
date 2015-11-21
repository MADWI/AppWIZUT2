package pl.edu.zut.mad.appwizut2.connections;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import pl.edu.zut.mad.appwizut2.models.MessagePlanChanges;
import pl.edu.zut.mad.appwizut2.utils.Constans;

/**
 * @author Damian Malarczyk
 */
public class PlanyChangesHandler {
    Context context;

    public PlanyChangesHandler(Context ctx){
        this.context = ctx;
        WeekParityChecker.folderSetup(context);

    }

    void setContext(Context ctx){
        this.context = context;
    }


    private ArrayList<MessagePlanChanges> currentData;
    private boolean fetchedOnline = false;
    private boolean changesMade = false;


    public interface DataCallback {
        void foundData(ArrayList<MessagePlanChanges> data);
    }

    public void getCurrentData(final DataCallback callback,boolean onlyOffline){
        if (onlyOffline || !HttpConnect.isOnline(context)) {
            if (currentData == null)
                currentData = getOfflineMessagesData();
            callback.foundData(currentData);
        }else if (!fetchedOnline && HttpConnect.isOnline(context)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    currentData = new GetPlanChanges().getServerMessages();

                    changesMade = true;
                    fetchedOnline = true;
                    saveChangesIfAny();
                    callback.foundData(currentData);
                }
            }).start();

        }else {
            callback.foundData(currentData);
        }

    }

    /**
     * przeładowanei oryginalnej metody, w celu automatycznego odczytu danych online
     * @param callback
     */
    public void getCurrentData(final DataCallback callback){
         getCurrentData(callback,false);
    }


    private ArrayList<MessagePlanChanges> getCurrentData(){
        if (currentData == null)
            currentData = getOfflineMessagesData();
        return currentData;
    }


    /**
     * umożliwa manualne nadpisanie obecnych danych
     * zalecane jest zapsiane danych po wykonaniu tej metody
     * @param changes
     */
    public void setCurrentOfflineData(ArrayList<MessagePlanChanges> changes){
        this.currentData = changes;
        changesMade = true;
    }


    /**
     * zapis danych do pliku offline jeśli zostały wprowadzone jakieś zmiany
     * @return
     */
    public boolean saveChangesIfAny(){
        if (changesMade)
            return    saveMessagesData();
        return false; //no changes
    }

    public void getLastMessage(final DataCallback callback,boolean onlyOffline){
        if (onlyOffline || !HttpConnect.isOnline(context)){
            ArrayList<MessagePlanChanges> data = getCurrentData();
            if (data != null && data.size() >= 1) {
                ArrayList<MessagePlanChanges> one = new ArrayList<>();
                one.add(data.get(0));
                callback.foundData(one);
            }else {
                callback.foundData(null);
            }

        }else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MessagePlanChanges one = new GetPlanChanges().getLastMessage();

                        ArrayList<MessagePlanChanges> oneRet = new ArrayList<MessagePlanChanges>();
                        if (one != null) {
                            oneRet.add(one);
                            addLastMessage(one);
                        }
                        callback.foundData(oneRet);
                    }
                }).start();

        }
    }

    /**
     * przeładowanei oryginalnej metody, w celu automatycznego odczytu danych online
     * @param callback
     */
    public void getLastMessage(final DataCallback callback){
        getLastMessage(callback, false);
    }



    /**
     *
     *
     * @return true jesli poprwanie udalo sie zapisac dane
     */
    private  boolean saveMessagesData(){

        if (changesMade) {
            File documents = context.getFilesDir();

            File offlineMessagesFile = new File(documents, Constans.OFFLINE_DATA_FOLDER + "/Messages");

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(offlineMessagesFile);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(currentData);
                objectOutputStream.close();
                fileOutputStream.close();
                changesMade = false;
                return true;
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }

        }
        return false;

    }

    /**
     *
     * @return posiadane offline zmiany w planie
     */
    private  ArrayList<MessagePlanChanges> getOfflineMessagesData(){
        File documents = context.getFilesDir();

        File offlineMessagesFile = new File(documents,Constans.OFFLINE_DATA_FOLDER + "/Messages");

        try {
            FileInputStream fileInputStream = new FileInputStream(offlineMessagesFile);
            if (fileInputStream != null) {
                ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
                return ((ArrayList<MessagePlanChanges>) inputStream.readObject());
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return null;

    }

    /**
     * metoda sluzaca do dodania jednej wiadomosci offline
     * przy odswiezaniu widgetu pobieramy tylko jedna wiadomosc,
     * mozemy ja wtedy dopisac do aktualnie juz posiadanych zmian w planie
     *
     * @return true jesli poprawnie dodano nowa zmiane w planie
     */
    private  boolean addLastMessage(MessagePlanChanges message){
        ArrayList<MessagePlanChanges> offlineData = getCurrentData();
        ArrayList<MessagePlanChanges> updatedData = new ArrayList<>();

        if (offlineData != null) {
            if (!message.compares(offlineData.get(0)))
                updatedData.add(message);

            for (MessagePlanChanges previous : offlineData)
                updatedData.add(previous);
        }else {
            updatedData.add(message);
        }

        setCurrentOfflineData(updatedData);
        return saveMessagesData();
    }
}
