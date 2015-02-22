package jp.tsur.chil.api;


import retrofit.RestAdapter;

public class AwsService {

    private static final String API_URL = "http://ecs.amazonaws.jp";

    public static AwsApi getAwsService() {
        return new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setConverter(new SimpleXMLConverter())
                .build()
                .create(AwsApi.class);
    }

}