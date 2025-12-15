package pl.fairydeck.bookscanner.ui.booklist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import pl.fairydeck.bookscanner.R;
import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.databinding.FragmentBookListBinding;

public class BookListFragment extends Fragment {
    private FragmentBookListBinding binding;
    private BookListViewModel viewModel;
    private BookListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(BookListViewModel.class);

        adapter = new BookListAdapter(book -> {
            // Navigate to book details
            Bundle args = new Bundle();
            args.putLong("bookId", book.getId());
            Navigation.findNavController(view).navigate(R.id.action_bookListFragment_to_bookDetailsFragment, args);
        });

        RecyclerView recyclerView = binding.recyclerViewBooks;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                BookEntity book = adapter.getCurrentList().get(position);
                if (direction == ItemTouchHelper.LEFT) {
                    // Confirm deletion
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Usuń książkę")
                            .setMessage("Czy na pewno chcesz usunąć tę książkę?")
                            .setPositiveButton("Usuń", (dialog, which) -> {
                                viewModel.deleteBook(book.getId());
                            })
                            .setNegativeButton("Anuluj", (dialog, which) -> adapter.notifyItemChanged(position))
                            .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Navigate to edit (book details)
                    Bundle args = new Bundle();
                    args.putLong("bookId", book.getId());
                    Navigation.findNavController(view).navigate(R.id.action_bookListFragment_to_bookDetailsFragment, args);
                    adapter.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        viewModel.getAllBooks().observe(getViewLifecycleOwner(), books -> {
            adapter.submitList(books);
            if (books == null || books.isEmpty()) {
                binding.textViewEmpty.setVisibility(View.VISIBLE);
                binding.recyclerViewBooks.setVisibility(View.GONE);
            } else {
                binding.textViewEmpty.setVisibility(View.GONE);
                binding.recyclerViewBooks.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

