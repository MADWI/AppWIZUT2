package pl.edu.zut.mad.appwizut2;

/**
 * Created by macko on 04.11.2015.
 */
public class ListItemContainer {
    private String title;
    private String date;
    private String author;
    private String body;

    ListItemContainer() {
        this.title = "";
        this.date = "";
        this.author = "";
        this.body = "";
    }

    ListItemContainer(String title, String date, String body, String author) {
        this.title = title;
        this.date = date;
        this.author = author;
        this.body = body;
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
}
