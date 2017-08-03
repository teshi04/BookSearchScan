package jp.tsur.booksearch.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
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
import jp.tsur.booksearch.data.api.model.LargeImage;
import jp.tsur.booksearch.data.prefs.BooleanPreference;
import jp.tsur.booksearch.data.prefs.StringPreference;
import jp.tsur.booksearch.databinding.ActivityItemBinding;
import jp.tsur.booksearch.utils.StringUtils;
import jp.tsur.booksearch.utils.Utils;
import retrofit2.HttpException;


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
    private String isbn;
    private ActivityItemBinding binding;
    private Disposable disposable = Disposables.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_item);
        binding.setActivity(this);

        InjectionUtils.inject(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent != null) {
            search(intent.getExtras().getString(EXTRA_ISBN), savedInstanceState == null);
        }
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    private void setData(Book book) {
        this.title = book.getTitle();
        this.amazonUrl = book.getUrl();
        this.isbn = book.getIsbn();

        binding.setBook(book);
        if (chilchilEnabled.get()) {
            binding.chilchilButton.setVisibility(View.VISIBLE);
        }

        Picasso.with(this).load(book.getImageUrl()).into(binding.image);

        binding.progressBar.setVisibility(View.GONE);
        binding.contents.setVisibility(View.VISIBLE);
    }

    private void search(final String isbn, final boolean save) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.JAPAN);
        String timestamp = df.format(new Date());
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("AWSAccessKeyId", AWS_ACCESS_KEY);
        builder.appendQueryParameter("AssociateTag", ASSOCIATE_TAG);
        builder.appendQueryParameter("IdType", "ISBN");
        builder.appendQueryParameter("ItemId", isbn);
        builder.appendQueryParameter("Operation", "ItemLookup");
        builder.appendQueryParameter("ResponseGroup", "ItemAttributes,Images");
        builder.appendQueryParameter("SearchIndex", "Books");
        builder.appendQueryParameter("Service", "AWSECommerceService");
        builder.appendQueryParameter("Timestamp", timestamp);
        builder.appendQueryParameter("Version", AMAZON_VERSION);

        String target = AMAZON_URL + builder.build().toString().replace("?", "");
        String digest = StringUtils.toHmacSHA256(target, AWS_SECRET);
        digest = StringUtils.urlEncode(digest);

        final String scanHistoryString = scanHistory.get();

        disposable = awsService.getBook(AWS_ACCESS_KEY, ASSOCIATE_TAG, "ISBN", isbn,
                "ItemLookup", "ItemAttributes,Images", "Books", "AWSECommerceService",
                timestamp, AMAZON_VERSION, digest)
                .subscribeOn(Schedulers.io())
                .retryWhen(new Function<Observable<Throwable>, ObservableSource<Long>>() {
                    @Override
                    public ObservableSource<Long> apply(Observable<Throwable> observable) throws Exception {
                        return observable.flatMap(new Function<Throwable, ObservableSource<Long>>() {
                            @Override
                            public ObservableSource<Long> apply(Throwable throwable) throws Exception {
                                if (throwable instanceof HttpException) {
                                    // 結構な確率で 503 エラーが出るが、リトライすれば大体成功する
                                    return Observable.timer(3, TimeUnit.SECONDS);
                                }
                                return Observable.error(throwable);
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ItemLookupResponse>() {
                    @Override
                    public void accept(ItemLookupResponse response) throws Exception {
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
                        String imageUrl = "";

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
                            LargeImage largeImage = item.getLargeImage();
                            if (largeImage != null) {
                                imageUrl = largeImage.getUrl();
                            }
                        }

                        Book book = new Book(isbn, title, authorList, publicationDate, url, imageUrl, existsKindle);
                        setData(book);
                        if (save) {
                            ArrayList<Book> list = Utils.toList(scanHistoryString);
                            list.add(0, book);
                            scanHistory.set(Utils.toJsonString(list));
                        }
                        setResult(RESULT_OK);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (throwable instanceof UnknownHostException) {
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_net), Toast.LENGTH_SHORT).show();
                        } else {
                            // 不明なエラー
                            Toast.makeText(ItemActivity.this, getString(R.string.toast_error_other), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                });
    }

    public void onChilChilButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, StringUtils.toChilChilUri(title));
        startActivity(intent);
    }

    public void onAmazonButtonClick(View view) {
        Uri uri = Uri.parse(amazonUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void onGoodreadsButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, StringUtils.toGoodreads(isbn));
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
