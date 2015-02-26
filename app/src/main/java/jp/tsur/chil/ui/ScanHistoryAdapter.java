package jp.tsur.chil.ui;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.tsur.chil.R;
import jp.tsur.chil.model.Book;
import jp.tsur.chil.utils.Utils;

public class ScanHistoryAdapter extends RecyclerView.Adapter<ScanHistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Book> bookList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.title_view)
        TextView titleView;
        @InjectView(R.id.author_view)
        TextView authorView;
        @InjectView(R.id.kindle_exist_view)
        TextView kindleExistView;
        @InjectView(R.id.kindle_none_view)
        TextView kindleNoneView;
        @InjectView(R.id.pop_menu)
        ImageButton popMenu;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }

    public ScanHistoryAdapter(Context context, ArrayList<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
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

        holder.titleView.setText(book.getTitle());
        holder.authorView.setText(book.getAuthor());
        if (book.isExistsKindle()) {
            holder.kindleExistView.setVisibility(View.VISIBLE);
            holder.kindleNoneView.setVisibility(View.GONE);
        } else {
            holder.kindleNoneView.setVisibility(View.VISIBLE);
            holder.kindleExistView.setVisibility(View.GONE);
        }

        holder.popMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, holder.popMenu);
                popup.getMenu().add(1, 0, 0, context.getString(R.string.label_open_amazon));
                popup.getMenu().add(1, 99, 2, context.getString(R.string.label_delete_book_from_history));
                if (Utils.isChilChilMode(context)) {
                    popup.getMenu().add(1, 1, 1, context.getString(R.string.label_open_chilchil));
                }
                popup.show();

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Intent intent;
                        switch (item.getItemId()) {
                            case 0:
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.getUrl()));
                                context.startActivity(intent);
                                break;
                            case 1:
                                intent = new Intent(Intent.ACTION_VIEW, Utils.toChilChilUri(book.getTitle()));
                                context.startActivity(intent);
                                break;
                            case 99:
                                remove(holder.getPosition());
                                break;
                        }
                        return true;
                    }
                });
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
        notifyItemRemoved(position);
    }
}