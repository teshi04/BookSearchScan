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
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String isbn = data.getStringExtra("SCAN_RESULT");
                search(isbn);
            }
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
        Log.d("a", "target: " + target + "\nsignature" + digest);

        AwsApi api = AwsService.getAwsService();
        api.getBook(getString(R.string.aws_access_key_id), getString(R.string.amazon_associate_tag), "ISBN", isbn,
                "ItemLookup", "ItemAttributes", "Books", "AWSECommerceService",
                timestamp, AMAZON_VERSION, digest, new Callback<ItemLookupResponse>() {
                    @Override
                    public void success(ItemLookupResponse itemLookupResponse, Response response) {
                        Log.d("a", response.getUrl());
                        List<Item> itemList = itemLookupResponse.getItems().getItemList();
                        String title = "";
                        String author = "";
                        String kindleUrl = "";
                        String url = "";

                        boolean kindleExist = false;
                        for (Item item : itemList) {
                            ItemAttributes itemAttributes = item.getItemAttributes();
                            title = itemAttributes.getTitle();
                            author = itemAttributes.getAuthor();
                            if (itemAttributes.getBinding().contains("Kindle")) {
                                kindleExist = true;
                                kindleUrl = item.getDetailPageURL();
                            } else {
                                url = item.getDetailPageURL();
                            }
                        }

                        Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("author", author);
                        intent.putExtra("kindle_exist", kindleExist);
                        intent.putExtra("amazon_url", !TextUtils.isEmpty(url) ? url : kindleUrl);
                        startActivity(intent);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("a", error.getResponse().getUrl());
                        error.printStackTrace();
                        if (error.getResponse() == null) {
                            Toast.makeText(MainActivity.this, "インターネットに繋がっていないようです", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        switch (error.getResponse().getStatus()) {
                            case 403:
                                Toast.makeText(MainActivity.this, "開発者に相談してください", Toast.LENGTH_SHORT).show();
                        }
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
