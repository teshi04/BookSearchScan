package jp.tsur.booksearch.ui.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import javax.inject.Inject;

import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ChilchilEnabled;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.prefs.BooleanPreference;
import jp.tsur.booksearch.databinding.ViewBookCardBinding;


public class BookCardView extends LinearLayout {

    public static final int MENU_AMAZON = 0;
    public static final int MENU_CHILCHIL = 1;
    public static final int MENU_DELETE = 2;

    @Inject
    @ChilchilEnabled
    BooleanPreference chilchilEnabled;

    private Context context;
    private ViewBookCardBinding binding;
    private BookCardListener listener;

    public interface BookCardListener {
        boolean popMenuClicked(int itemId);
    }

    public BookCardView(Context context) {
        this(context, null);
    }

    public BookCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BookCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.view_book_card, this, true);
        binding.setView(this);
        InjectionUtils.inject(context, this);
        this.context = context;
    }

    public void setBookCardListener(BookCardListener listener) {
        this.listener = listener;
    }

    public void setData(Book book) {
        binding.setBook(book);
    }

    public void onPopMenuClick(View view) {
        PopupMenu popup = new PopupMenu(context, binding.popMenu);
        Menu menu = popup.getMenu();
        menu.add(1, MENU_AMAZON, 0, R.string.label_open_amazon);
        if (chilchilEnabled.get()) {
            menu.add(1, MENU_CHILCHIL, 1, R.string.label_open_chilchil);
        }
        menu.add(1, MENU_DELETE, 2, R.string.label_delete_book_from_history);

        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return listener.popMenuClicked(item.getItemId());
            }
        });
    }
}
