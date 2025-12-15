package pl.fairydeck.bookscanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import pl.fairydeck.bookscanner.data.database.BookEntity;
import pl.fairydeck.bookscanner.data.repository.BookRepository;
import pl.fairydeck.bookscanner.databinding.ActivityMainBinding;
import pl.fairydeck.bookscanner.ui.booklist.BookListViewModel;
import pl.fairydeck.bookscanner.ui.export.ExportUtils;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private BookRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        repository = new BookRepository(getApplication());

        // Setup navigation after view is created
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    navController.navigate(R.id.action_bookListFragment_to_scannerFragment);
                } catch (Exception e) {
                    // NavController might not be ready yet
                    Toast.makeText(MainActivity.this, "Błąd nawigacji", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Change FAB icon to plus
        binding.fab.setImageResource(android.R.drawable.ic_input_add);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export_csv) {
            exportToCsv();
            return true;
        } else if (id == R.id.action_export_pdf) {
            exportToPdf();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportToCsv() {
        new Thread(() -> {
            try {
                List<BookEntity> books = repository.getAllBooksSync();
                if (books == null || books.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Brak książek do eksportu", Toast.LENGTH_SHORT).show());
                    return;
                }

                File csvFile = ExportUtils.exportToCsv(this, books);
                Intent shareIntent = ExportUtils.createShareIntent(this, csvFile);
                
                runOnUiThread(() -> {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.export_csv)));
                    Toast.makeText(this, "Eksport zakończony", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Błąd eksportu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void exportToPdf() {
        new Thread(() -> {
            try {
                List<BookEntity> books = repository.getAllBooksSync();
                if (books == null || books.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "Brak książek do eksportu", Toast.LENGTH_SHORT).show());
                    return;
                }

                File pdfFile = ExportUtils.exportToPdf(this, books);
                Intent shareIntent = ExportUtils.createPdfShareIntent(this, pdfFile);

                runOnUiThread(() -> {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.export_pdf)));
                    Toast.makeText(this, "Eksport PDF zakończony", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Błąd eksportu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
