package jp.tsur.chil.model;

import java.io.Serializable;

public class Book implements Serializable {

    private String title;
    private String author;
    private String url;
    private boolean existsKindle;

    public Book(String title, String author, String url, boolean existsKindle) {
        this.title = title;
        this.author = author;
        this.url = url;
        this.existsKindle = existsKindle;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExistsKindle() {
        return existsKindle;
    }
}
