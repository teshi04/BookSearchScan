package jp.tsur.chil.ui;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.tsur.chil.R;


public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
    }

    @OnClick(R.id.scan_button)
    void scan() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_FORMATS", "EAN_13");

        try {
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.error_toast_scaner_not_installed), Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("market://details?id=com.google.zxing.client.android&hl=ja");
            Intent zxingIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(zxingIntent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            String isbn = data.getStringExtra("SCAN_RESULT");

            Intent intent = new Intent(MainActivity.this, ItemActivity.class);
            intent.putExtra(ItemActivity.EXTRA_ISBN, isbn);
            startActivity(intent);
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
