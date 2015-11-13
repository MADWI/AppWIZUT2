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
public class PlanChangesOfflineHandler {
    Context context;

    public PlanChangesOfflineHandler(Context ctx){
        this.context = ctx;
    }

    void setContext(Context ctx){
        this.context = context;
    }

    /**
     *
     * @return ostatnia zmiana w planie jaka przechowujemy
     */
    public MessagePlanChanges lastOfflineMessage(){

        ArrayList<MessagePlanChanges> offlineData = getMessagesData();
            if (offlineData != null && offlineData.size() >= 1){
                return offlineData.get(0);
            }

        return null;
    }

    /**
     *
     * @param data
     * @return true jesli poprwanie udalo sie zapisac dane
     */
    public  boolean saveMessagesData(ArrayList<MessagePlanChanges> data){

        File documents = context.getFilesDir();

        File offlineMessagesFile = new File(documents,Constans.OFFLINE_DATA_FOLDER + "/Messages");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(offlineMessagesFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
            fileOutputStream.close();
            return true;
        }catch (FileNotFoundException e){

        }catch (IOException e){

        }
        return false;

    }

    /**
     *
     * @return posiadane offline zmiany w planie
     */
    public  ArrayList<MessagePlanChanges> getMessagesData(){
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
        ArrayList<MessagePlanChanges> offlineData = getMessagesData();
        ArrayList<MessagePlanChanges> updatedData = new ArrayList<>();

        if (offlineData != null) {
            if (!message.compares(offlineData.get(0)))
                updatedData.add(message);

            for (MessagePlanChanges previous : offlineData)
                updatedData.add(previous);
        }else {
            updatedData.add(message);
        }


        return saveMessagesData(updatedData);
    }
}
