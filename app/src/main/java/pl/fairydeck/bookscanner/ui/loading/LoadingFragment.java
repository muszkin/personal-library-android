package pl.fairydeck.bookscanner.ui.loading;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import pl.fairydeck.bookscanner.R;
import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.data.repository.BookRepository;
import pl.fairydeck.bookscanner.databinding.FragmentLoadingBinding;

public class LoadingFragment extends Fragment {
    private FragmentLoadingBinding binding;
    private String isbn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isbn = getArguments().getString("isbn");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentLoadingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isbn == null || isbn.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_isbn), Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        BookRepository repository = new BookRepository((android.app.Application) requireContext().getApplicationContext());
        Handler mainHandler = new Handler(Looper.getMainLooper());
        
        repository.fetchBookByIsbn(isbn, new BookRepository.FetchCallback() {
            @Override
            public void onSuccess(BookEntity book) {
                mainHandler.post(() -> {
                    if (getView() != null) {
                        Bundle args = new Bundle();
                        args.putLong("bookId", book.getId());
                        Navigation.findNavController(getView())
                                .navigate(R.id.action_loadingFragment_to_bookDetailsFragment, args);
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    if (getView() != null) {
                        // Navigate to book details with empty book for manual entry
                        Bundle args = new Bundle();
                        args.putString("isbn", isbn);
                        args.putBoolean("manualEntry", true);
                        Navigation.findNavController(getView())
                                .navigate(R.id.action_loadingFragment_to_bookDetailsFragment, args);
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

