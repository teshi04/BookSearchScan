package jp.tsur.chil.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.tsur.chil.R;


public class ItemActivity extends ActionBarActivity {

    public static final String EXTRA_ITEM_TITLE = "book_title";
    public static final String EXTRA_ITEM_AUTHOR = "book_author";
    public static final String EXTRA_ITEM_URL = "book_url";
    public static final String EXTRA_EXISTS_KINDLE = "book_exists_kindle";
    public static final String URL_CHIL_CHIL = "http://www.chil-chil.net/sp/goodsList/?freeword=";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
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

        Bundle extras = getIntent().getExtras();
        amazonUrl = extras.getString(EXTRA_ITEM_URL);
        title = extras.getString(EXTRA_ITEM_TITLE);
        titleView.setText(title);
        authorView.setText(extras.getString(EXTRA_ITEM_AUTHOR));
        if (extras.getBoolean(EXTRA_EXISTS_KINDLE)) {
            kindleExistView.setVisibility(View.VISIBLE);
        } else {
            kindleNoneView.setVisibility(View.VISIBLE);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("chilchil_visible", false))
            openChilButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.open_chil_button)
    void openChilChil() {
        // スペースより前だけでいいと思う、、
        String[] split = title.split(" ");
        Uri uri = Uri.parse(URL_CHIL_CHIL + split[0]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
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
