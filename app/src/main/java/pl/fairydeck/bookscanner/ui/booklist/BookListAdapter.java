package pl.fairydeck.bookscanner.ui.booklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import pl.fairydeck.bookscanner.R;
import pl.fairydeck.bookscanner.data.database.BookEntity;

public class BookListAdapter extends ListAdapter<BookEntity, BookListAdapter.BookViewHolder> {
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(BookEntity book);
    }

    public BookListAdapter(OnBookClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookEntity book = getItem(position);
        holder.bind(book);
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewCover;
        private TextView textViewTitle;
        private TextView textViewAuthor;
        private TextView textViewIsbn;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCover = itemView.findViewById(R.id.imageViewCover);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewIsbn = itemView.findViewById(R.id.textViewIsbn);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookClick(getItem(position));
                }
            });
        }

        public void bind(BookEntity book) {
            textViewTitle.setText(book.getTitle() != null ? book.getTitle() : itemView.getContext().getString(R.string.no_title));
            textViewAuthor.setText(book.getAuthor() != null ? book.getAuthor() : itemView.getContext().getString(R.string.no_author));
            textViewIsbn.setText(itemView.getContext().getString(R.string.isbn) + ": " + (book.getIsbn() != null ? book.getIsbn() : ""));

            // Load cover image
            String imageUrl = book.getCoverImageUrl();
            String localPath = book.getLocalCoverPath();
            
            if (localPath != null && !localPath.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(localPath)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageViewCover);
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(imageViewCover);
            } else {
                imageViewCover.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    private static final DiffUtil.ItemCallback<BookEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<BookEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull BookEntity oldItem, @NonNull BookEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull BookEntity oldItem, @NonNull BookEntity newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getAuthor().equals(newItem.getAuthor()) &&
                    oldItem.getIsbn().equals(newItem.getIsbn());
        }
    };
}





