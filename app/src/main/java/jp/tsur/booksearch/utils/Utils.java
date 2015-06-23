package jp.tsur.booksearch.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import jp.tsur.booksearch.data.api.model.Book;

public class Utils {

    public static ArrayList<Book> getScanHistory(String scanHistory) {
        if (TextUtils.isEmpty(scanHistory)) {
            return new ArrayList<>();
        }
        return new Gson().fromJson(scanHistory, new TypeToken<ArrayList<Book>>() {
        }.getType());
    }

    public static String saveScanHistory(ArrayList<Book> scanHistory) {
        return new Gson().toJson(scanHistory);
    }

}
