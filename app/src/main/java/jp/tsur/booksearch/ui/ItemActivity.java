package jp.tsur.booksearch.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import jp.tsur.booksearch.BuildConfig;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.api.AwsApi;
import jp.tsur.booksearch.api.AwsService;
import jp.tsur.booksearch.model.Author;
import jp.tsur.booksearch.model.Book;
import jp.tsur.booksearch.model.Item;
import jp.tsur.booksearch.model.ItemAttributes;
import jp.tsur.booksearch.model.ItemLookupResponse;
import jp.tsur.booksearch.utils.Utils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class ItemActivity extends AppCompatActivity {

    public static final String AWS_ACCESS_KEY = BuildConfig.AWS_ACCESS_KEY;
    public static final String AWS_SECRET = BuildConfig.AWS_SECRET;
    public static final String ASSOCIATE_TAG = BuildConfig.ASSOCIATE_TAG;
    public static final String EXTRA_ISBN = "isbn";

    private static final String AMAZON_URL = "GET\necs.amazonaws.jp\n/onca/xml\n";
    private static final String AMAZON_VERSION = "2011-08-01";

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
    @InjectView(R.id.publication_date_view)
    TextView publicationDateView;
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

    private void setData(String title, String author, String publicationDate, String amazonUrl, boolean kindleExist) {
        this.title = title;
        this.amazonUrl = amazonUrl;
        titleView.setText(title);
        authorView.setText(author);
        publicationDateView.setText(publicationDate);
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
        params.add(new BasicNameValuePair("AWSAccessKeyId", AWS_ACCESS_KEY));
        params.add(new BasicNameValuePair("AssociateTag", ASSOCIATE_TAG));
        params.add(new BasicNameValuePair("IdType", "ISBN"));
        params.add(new BasicNameValuePair("ItemId", isbn));
        params.add(new BasicNameValuePair("Operation", "ItemLookup"));
        params.add(new BasicNameValuePair("ResponseGroup", "ItemAttributes"));
        params.add(new BasicNameValuePair("SearchIndex", "Books"));
        params.add(new BasicNameValuePair("Service", "AWSECommerceService"));
        params.add(new BasicNameValuePair("Timestamp", timestamp));
        params.add(new BasicNameValuePair("Version", AMAZON_VERSION));
        String target = AMAZON_URL + Utils.getQuery(params);
        String digest = Utils.toHmacSHA256(target, AWS_SECRET);
        digest = Utils.urlEncode(digest);

        AwsApi api = AwsService.getAwsService();
        api.getBook(AWS_ACCESS_KEY, ASSOCIATE_TAG, "ISBN", isbn,
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
                        String publicationDate = "";
                        String authorList = "";
                        String url = "";

                        boolean existsKindle = false;
                        for (Item item : itemList) {
                            ItemAttributes itemAttributes = item.getItemAttributes();
                            if (itemAttributes.getBinding().contains("Kindle")) {
                                existsKindle = true;
                            } else {
                                title = itemAttributes.getTitle();
                                publicationDate = Utils.formatDate(itemAttributes.getPublicationDate());
                                url = item.getDetailPageURL();
                                for (Author author : itemAttributes.getAuthorList()) {
                                    authorList = TextUtils.isEmpty(authorList) ?
                                            author.getAuthorName() : authorList + ", " + author.getAuthorName();
                                }
                            }
                        }

                        setData(title, authorList, publicationDate, url, existsKindle);
                        Utils.addScanHistory(ItemActivity.this, new Book(title, authorList, publicationDate, url, existsKindle));
                        setResult(RESULT_OK);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() == null) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_net), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_other), Toast.LENGTH_SHORT).show();
                            if (BuildConfig.DEBUG) {
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
