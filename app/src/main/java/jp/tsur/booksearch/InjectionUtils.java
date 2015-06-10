package jp.tsur.booksearch;

import android.content.Context;

public class InjectionUtils {

    public static void inject(Context context) {
        inject(context, context);
    }

    public static void inject(Context context, Object target) {
        BookSearchApplication appContext = (BookSearchApplication) ((context instanceof BookSearchApplication) ? context : context.getApplicationContext());
        appContext.getObjectGraph().inject(target);
    }
}
