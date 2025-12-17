package pl.fairydeck.bookscanner.ui.bookdetails;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

import pl.fairydeck.bookscanner.R;
import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.databinding.FragmentBookDetailsBinding;
import pl.fairydeck.bookscanner.utils.ImageUtils;

public class BookDetailsFragment extends Fragment {
    private FragmentBookDetailsBinding binding;
    private BookDetailsViewModel viewModel;
    private BookEntity currentBook;
    private boolean isManualEntry = false;
    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long bookId = getArguments().getLong("bookId", 0);
            isManualEntry = getArguments().getBoolean("manualEntry", false);
            String isbn = getArguments().getString("isbn");

            if (isManualEntry && isbn != null) {
                // Create new book for manual entry
                currentBook = new BookEntity();
                currentBook.setIsbn(isbn);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentBookDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(BookDetailsViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            long bookId = args.getLong("bookId", 0);
            if (bookId > 0) {
                // Load existing book
                viewModel.getBookById(bookId).observe(getViewLifecycleOwner(), book -> {
                    if (book != null) {
                        currentBook = book;
                        viewModel.setCurrentBook(book);
                        displayBook(book);
                    }
                });
            } else if (currentBook != null) {
                // Manual entry - display empty book
                viewModel.setCurrentBook(currentBook);
                displayBook(currentBook);
            }
        }

        binding.buttonEdit.setOnClickListener(v -> showEditDialog());
        binding.buttonTakePhoto.setOnClickListener(v -> takePhoto());
        binding.buttonSave.setOnClickListener(v -> saveBook());
        binding.buttonCancel.setOnClickListener(v -> {
            // Cofnięcie zawsze wraca do poprzedniego ekranu:
            // - z listy książek -> lista
            // - ze skanera -> skaner
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void displayBook(BookEntity book) {
        String title = book.getTitle() != null ? book.getTitle() : getString(R.string.no_title);
        binding.textViewTitle.setText(title);
        binding.textViewAuthor.setText(book.getAuthor() != null ? book.getAuthor() : getString(R.string.no_author));
        binding.textViewIsbn.setText(getString(R.string.isbn) + ": " + (book.getIsbn() != null ? book.getIsbn() : ""));
        binding.textViewPublisher.setText(book.getPublisher() != null ? getString(R.string.publisher) + ": " + book.getPublisher() : "");
        binding.textViewPublishedDate.setText(book.getPublishedDate() != null ? getString(R.string.published_date) + ": " + book.getPublishedDate() : "");
        binding.textViewDescription.setText(book.getDescription() != null ? book.getDescription() : "");

        // Ustaw tytuł w pasku obok strzałki cofania (toolbar)
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(title);
        }

        // Load cover image
        String imageUrl = book.getCoverImageUrl();
        String localPath = book.getLocalCoverPath();
        
        if (localPath != null && !localPath.isEmpty()) {
            Glide.with(requireContext())
                    .load(new File(localPath))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.imageViewCover);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(binding.imageViewCover);
        } else {
            binding.imageViewCover.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void showEditDialog() {
        BookEditDialog dialog = BookEditDialog.newInstance(currentBook, book -> {
            currentBook = book;
            viewModel.setCurrentBook(book);
            displayBook(book);
        });
        dialog.show(getParentFragmentManager(), "BookEditDialog");
    }

    private void takePhoto() {
        if (checkCameraPermission()) {
            startCameraForPhoto();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 200);
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void startCameraForPhoto() {
        // Create a dialog with camera preview
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        dialog.setContentView(R.layout.fragment_camera_photo);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        androidx.camera.view.PreviewView previewView = dialog.findViewById(R.id.previewView);
        android.widget.Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        android.widget.Button buttonCapture = dialog.findViewById(R.id.buttonCapture);

        if (previewView == null || buttonCancel == null || buttonCapture == null) {
            Toast.makeText(requireContext(), getString(R.string.camera_init_error), Toast.LENGTH_SHORT).show();
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    cameraProvider.unbindAll();
                    // Use activity as lifecycle owner since dialog doesn't have lifecycle
                    camera = cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview, imageCapture);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), getString(R.string.camera_start_error), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                buttonCancel.setOnClickListener(v -> {
                    if (cameraProvider != null) {
                        cameraProvider.unbindAll();
                    }
                    dialog.dismiss();
                });

                buttonCapture.setOnClickListener(v -> {
                    if (imageCapture != null && currentBook != null) {
                        File photoFile = new File(requireContext().getFilesDir(), "temp_photo_" + System.currentTimeMillis() + ".jpg");
                        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                        imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(requireContext()),
                                new ImageCapture.OnImageSavedCallback() {
                                    @Override
                                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                                        // Save the photo and update book
                                        String savedPath = ImageUtils.saveCoverImageFromFile(requireContext(), 
                                                photoFile,
                                                currentBook.getIsbn() != null ? currentBook.getIsbn() : "unknown");
                                        
                                        if (savedPath != null) {
                                            currentBook.setLocalCoverPath(savedPath);
                                            viewModel.setCurrentBook(currentBook);
                                            displayBook(currentBook);
                                            Toast.makeText(requireContext(), getString(R.string.photo_saved), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(requireContext(), getString(R.string.photo_save_error), Toast.LENGTH_SHORT).show();
                                        }
                                        
                                        // Delete temp file
                                        photoFile.delete();
                                        if (cameraProvider != null) {
                                            cameraProvider.unbindAll();
                                        }
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onError(@NonNull ImageCaptureException exception) {
                                        Toast.makeText(requireContext(), getString(R.string.photo_capture_error, exception.getMessage()), Toast.LENGTH_SHORT).show();
                                        photoFile.delete();
                                        if (cameraProvider != null) {
                                            cameraProvider.unbindAll();
                                        }
                                        dialog.dismiss();
                                    }
                                }
                        );
                    }
                });

                dialog.setOnDismissListener(dialog1 -> {
                    if (cameraProvider != null) {
                        cameraProvider.unbindAll();
                    }
                });

                dialog.show();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(), getString(R.string.camera_init_error), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void saveBook() {
        if (currentBook == null) {
            Toast.makeText(requireContext(), getString(R.string.no_data_to_save), Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.saveBook(currentBook);
        Toast.makeText(requireContext(), getString(R.string.book_saved), Toast.LENGTH_SHORT).show();
        // Cofnięcie o jeden ekran wstecz:
        // - jeśli weszliśmy z listy, wrócimy na listę
        // - jeśli ze skanera (przez loading), wrócimy na skaner
        Navigation.findNavController(requireView()).popBackStack();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCameraForPhoto();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

