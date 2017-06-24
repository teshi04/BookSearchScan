package jp.tsur.booksearch.data.api.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Item", strict = false)
public class Item {

    @Element(name = "ItemAttributes")
    ItemAttributes itemAttributes;

    @Element(name = "DetailPageURL")
    String detailPageURL;

    @Element(name = "LargeImage")
    LargeImage largeImage;

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

    public LargeImage getLargeImage() {
        return largeImage;
    }

    public void setLargeImage(LargeImage largeImage) {
        this.largeImage = largeImage;
    }
}
