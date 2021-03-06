package jp.tsur.booksearch.ui;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.ui.widget.BookCardView;

public class ScanHistoryAdapter extends RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder> {

    private ArrayList<Book> bookList;

    public ScanHistoryAdapter(ArrayList<Book> bookList) {
        this.bookList = bookList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(new BookCardView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Book book = bookList.get(position);
        holder.bookCardView.setData(book);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    public void addAll(ArrayList<Book> bookList) {
        this.bookList = bookList;
        notifyDataSetChanged();
    }

    public void insert(Book item, int position) {
        bookList.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        bookList.remove(position);
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
