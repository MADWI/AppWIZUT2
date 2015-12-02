package pl.edu.zut.mad.appwizut2;

/**
 * Created by macko on 04.11.2015.
 */
public class ListItemContainer {
    private String title;
    private String date;
    private String author;
    private String body;
    private String id;

    ListItemContainer() {
        this.title = "";
        this.date = "";
        this.author = "";
        this.body = "";
        this.id = "";
    }

    ListItemContainer(String title, String date, String body, String author, String id) {
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
        return body;
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
