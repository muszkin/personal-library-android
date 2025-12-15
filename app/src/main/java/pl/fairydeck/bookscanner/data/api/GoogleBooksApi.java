package pl.fairydeck.bookscanner.data.api;

import pl.fairydeck.bookscanner.data.api.models.GoogleBooksResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleBooksApi {
    @GET("volumes")
    Call<GoogleBooksResponse> searchByIsbn(@Query("q") String query);
}





