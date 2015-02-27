package jp.tsur.booksearch.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "ItemAttributes", strict = false)
public class ItemAttributes {

    @ElementList(name = "Author", inline = true)
    List<Author> authorList;

    @Element(name = "Binding")
    String binding;

    @Element(name = "Title")
    String title;

    public List<Author> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
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
