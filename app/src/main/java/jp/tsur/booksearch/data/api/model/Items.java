package jp.tsur.booksearch.data.api.model;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;

@Root(name = "Items", strict = false)
public class Items {

    @ElementList(name = "Item", inline = true, required = false)
    List<Item> itemList;

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }
}