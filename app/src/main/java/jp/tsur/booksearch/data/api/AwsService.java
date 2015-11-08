package jp.tsur.booksearch.data.api;

import jp.tsur.booksearch.data.api.model.ItemLookupResponse;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface AwsService {

    @GET("/onca/xml")
    Observable<ItemLookupResponse> getBook(
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
            @Query(value = "Signature", encoded = true) String signature  // http://docs.aws.amazon.com/AWSECommerceService/latest/DG/rest-signature.html
    );
}
