package pl.edu.zut.mad.appwizut2.models;

import java.io.Serializable;

/**
 * Created by macko on 04.11.2015.
 */
public class ListItemContainer implements Serializable {
    private String title;
    private String date;
    private String author;
    private String body;
    private String id;

    public ListItemContainer() {
        this.title = "";
        this.date = "";
        this.author = "";
        this.body = "";
        this.id = "";
    }

    public ListItemContainer(String title, String date, String body, String author, String id) {
        this.title = title;
        this.date = date;
        this.author = author;
        this.body = body;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {return author; }

    public String getBody() {
        String bodyNoImg = body.replaceAll("<img.+?>", "");
        return bodyNoImg;
    }

    public String getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAuthor(String author) { this.author = author; }

    public void setBody(String body) {
        this.body = body;
    }

    public void setId(String id) {
        this.id = id;
    }
}
