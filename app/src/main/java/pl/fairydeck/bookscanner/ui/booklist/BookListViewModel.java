package pl.fairydeck.bookscanner.ui.booklist;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.data.repository.BookRepository;

public class BookListViewModel extends AndroidViewModel {
    private BookRepository repository;
    private LiveData<List<BookEntity>> allBooks;

    public BookListViewModel(Application application) {
        super(application);
        repository = new BookRepository(application);
        allBooks = repository.getAllBooks();
    }

    public LiveData<List<BookEntity>> getAllBooks() {
        return allBooks;
    }

    public LiveData<List<BookEntity>> searchBooks(String query) {
        return repository.searchBooks(query);
    }

    public void deleteBook(long id) {
        repository.delete(id);
    }
}




