package pl.fairydeck.bookscanner.data.api;

import pl.fairydeck.bookscanner.data.api.models.OpenLibraryResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OpenLibraryApi {
    @GET("isbn/{isbn}.json")
    Call<OpenLibraryResponse> getBookByIsbn(@Path("isbn") String isbn);
}





