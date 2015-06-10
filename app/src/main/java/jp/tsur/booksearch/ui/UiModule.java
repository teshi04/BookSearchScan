package jp.tsur.booksearch.ui;

import dagger.Module;

/**
 * Created by teshi on 2015/06/10.
 */
@Module(complete = false,
        injects = {
                MainActivity.class,
                ItemActivity.class,
                ScanHistoryAdapter.class

        })
public class UiModule {
}
