package jp.tsur.booksearch.ui;

import dagger.Module;
import jp.tsur.booksearch.ui.widget.BookCardView;

@Module(complete = false,
        injects = {
                MainActivity.class,
                ItemActivity.class,
                SettingsActivity.SettingsFragment.class,
                BookCardView.class
        })
public class UiModule {
}
