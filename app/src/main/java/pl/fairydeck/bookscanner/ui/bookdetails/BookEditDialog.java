package pl.fairydeck.bookscanner.ui.bookdetails;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import pl.fairydeck.bookscanner.R;
import pl.fairydeck.bookscanner.data.database.BookEntity;

public class BookEditDialog extends DialogFragment {
    private BookEntity book;
    private OnSaveListener listener;

    public interface OnSaveListener {
        void onSave(BookEntity book);
    }

    public static BookEditDialog newInstance(BookEntity book, OnSaveListener listener) {
        BookEditDialog dialog = new BookEditDialog();
        dialog.book = book;
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_book_edit, container, false);

        EditText editTitle = view.findViewById(R.id.editTextTitle);
        EditText editAuthor = view.findViewById(R.id.editTextAuthor);
        EditText editIsbn = view.findViewById(R.id.editTextIsbn);
        EditText editPublisher = view.findViewById(R.id.editTextPublisher);
        EditText editPublishedDate = view.findViewById(R.id.editTextPublishedDate);
        EditText editDescription = view.findViewById(R.id.editTextDescription);

        if (book != null) {
            editTitle.setText(book.getTitle());
            editAuthor.setText(book.getAuthor());
            editIsbn.setText(book.getIsbn());
            editPublisher.setText(book.getPublisher());
            editPublishedDate.setText(book.getPublishedDate());
            editDescription.setText(book.getDescription());
        }

        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener(v -> {
            if (book == null) {
                book = new BookEntity();
            }
            book.setTitle(editTitle.getText().toString());
            book.setAuthor(editAuthor.getText().toString());
            book.setIsbn(editIsbn.getText().toString());
            book.setPublisher(editPublisher.getText().toString());
            book.setPublishedDate(editPublishedDate.getText().toString());
            book.setDescription(editDescription.getText().toString());

            if (listener != null) {
                listener.onSave(book);
            }
            dismiss();
        });

        buttonCancel.setOnClickListener(v -> dismiss());

        return view;
    }
}





