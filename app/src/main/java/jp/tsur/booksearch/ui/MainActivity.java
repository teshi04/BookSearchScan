package jp.tsur.booksearch.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ChilchilEnabled;
import jp.tsur.booksearch.data.ScanHistory;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.prefs.BooleanPreference;
import jp.tsur.booksearch.data.prefs.StringPreference;
import jp.tsur.booksearch.databinding.ActivityMainBinding;
import jp.tsur.booksearch.ui.widget.BookCardView;
import jp.tsur.booksearch.utils.StringUtils;
import jp.tsur.booksearch.utils.Utils;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_BARCODE_CAPTURE = 0;
    public static final int REQUEST_ITEM = 1;
    public static final int REQUEST_SETTINGS = 2;

    @Inject
    @ChilchilEnabled
    BooleanPreference chilchilEnabled;

    @Inject
    @ScanHistory
    StringPreference scanHistory;

    private ActivityMainBinding binding;
    private ScanHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setActivity(this);
        InjectionUtils.inject(this);

        final ArrayList<Book> bookList = Utils.toList(scanHistory.get());
        adapter = new ScanHistoryAdapter(bookList) {
            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
                viewHolder.bookCardView.setBookCardListener(new BookCardView.BookCardListener() {
                    @Override
                    public boolean popMenuClicked(int itemId) {
                        Book book = adapter.getItem(viewHolder.getAdapterPosition());
                        Intent intent;
                        switch (itemId) {
                            case BookCardView.MENU_AMAZON:
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getUrl()));
                                startActivity(intent);
                                return true;
                            case BookCardView.MENU_GOODREADS:
                                if (!TextUtils.isEmpty(book.getIsbn())) {
                                    intent = new Intent(Intent.ACTION_VIEW, StringUtils.toGoodreads(book.getIsbn()));
                                    startActivity(intent);
                                }
                                return true;
                            case BookCardView.MENU_CHILCHIL:
                                intent = new Intent(Intent.ACTION_VIEW, StringUtils.toChilChilUri(book.getTitle()));
                                startActivity(intent);
                                return true;
                            case BookCardView.MENU_DELETE:
                                final ArrayList<Book> list = Utils.toList(scanHistory.get());
                                list.remove(viewHolder.getAdapterPosition());
                                remove(viewHolder.getAdapterPosition());
                                scanHistory.set(!list.isEmpty() ? Utils.toJsonString(list) : "");
                                return true;
                        }
                        return false;
                    }
                });
                return viewHolder;
            }
        };
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setVisibility(bookList.isEmpty() ? View.GONE : View.VISIBLE);
        binding.emptyText.setVisibility(bookList.isEmpty() ? View.VISIBLE : View.GONE);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        final int targetPosition = viewHolder.getAdapterPosition();
                        final Book targetItem = adapter.getItem(targetPosition);

                        // 削除
                        adapter.remove(targetPosition);
                        final ArrayList<Book> list = Utils.toList(scanHistory.get());
                        list.remove(targetPosition);
                        scanHistory.set(!list.isEmpty() ? Utils.toJsonString(list) : "");
                        Snackbar.make(binding.container, R.string.snack_deleted, Snackbar.LENGTH_LONG)
                                .setAction(R.string.snack_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // 元に戻す
                                        list.add(targetItem);
                                        scanHistory.set(Utils.toJsonString(list));
                                        adapter.insert(targetItem, targetPosition);
                                    }
                                })
                                .show();
                    }
                });
        swipeToDismissTouchHelper.attachToRecyclerView(binding.recyclerView);
    }

    public void onScanButtonClick(View view) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, REQUEST_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_BARCODE_CAPTURE:
                    String isbn = data.getStringExtra("SCAN_RESULT");
                    Intent intent = ItemActivity.createIntent(this, isbn);
                    startActivityForResult(intent, REQUEST_ITEM);
                    break;
                case REQUEST_ITEM:
                    binding.recyclerView.scrollToPosition(0);
                    ArrayList<Book> books = Utils.toList(scanHistory.get());
                    Book book = books.get(0);
                    adapter.insert(book, 0);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyText.setVisibility(View.GONE);
                    break;
                case REQUEST_SETTINGS:
                    binding.recyclerView.scrollToPosition(0);
                    adapter.addAll(Utils.toList(scanHistory.get()));
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.emptyText.setVisibility(View.GONE);
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
            startActivityForResult(intent, REQUEST_SETTINGS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
