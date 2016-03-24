package jp.tsur.booksearch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.tsur.booksearch.BuildConfig;
import jp.tsur.booksearch.InjectionUtils;
import jp.tsur.booksearch.R;
import jp.tsur.booksearch.data.ScanHistory;
import jp.tsur.booksearch.data.api.model.Book;
import jp.tsur.booksearch.data.prefs.StringPreference;
import jp.tsur.booksearch.utils.Utils;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (BuildConfig.DEBUG) {
            setResult(RESULT_OK);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Inject
        @ScanHistory
        StringPreference scanHistory;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            InjectionUtils.inject(getActivity(), this);

            findPreference("licence").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getActivity(), LicenceActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

            /**
             * debug menu (develop only)
             */

            if (BuildConfig.DEBUG) {
                findPreference("insert_items").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ArrayList<Book> list = Utils.toList(scanHistory.get());
                        list.add(0, new Book("9784003109045", "人間失格、グッド・バイ 他一篇 (岩波文庫)", "太宰治", "1988/5/16", "http://www.amazon.co.jp/%E4%BA%BA%E9%96%93%E5%A4%B1%E6%A0%BC%E3%80%81%E3%82%B0%E3%83%83%E3%83%89%E3%83%BB%E3%83%90%E3%82%A4-%E4%BB%96%E4%B8%80%E7%AF%87-%E5%B2%A9%E6%B3%A2%E6%96%87%E5%BA%AB-%E5%A4%AA%E5%AE%B0-%E6%B2%BB/dp/400310904X/ref=sr_1_3?s=books&ie=UTF8&qid=1458827429&sr=1-3&keywords=%E4%BA%BA%E9%96%93%E5%A4%B1%E6%A0%BC", false));
                        list.add(0, new Book("9784087520095", "こころ (集英社文庫)", "夏目漱石", "1991/2/25", "http://www.amazon.co.jp/%E3%81%93%E3%81%93%E3%82%8D-%E9%9B%86%E8%8B%B1%E7%A4%BE%E6%96%87%E5%BA%AB-%E5%A4%8F%E7%9B%AE-%E6%BC%B1%E7%9F%B3/dp/4087520099/ref=pd_sim_14_2?ie=UTF8&dpID=51v9EsqegjL&dpSrc=sims&preST=_AC_UL160_SR113%2C160_&refRID=14R8MAQET8CHBBJMBSVT", true));
                        scanHistory.set(Utils.toJsonString(list));
                        return true;
                    }
                });

                findPreference("item_activity").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(ItemActivity.createIntent(getActivity(), "9784087520095"));
                        return true;
                    }
                });
            }
        }
    }
}
