package jp.tsur.booksearch.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Item", strict = false)
public class Item {

    @Element(name = "ItemAttributes")
    ItemAttributes itemAttributes;

    @Element(name = "DetailPageURL")
    String detailPageURL;

    public ItemAttributes getItemAttributes() {
        return itemAttributes;
    }

    public void setItemAttributes(ItemAttributes itemAttributes) {
        this.itemAttributes = itemAttributes;
    }

    public String getDetailPageURL() {
        return detailPageURL;
    }

    public void setDetailPageURL(String detailPageURL) {
        this.detailPageURL = detailPageURL;
    }
}
