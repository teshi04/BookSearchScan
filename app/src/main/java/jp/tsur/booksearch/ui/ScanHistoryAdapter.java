package jp.tsur.booksearch.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ScanHistory;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.prefs.StringPreference;
import jp.tsur.booksearch.ui.widget.BookCardView;
import jp.tsur.booksearch.utils.StringUtils;
import jp.tsur.booksearch.utils.Utils;

public class ScanHistoryAdapter extends RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Book> bookList;

    @Inject
    @ScanHistory
    StringPreference books;

    public ScanHistoryAdapter(Context context, ArrayList<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
        InjectionUtils.inject(context, this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book_history, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Book book = bookList.get(position);

        holder.bookCardView.setData(book.getTitle(),
                book.getAuthor(),
                book.getPublicationDate(),
                book.isExistsKindle());

        holder.bookCardView.setBookCardListener(new BookCardView.BookCardListener() {
            @Override
            public boolean popMenuClicked(int itemId) {
                Intent intent;
                switch (itemId) {
                    case BookCardView.MENU_AMAZON:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getUrl()));
                        context.startActivity(intent);
                        return true;
                    case BookCardView.MENU_CHILCHIL:
                        intent = new Intent(Intent.ACTION_VIEW, StringUtils.toChilChilUri(book.getTitle()));
                        context.startActivity(intent);
                        return true;
                    case BookCardView.MENU_DELETE:
                        remove(holder.getAdapterPosition());
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void add(Book item) {
        bookList.add(item);
        notifyItemInserted(bookList.size());
    }

    public void insert(Book item, int position) {
        bookList.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        bookList.remove(position);

        // 削除
        ArrayList<Book> scanHistory = Utils.toList(books.get());
        scanHistory.remove(position);
        books.set(Utils.toJsonString(scanHistory));

        notifyItemRemoved(position);
    }

    public Book getItem(int position) {
        return bookList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        BookCardView bookCardView;

        public ViewHolder(View view) {
            super(view);
            bookCardView = (BookCardView) view;
        }
    }
}
