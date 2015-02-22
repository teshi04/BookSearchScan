package jp.tsur.chil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ItemAttributes", strict = false)
public class ItemAttributes {

    @Element(name = "Author") //TODO: 著者が複数の場合があるから対応する
    String author;

    @Element(name = "Binding")
    String binding;

    @Element(name = "Title")
    String title;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
