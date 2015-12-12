package pl.edu.zut.mad.appwizut2.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author  Damian Malarczyk created on 10.12.15.
 */
public class BusTimetableModel implements Serializable{

    String lineNumber;
    String lineInfo;

    String lineLink;

    HashMap<String,HashMap<Integer,ArrayList<Integer>>> info;
    public BusTimetableModel(String lineNumber,String lineInfo,String lineLink,HashMap<String,HashMap<Integer,ArrayList<Integer>>> info){
        this.lineNumber = lineNumber;
        this.lineInfo = lineInfo;
        this.lineLink = lineLink;
        this.info = info;
    }

    public BusTimetableModel(String lineNumber,String lineInfo,String lineLink){
        this.lineNumber = lineNumber;
        this.lineInfo = lineInfo;
        this.lineLink = lineLink;

    }

    public String getLineNumber(){
        return this.lineNumber;
    }

    /**
     *
     * @return map of days contains map of hours, where each element stores it's own List of minutes
     */

    public HashMap<String, HashMap<Integer, ArrayList<Integer>>> getInfo() {
        return info;
    }



    public void setInfo(HashMap<String, HashMap<Integer, ArrayList<Integer>>> info) {
        this.info = info;
    }

    public String getLineInfo() {
        return lineInfo;
    }

    public String getLineLink() {
        return lineLink;
    }



}
