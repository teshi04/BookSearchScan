package jp.tsur.booksearch.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.tsur.booksearch.data.api.ApiModule;
import jp.tsur.booksearch.data.prefs.BooleanPreference;

@Module(
        includes = ApiModule.class,
        complete = false,
        library = true
)
public class DataModule {

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application app) {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @ChilchilEnabled
    @Singleton
    BooleanPreference provideChilchilEnabledPref(SharedPreferences pref) {
        return new BooleanPreference(pref, "chilchil_visible", false);
    }
}
