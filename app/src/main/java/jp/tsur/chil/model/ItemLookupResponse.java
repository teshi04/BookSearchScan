package jp.tsur.chil.model;


import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ItemLookupResponse", strict = false)
public class ItemLookupResponse {

    @Element(name = "Items")
    Items items;

    public Items getItems() {
        return items;
    }

    public void setItems(Items items) {
        this.items = items;
    }
}
