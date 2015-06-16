package jp.tsur.booksearch.ui;

import dagger.Module;
import jp.tsur.booksearch.ui.widget.BookCardView;

/**
 * Created by teshi on 2015/06/10.
 */
@Module(complete = false,
        injects = {
                MainActivity.class,
                ItemActivity.class,
                BookCardView.class
        })
public class UiModule {
}
