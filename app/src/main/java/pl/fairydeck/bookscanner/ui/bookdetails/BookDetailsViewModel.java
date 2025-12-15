package pl.fairydeck.bookscanner.ui.bookdetails;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.data.repository.BookRepository;

public class BookDetailsViewModel extends AndroidViewModel {
    private BookRepository repository;
    private MutableLiveData<BookEntity> currentBook = new MutableLiveData<>();

    public BookDetailsViewModel(Application application) {
        super(application);
        repository = new BookRepository(application);
    }

    public LiveData<BookEntity> getBookById(long id) {
        return repository.getBookById(id);
    }

    public MutableLiveData<BookEntity> getCurrentBook() {
        return currentBook;
    }

    public void setCurrentBook(BookEntity book) {
        currentBook.setValue(book);
    }

    public void saveBook(BookEntity book) {
        if (book.getId() == 0) {
            repository.insert(book);
        } else {
            repository.update(book);
        }
    }
}





