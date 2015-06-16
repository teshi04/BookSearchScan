package jp.tsur.booksearch.data.api;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

@Module(
        complete = false,
        library = true
)
public class ApiModule {

    private static final String API_URL = "http://ecs.amazonaws.jp";

    @Provides
    @Singleton
    RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new SimpleXMLConverter())
                .build();
    }

    @Provides
    @Singleton
    AwsService provideAwsService(RestAdapter restAdapter) {
        return restAdapter.create(AwsService.class);
    }
}
