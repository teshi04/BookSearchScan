package jp.tsur.booksearch.api;

import jp.tsur.booksearch.model.ItemLookupResponse;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface AwsApi {

    @GET("/onca/xml")
    void getBook(
            @Query("AWSAccessKeyId") String awsAccessKeyId,
            @Query("AssociateTag") String associateTag,
            @Query("IdType") String idType,
            @Query("ItemId") String itemId,
            @Query("Operation") String operation,
            @Query("ResponseGroup") String responseGroup,
            @Query("SearchIndex") String searchIndex,
            @Query("Service") String service,
            @Query("Timestamp") String timestamp, //  ISO 8601 "yyyy-MM-dd'T'HH:mm:ss'Z'"
            @Query("Version") String version,
            @Query(value = "Signature", encodeValue = false) String signature, // http://docs.aws.amazon.com/AWSECommerceService/latest/DG/rest-signature.html
            Callback<ItemLookupResponse> callback
    );

}