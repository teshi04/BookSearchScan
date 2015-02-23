package jp.tsur.chil.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.tsur.chil.R;


public class ItemActivity extends Activity {

    public static final String EXTRA_ITEM_TITLE = "book_title";
    public static final String EXTRA_ITEM_AUTHOR = "book_author";
    public static final String EXTRA_ITEM_URL = "book_url";
    public static final String EXTRA_EXISTS_KINDLE = "book_exists_kindle";
    public static final String URL_CHIL_CHIL = "http://www.chil-chil.net/sp/goodsList/?freeword=";

    @InjectView(R.id.title_view)
    TextView titleView;
    @InjectView(R.id.author_view)
    TextView authorView;
    @InjectView(R.id.kindle_view)
    TextView kindleView;

    private String amazonUrl;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        amazonUrl = extras.getString(EXTRA_ITEM_URL);
        title = extras.getString(EXTRA_ITEM_TITLE);
        titleView.setText(title);
        authorView.setText(extras.getString(EXTRA_ITEM_AUTHOR));
        kindleView.setText(getString(R.string.label_kindle_edition, extras.getBoolean(EXTRA_EXISTS_KINDLE) ? "あり" : "なし"));
    }

    @OnClick(R.id.browser_chil_button)
    void openChilChil() {
        // スペースより前だけでいいと思う、、
        String[] split = title.split(" ");
        Uri uri = Uri.parse(URL_CHIL_CHIL + split[0]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.browser_amazon_button)
    void openAmazon() {
        Uri uri = Uri.parse(amazonUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;

            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
