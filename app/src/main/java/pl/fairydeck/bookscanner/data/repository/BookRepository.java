package pl.fairydeck.bookscanner.data.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.fairydeck.bookscanner.data.api.BookApiService;
import pl.fairydeck.bookscanner.data.database.BookDao;
import pl.fairydeck.bookscanner.data.database.BookDatabase;
import pl.fairydeck.bookscanner.data.database.BookEntity;

public class BookRepository {
    private BookDao bookDao;
    private LiveData<List<BookEntity>> allBooks;
    private BookApiService apiService;
    private ExecutorService executorService;

    public BookRepository(Application application) {
        BookDatabase database = BookDatabase.getInstance(application);
        bookDao = database.bookDao();
        allBooks = bookDao.getAll();
        apiService = new BookApiService();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<BookEntity>> getAllBooks() {
        return allBooks;
    }

    public LiveData<BookEntity> getBookById(long id) {
        return bookDao.getById(id);
    }

    public LiveData<List<BookEntity>> searchBooks(String query) {
        return bookDao.search(query);
    }

    public List<BookEntity> getAllBooksSync() {
        try {
            return executorService.submit(() -> {
                return bookDao.getAllSync();
            }).get();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public void insert(BookEntity book) {
        executorService.execute(() -> bookDao.insert(book));
    }

    public void update(BookEntity book) {
        executorService.execute(() -> bookDao.update(book));
    }

    public void delete(long id) {
        executorService.execute(() -> bookDao.delete(id));
    }

    public void fetchBookByIsbn(String isbn, FetchCallback callback) {
        executorService.execute(() -> {
            // Check if book already exists
            BookEntity existingBook = bookDao.getByIsbn(isbn);
            if (existingBook != null) {
                callback.onSuccess(existingBook);
                return;
            }

            // Fetch from API
            BookEntity book = apiService.fetchBookByIsbn(isbn);
            if (book != null) {
                long id = bookDao.insert(book);
                book.setId(id);
                callback.onSuccess(book);
            } else {
                callback.onError("Nie znaleziono książki o podanym ISBN");
            }
        });
    }

    public interface FetchCallback {
        void onSuccess(BookEntity book);
        void onError(String error);
    }
}

