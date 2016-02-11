package jp.tsur.booksearch.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;


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
    private Subscription subscription = Subscriptions.empty();

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

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
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
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("AWSAccessKeyId", AWS_ACCESS_KEY);
        builder.appendQueryParameter("AssociateTag", ASSOCIATE_TAG);
        builder.appendQueryParameter("IdType", "ISBN");
        builder.appendQueryParameter("ItemId", isbn);
        builder.appendQueryParameter("Operation", "ItemLookup");
        builder.appendQueryParameter("ResponseGroup", "ItemAttributes");
        builder.appendQueryParameter("SearchIndex", "Books");
        builder.appendQueryParameter("Service", "AWSECommerceService");
        builder.appendQueryParameter("Timestamp", timestamp);
        builder.appendQueryParameter("Version", AMAZON_VERSION);

        String target = AMAZON_URL + builder.build().toString().replace("?", "");
        String digest = StringUtils.toHmacSHA256(target, AWS_SECRET);
        digest = StringUtils.urlEncode(digest);

        final String scanHistoryString = scanHistory.get();

        subscription = awsService.getBook(AWS_ACCESS_KEY, ASSOCIATE_TAG, "ISBN", isbn,
                "ItemLookup", "ItemAttributes", "Books", "AWSECommerceService",
                timestamp, AMAZON_VERSION, digest)
                .subscribeOn(Schedulers.newThread())
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(Observable<? extends Throwable> observable) {
                        return observable.flatMap(new Func1<Throwable, Observable<Long>>() {
                            @Override
                            public Observable<Long> call(Throwable e) {
                                if (e instanceof HttpException) {
                                    // 結構な確率で 503 エラーが出るが、リトライすれば大体成功する
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                }
                                return Observable.error(e);
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ItemLookupResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof UnknownHostException) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_net), Toast.LENGTH_SHORT).show();
                        } else {
                            // 不明なエラー
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_other), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }

                    @Override
                    public void onNext(ItemLookupResponse response) {
                        if (response.getItems().getItemList() == null) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_not_isbn),
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        List<Item> itemList = response.getItems().getItemList();
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
