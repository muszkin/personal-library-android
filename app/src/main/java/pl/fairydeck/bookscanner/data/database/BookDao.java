package pl.fairydeck.bookscanner.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BookDao {
    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    LiveData<List<BookEntity>> getAll();

    @Query("SELECT * FROM books ORDER BY createdAt DESC")
    List<BookEntity> getAllSync();

    @Query("SELECT * FROM books WHERE id = :id")
    LiveData<BookEntity> getById(long id);

    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    BookEntity getByIsbn(String isbn);

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    LiveData<List<BookEntity>> search(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BookEntity book);

    @Update
    void update(BookEntity book);

    @Query("DELETE FROM books WHERE id = :id")
    void delete(long id);
}

