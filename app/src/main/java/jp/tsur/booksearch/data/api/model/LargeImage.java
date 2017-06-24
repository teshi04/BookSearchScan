package jp.tsur.booksearch.data.api.model;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "LargeImage", strict = false)
public class LargeImage {

    @Element(name = "URL")
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
