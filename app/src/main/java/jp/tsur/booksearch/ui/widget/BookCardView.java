package jp.tsur.booksearch.ui.widget;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ChilchilEnabled;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.prefs.BooleanPreference;
import jp.tsur.booksearch.databinding.ViewBookCardBinding;


public class BookCardView extends LinearLayout {

    public static final int MENU_AMAZON = 0;
    public static final int MENU_GOODREADS = 3;
    public static final int MENU_CHILCHIL = 1;
    public static final int MENU_DELETE = 2;

    @Inject
    @ChilchilEnabled
    BooleanPreference chilchilEnabled;

    private String isbn;
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

        // FIXME: これをここに書かないとwrapになっちゃうのなんなの
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        InjectionUtils.inject(context, this);
    }

    public void setBookCardListener(BookCardListener listener) {
        this.listener = listener;
    }

    public void setData(Book book) {
        isbn = book.getIsbn();
        binding.setBook(book);
        if (TextUtils.isEmpty(book.getImageUrl())) {
            binding.image.setImageResource(R.drawable.no_data);
        } else {
            Picasso.with(getContext()).load(book.getImageUrl()).into(binding.image);
        }
    }

    public void onPopMenuClick(View view) {
        PopupMenu popup = new PopupMenu(view.getContext(), binding.popMenu);
        Menu menu = popup.getMenu();
        menu.add(1, MENU_AMAZON, 0, R.string.label_open_amazon);
        if (!TextUtils.isEmpty(isbn)) {
            menu.add(1, MENU_GOODREADS, 1, R.string.label_open_goodreads);
        }
        if (chilchilEnabled.get()) {
            menu.add(1, MENU_CHILCHIL, 2, R.string.label_open_chilchil);
        }
        menu.add(1, MENU_DELETE, 3, R.string.label_delete_book_from_history);

        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return listener.popMenuClicked(item.getItemId());
            }
        });
    }
}
