package jp.tsur.booksearch.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jp.tsur.booksearch.model.Book;

public class Utils {

    private static final String URL_CHIL_CHIL = "http://www.chil-chil.net/sp/goodsList/?freeword=";
    private static final String PREF_SCAN_HISTORY = "scan_history";
    public static final int SCAN_HISTORY_MAX = 10;


    /**
     * HmacSHA256化する
     *
     * @param target 暗号化したい文字列
     * @param key    公開鍵
     * @return HmacSHA256エンコード化した文字列
     */
    public static String toHmacSHA256(String target, String key) {

        byte[] result;
        try {
            SecretKeySpec sk = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(sk);
            result = mac.doFinal(target.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return Base64.encodeToString(result, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public static String getQuery(ArrayList<NameValuePair> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            try {
                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }


    /**
     * "-" と "_" も URL エンコードする
     *
     * @return URL エンコードした文字列
     */
    public static String urlEncode(String text) {
        try {
            text = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        text = text.replaceAll("_", "%2F");
        text = text.replaceAll("-", "%2B");
        return text;
    }

    public static Uri toChilChilUri(String title) {
        String[] split = title.split(" ");
        return Uri.parse(URL_CHIL_CHIL + split[0]);
    }

    public static boolean isChilChilMode(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean("chilchil_visible", false);
    }

    public static ArrayList<Book> getScanHistory(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String scanHistory = sharedPref.getString(PREF_SCAN_HISTORY, null);
        if (TextUtils.isEmpty(scanHistory)) {
            return new ArrayList<>();
        }
        return new Gson().fromJson(scanHistory, new TypeToken<ArrayList<Book>>() {
        }.getType());
    }

    private static void saveScanHistory(Context context, ArrayList<Book> scanHistory) {
        String json = new Gson().toJson(scanHistory);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_SCAN_HISTORY, json);
        editor.apply();
    }

    public static void addScanHistory(Context context, Book book) {
        ArrayList<Book> scanHistory = Utils.getScanHistory(context);
        scanHistory.add(0, book);
        Utils.saveScanHistory(context, scanHistory);
    }

    public static void removeScanHistory(Context context, int index) {
        ArrayList<Book> scanHistory = Utils.getScanHistory(context);
        scanHistory.remove(index);
        Utils.saveScanHistory(context, scanHistory);
    }
}
