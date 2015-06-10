package jp.tsur.booksearch.data.api.model;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "Author", strict = false)
public class Author {

    @Text
    private String authorName = null;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

}