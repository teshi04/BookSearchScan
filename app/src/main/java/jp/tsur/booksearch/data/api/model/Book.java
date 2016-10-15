package jp.tsur.booksearch.data.api.model;

import java.io.Serializable;

public class Book implements Serializable {

    private String isbn;
    private String title;
    private String author;
    private String publicationDate;
    private String url;
    private boolean existsKindle;

    public Book(String isbn, String title, String author, String publicationDate, String url, boolean existsKindle) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.url = url;
        this.publicationDate = publicationDate;
        this.existsKindle = existsKindle;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublicationDate() {
        return publicationDate;
    }

    public String getTitle() {
        return title;
    }

    public boolean isExistsKindle() {
        return existsKindle;
    }
}
