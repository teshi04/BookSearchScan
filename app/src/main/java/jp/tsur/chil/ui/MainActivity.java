package jp.tsur.chil.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.tsur.chil.R;
import jp.tsur.chil.api.AwsApi;
import jp.tsur.chil.api.AwsService;
import jp.tsur.chil.model.Author;
import jp.tsur.chil.model.Item;
import jp.tsur.chil.model.ItemAttributes;
import jp.tsur.chil.model.ItemLookupResponse;
import jp.tsur.chil.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MainActivity extends Activity {

    private static final String AMAZON_URL = "GET\necs.amazonaws.jp\n/onca/xml\n";
    private static final String AMAZON_VERSION = "2011-08-01";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.scan_button)
    void scan() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String isbn = data.getStringExtra("SCAN_RESULT");
            search(isbn);
        }
    }

    private void search(final String isbn) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.JAPAN);
        String timestamp = df.format(new Date());

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("AWSAccessKeyId", getString(R.string.aws_access_key_id)));
        params.add(new BasicNameValuePair("AssociateTag", getString(R.string.amazon_associate_tag)));
        params.add(new BasicNameValuePair("IdType", "ISBN"));
        params.add(new BasicNameValuePair("ItemId", isbn));
        params.add(new BasicNameValuePair("Operation", "ItemLookup"));
        params.add(new BasicNameValuePair("ResponseGroup", "ItemAttributes"));
        params.add(new BasicNameValuePair("SearchIndex", "Books"));
        params.add(new BasicNameValuePair("Service", "AWSECommerceService"));
        params.add(new BasicNameValuePair("Timestamp", timestamp));
        params.add(new BasicNameValuePair("Version", AMAZON_VERSION));

        String target = AMAZON_URL + Utils.getQuery(params);
        String digest = Utils.toHmacSHA256(target, getString(R.string.aws_secret_access_key_id));
        digest = Utils.urlEncode(digest);

        AwsApi api = AwsService.getAwsService();
        api.getBook(getString(R.string.aws_access_key_id), getString(R.string.amazon_associate_tag), "ISBN", isbn,
                "ItemLookup", "ItemAttributes", "Books", "AWSECommerceService",
                timestamp, AMAZON_VERSION, digest, new Callback<ItemLookupResponse>() {
                    @Override
                    public void success(ItemLookupResponse itemLookupResponse, Response response) {
                        if (itemLookupResponse.getItems() == null) {
                            Toast.makeText(MainActivity.this, "ISBN コードが間違っています", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        List<Item> itemList = itemLookupResponse.getItems().getItemList();
                        String title = "";
                        String authorList = "";
                        String url = "";

                        boolean kindleExist = false;
                        for (Item item : itemList) {
                            ItemAttributes itemAttributes = item.getItemAttributes();
                            if (itemAttributes.getBinding().contains("Kindle")) {
                                kindleExist = true;
                            } else {
                                title = itemAttributes.getTitle();
                                url = item.getDetailPageURL();
                                for (Author author : itemAttributes.getAuthorList()) {
                                    authorList = TextUtils.isEmpty(authorList) ?
                                            author.getAuthorName() : authorList + ", " + author.getAuthorName();
                                }
                            }
                        }

                        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                        intent.putExtra(ItemActivity.EXTRA_ITEM_TITLE, title);
                        intent.putExtra(ItemActivity.EXTRA_ITEM_AUTHOR, authorList);
                        intent.putExtra(ItemActivity.EXTRA_EXISTS_KINDLE, kindleExist);
                        intent.putExtra(ItemActivity.EXTRA_ITEM_URL, url);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("a", error.getResponse().getUrl());
                        error.printStackTrace();
                        if (error.getResponse() == null) {
                            Toast.makeText(MainActivity.this, getString(R.string.toast_error_net), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this, getString(R.string.toast_error_other), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
