package pl.fairydeck.bookscanner.data.api;

import android.util.Log;

import pl.fairydeck.bookscanner.data.api.models.GoogleBooksResponse;
import pl.fairydeck.bookscanner.data.api.models.OpenLibraryResponse;
import pl.fairydeck.bookscanner.data.database.BookEntity;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookApiService {
    private static final String TAG = "BookApiService";
    private static final String OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/";
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/";

    private final OpenLibraryApi openLibraryApi;
    private final GoogleBooksApi googleBooksApi;

    public BookApiService() {
        Retrofit openLibraryRetrofit = new Retrofit.Builder()
                .baseUrl(OPEN_LIBRARY_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Retrofit googleBooksRetrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_BOOKS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.openLibraryApi = openLibraryRetrofit.create(OpenLibraryApi.class);
        this.googleBooksApi = googleBooksRetrofit.create(GoogleBooksApi.class);
    }

    public BookEntity fetchBookByIsbn(String isbn) {
        // Clean ISBN (remove dashes and spaces)
        String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
        
        // Try Open Library first
        BookEntity book = fetchFromOpenLibrary(cleanIsbn);
        if (book != null) {
            return book;
        }

        // Fallback to Google Books
        book = fetchFromGoogleBooks(cleanIsbn);
        return book;
    }

    private BookEntity fetchFromOpenLibrary(String isbn) {
        try {
            Call<OpenLibraryResponse> call = openLibraryApi.getBookByIsbn(isbn);
            Response<OpenLibraryResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                OpenLibraryResponse openLibraryResponse = response.body();
                return mapOpenLibraryToEntity(openLibraryResponse, isbn);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching from Open Library", e);
        }
        return null;
    }

    private BookEntity fetchFromGoogleBooks(String isbn) {
        try {
            Call<GoogleBooksResponse> call = googleBooksApi.searchByIsbn("isbn:" + isbn);
            Response<GoogleBooksResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                GoogleBooksResponse.VolumeInfo volumeInfo = response.body().getFirstVolumeInfo();
                if (volumeInfo != null) {
                    return mapGoogleBooksToEntity(volumeInfo, isbn);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error fetching from Google Books", e);
        }
        return null;
    }

    private BookEntity mapOpenLibraryToEntity(OpenLibraryResponse response, String isbn) {
        BookEntity entity = new BookEntity();
        entity.setIsbn(isbn);
        entity.setTitle(response.getTitle());

        // Authors
        if (response.getAuthors() != null && !response.getAuthors().isEmpty()) {
            List<String> authorNames = new ArrayList<>();
            for (OpenLibraryResponse.Author author : response.getAuthors()) {
                if (author.getName() != null) {
                    authorNames.add(author.getName());
                }
            }
            entity.setAuthor(String.join(", ", authorNames));
        }

        // Publisher
        if (response.getPublishers() != null && !response.getPublishers().isEmpty()) {
            entity.setPublisher(response.getPublishers().get(0));
        }

        entity.setPublishedDate(response.getPublishDate());
        entity.setDescription(response.getDescriptionText());
        entity.setCoverImageUrl(response.getCoverImageUrl());

        return entity;
    }

    private BookEntity mapGoogleBooksToEntity(GoogleBooksResponse.VolumeInfo volumeInfo, String isbn) {
        BookEntity entity = new BookEntity();
        entity.setIsbn(isbn);
        entity.setTitle(volumeInfo.getTitle());
        entity.setAuthor(volumeInfo.getAuthor());
        entity.setPublisher(volumeInfo.getPublisher());
        entity.setPublishedDate(volumeInfo.getPublishedDate());
        entity.setDescription(volumeInfo.getDescription());
        entity.setCoverImageUrl(volumeInfo.getCoverImageUrl());

        return entity;
    }
}





