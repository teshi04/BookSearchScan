package jp.tsur.booksearch.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ChilchilEnabled;
import jp.tsur.booksearch.data.prefs.BooleanPreference;


public class BookCardView extends LinearLayout {

    public static final int MENU_AMAZON = 0;
    public static final int MENU_CHILCHIL = 1;
    public static final int MENU_DELETE = 2;

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

    @Bind(R.id.pop_menu)
    ImageButton popMenu;

    @Inject
    @ChilchilEnabled
    BooleanPreference chilchilEnabled;

    private Context context;
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
        InjectionUtils.inject(context, this);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.book_card_view, this, true);
        ButterKnife.bind(this);
        this.context = context;
    }

    public void setBookCardListener(BookCardListener listener) {
        this.listener = listener;
    }

    public void setData(String title, String author, String date, boolean kindleExists) {
        titleView.setText(title);
        authorView.setText(author);
        publicationDateView.setVisibility(
                !TextUtils.isEmpty(date) ? View.VISIBLE : View.GONE);
        publicationDateView.setText(date);
        kindleExistView.setVisibility(kindleExists ? View.VISIBLE : View.GONE);
        kindleNoneView.setVisibility(kindleExists ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.pop_menu)
    void popMenuClicked() {
        PopupMenu popup = new PopupMenu(context, popMenu);
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
