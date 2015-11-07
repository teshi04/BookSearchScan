package jp.tsur.booksearch.utils;

import android.net.Uri;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StringUtils {

    private static final String URL_CHIL_CHIL = "http://www.chil-chil.net/sp/goodsList/?freeword=";

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
}
