package jp.tsur.booksearch.ui;

import android.content.Context;
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

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.tsur.booksearch.BuildConfig;
import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ChilchilEnabled;
import jp.tsur.booksearch.data.ScanHistory;
import jp.tsur.booksearch.data.api.AwsService;
import jp.tsur.booksearch.data.api.model.Author;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.api.model.Item;
import jp.tsur.booksearch.data.api.model.ItemAttributes;
import jp.tsur.booksearch.data.api.model.ItemLookupResponse;
import jp.tsur.booksearch.data.prefs.BooleanPreference;
import jp.tsur.booksearch.data.prefs.StringPreference;
import jp.tsur.booksearch.utils.StringUtils;
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

    public static Intent createIntent(Context context, String isbn) {
        Intent intent = new Intent(context, ItemActivity.class);
        intent.putExtra(EXTRA_ISBN, isbn);
        return intent;
    }

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    @Bind(R.id.card_view)
    CardView cardView;

    @Bind(R.id.title_view)
    TextView titleView;

    @Bind(R.id.author_view)
    TextView authorView;

    @Bind(R.id.publication_date_view)
    TextView publicationDateView;

    @Bind(R.id.kindle_exist_view)
    TextView kindleExistView;

    @Bind(R.id.kindle_none_view)
    TextView kindleNoneView;

    @Bind(R.id.open_chil_button)
    Button openChilButton;

    @Inject
    @ChilchilEnabled
    BooleanPreference chilchilEnabled;

    @Inject
    @ScanHistory
    StringPreference scanHistory;

    @Inject
    AwsService awsService;

    private String amazonUrl;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);

        InjectionUtils.inject(this);

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
        kindleExistView.setVisibility(kindleExist ? View.VISIBLE : View.GONE);
        kindleNoneView.setVisibility(kindleExist ? View.GONE : View.VISIBLE);

        if (chilchilEnabled.get()) {
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
        String target = AMAZON_URL + StringUtils.getQuery(params);
        String digest = StringUtils.toHmacSHA256(target, AWS_SECRET);
        digest = StringUtils.urlEncode(digest);

        final String scanHistoryString = scanHistory.get();
        awsService.getBook(AWS_ACCESS_KEY, ASSOCIATE_TAG, "ISBN", isbn,
                "ItemLookup", "ItemAttributes", "Books", "AWSECommerceService",
                timestamp, AMAZON_VERSION, digest, new Callback<ItemLookupResponse>() {
                    @Override
                    public void success(ItemLookupResponse itemLookupResponse, Response response) {
                        if (itemLookupResponse.getItems().getItemList() == null) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_not_isbn),
                                    Toast.LENGTH_LONG).show();
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
                                publicationDate = formatDate(itemAttributes.getPublicationDate());
                                url = item.getDetailPageURL();
                                for (Author author : itemAttributes.getAuthorList()) {
                                    authorList = TextUtils.isEmpty(authorList) ?
                                            author.getAuthorName() : authorList + ", " + author.getAuthorName();
                                }
                            }
                        }

                        setData(title, authorList, publicationDate, url, existsKindle);

                        // 保存
                        ArrayList<Book> list = Utils.toList(scanHistoryString);
                        list.add(0, new Book(title, authorList, publicationDate, url, existsKindle));
                        scanHistory.set(Utils.toJsonString(list));

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
        Intent intent = new Intent(Intent.ACTION_VIEW, StringUtils.toChilChilUri(title));
        startActivity(intent);
    }

    @OnClick(R.id.open_amazon_button)
    void openAmazon() {
        Uri uri = Uri.parse(amazonUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * "-" を "/" に置換して、日付を見やすくする
     *
     * @return 2009-7-30 → 2009/7/30
     */
    public static String formatDate(String text) {
        return text.replaceAll("-", "/");
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
