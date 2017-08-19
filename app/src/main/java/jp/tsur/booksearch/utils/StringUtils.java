package jp.tsur.booksearch.utils;

import android.net.Uri;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StringUtils {

    private static final String URL_CHIL_CHIL_SCHEME = "https";
    private static final String URL_CHIL_CHIL_AUTHORITY = "www.chil-chil.net";
    private static final String URL_CHIL_CHIL_PATH = "goodsList/freeword";
    private static final String URL_CHIL_CHIL_QUERY = "freeword";
    private static final String URL_GOOD_READS = "https://www.goodreads.com/book/isbn/";

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

    /**
     * 本のタイトルをちるちるの検索URLにする
     * <p>
     * ワンダーフォーゲル(CHARAコミックス)
     * -> https://www.chil-chil.net/goodsList/freeword/?freeword=ワンダーフォーゲル
     *
     * @param title 本のタイトル
     * @return ちるちるの検索URL
     */
    public static Uri toChilChilUri(String title) {
        // タイトルだけあればいいから、"(" より前があればOK
        //  Uri.parse() だとクエリー部分までエンコードされてうまくいかなかった
        return new Uri.Builder()
                .scheme(URL_CHIL_CHIL_SCHEME)
                .authority(URL_CHIL_CHIL_AUTHORITY)
                .path(URL_CHIL_CHIL_PATH)
                .appendQueryParameter(URL_CHIL_CHIL_QUERY, title.substring(0, title.indexOf("(")))
                .build();
    }

    public static Uri toGoodreads(String isbn) {
        return Uri.parse(URL_GOOD_READS + isbn);
    }
}
