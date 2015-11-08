package jp.tsur.booksearch.data.api;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.SimpleXmlConverterFactory;

@Module(
        complete = false,
        library = true
)
public class ApiModule {

    private static final String API_URL = "http://ecs.amazonaws.jp";

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    AwsService provideAwsService(Retrofit retrofit) {
        return retrofit.create(AwsService.class);
    }
}
