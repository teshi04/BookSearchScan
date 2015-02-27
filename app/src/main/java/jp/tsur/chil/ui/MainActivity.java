package jp.tsur.chil.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.tsur.chil.R;
import jp.tsur.chil.model.Book;
import jp.tsur.chil.utils.Utils;


public class MainActivity extends ActionBarActivity {

    public static final int REQUEST_ZXING = 0;
    public static final int REQUEST_ITEM = 1;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.history_view)
    RecyclerView historyView;
    @InjectView(R.id.scan_button)
    FloatingActionButton scanButton;

    ScanHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        scanButton.attachToRecyclerView(historyView);
        historyView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        historyView.setLayoutManager(mLayoutManager);

        adapter = new ScanHistoryAdapter(this, Utils.getScanHistory(this));
        historyView.setAdapter(adapter);
    }

    @OnClick(R.id.scan_button)
    void scan() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_FORMATS", "EAN_13");

        try {
            startActivityForResult(intent, REQUEST_ZXING);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.error_toast_scaner_not_installed), Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.google.zxing.client.android&hl=ja");
            Intent zxingIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(zxingIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ZXING:
                    String isbn = data.getStringExtra("SCAN_RESULT");
                    Intent intent = new Intent(MainActivity.this, ItemActivity.class);
                    intent.putExtra(ItemActivity.EXTRA_ISBN, isbn);
                    startActivityForResult(intent, REQUEST_ITEM);
                    break;
                case REQUEST_ITEM:
                    historyView.scrollToPosition(0);
                    if (adapter.getItemCount() == Utils.SCAN_HISTORY_MAX) {
                        adapter.remove(adapter.getItemCount() - 1);
                    }
                    ArrayList<Book> scanHistory = Utils.getScanHistory(this);
                    Book book = scanHistory.get(0);
                    adapter.insert(book, 0);
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
