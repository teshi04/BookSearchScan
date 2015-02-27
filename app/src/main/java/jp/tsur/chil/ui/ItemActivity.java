package jp.tsur.chil.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.tsur.chil.BuildConfig;
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


public class ItemActivity extends ActionBarActivity {

    private static final String AMAZON_URL = "GET\necs.amazonaws.jp\n/onca/xml\n";
    private static final String AMAZON_VERSION = "2011-08-01";
    public static final String EXTRA_ISBN = "isbn";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    @InjectView(R.id.card_view)
    CardView cardView;
    @InjectView(R.id.title_view)
    TextView titleView;
    @InjectView(R.id.author_view)
    TextView authorView;
    @InjectView(R.id.kindle_exist_view)
    TextView kindleExistView;
    @InjectView(R.id.kindle_none_view)
    TextView kindleNoneView;
    @InjectView(R.id.open_chil_button)
    Button openChilButton;

    private String amazonUrl;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            search(intent.getExtras().getString(EXTRA_ISBN));
        }
    }

    private void setResult(String title, String author, String amazonUrl, boolean kindleExist) {
        this.title = title;
        this.amazonUrl = amazonUrl;
        titleView.setText(title);
        authorView.setText(author);
        if (kindleExist) {
            kindleExistView.setVisibility(View.VISIBLE);
        } else {
            kindleNoneView.setVisibility(View.VISIBLE);
        }

        if (Utils.isChilChilMode(this)) {
            openChilButton.setVisibility(View.VISIBLE);
        }

        progressBar.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
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
                        if (itemLookupResponse.getItems().getItemList() == null) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_not_isbn), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        List<Item> itemList = itemLookupResponse.getItems().getItemList();
                        String title = "";
                        String authorList = "";
                        String url = "";

                        boolean existsKindle = false;
                        for (Item item : itemList) {
                            ItemAttributes itemAttributes = item.getItemAttributes();
                            if (itemAttributes.getBinding().contains("Kindle")) {
                                existsKindle = true;
                            } else {
                                title = itemAttributes.getTitle();
                                url = item.getDetailPageURL();
                                for (Author author : itemAttributes.getAuthorList()) {
                                    authorList = TextUtils.isEmpty(authorList) ?
                                            author.getAuthorName() : authorList + ", " + author.getAuthorName();
                                }
                            }
                        }

                        setResult(title, authorList, url, existsKindle);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() == null) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_net), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_other), Toast.LENGTH_SHORT).show();
                            if (BuildConfig.DEBUG) {
                                Log.d("bbs", error.getResponse().getUrl());
                                error.printStackTrace();
                            }
                        }
                        finish();
                    }
                });
    }


    @OnClick(R.id.open_chil_button)
    void openChilChil() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Utils.toChilChilUri(title));
        startActivity(intent);
    }

    @OnClick(R.id.open_amazon_button)
    void openAmazon() {
        Uri uri = Uri.parse(amazonUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
