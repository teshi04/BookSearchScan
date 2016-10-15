package jp.tsur.booksearch;


import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.tsur.booksearch.data.DataModule;
import jp.tsur.booksearch.ui.UiModule;

@Module(
        includes = {
                UiModule.class,
                DataModule.class,
        },
        injects = {
                BookSearchApplication.class
        }
)
public class BookSearchModule {

    private final BookSearchApplication app;

    public BookSearchModule(BookSearchApplication app) {
        this.app = app;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return app;
    }
}
