package pl.fairydeck.bookscanner.utils;

import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.regex.Pattern;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "BarcodeAnalyzer";
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$");

    private com.google.mlkit.vision.barcode.BarcodeScanner scanner;
    private BarcodeCallback callback;

    public interface BarcodeCallback {
        void onBarcodeDetected(String isbn);
    }

    public BarcodeAnalyzer(BarcodeCallback callback) {
        this.callback = callback;
        this.scanner = BarcodeScanning.getClient();
    }

    @Override
    public void analyze(ImageProxy imageProxy) {
        InputImage image = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String rawValue = barcode.getRawValue();
                        if (rawValue != null && isValidIsbn(rawValue)) {
                            String cleanIsbn = cleanIsbn(rawValue);
                            if (callback != null) {
                                callback.onBarcodeDetected(cleanIsbn);
                            }
                            imageProxy.close();
                            return;
                        }
                    }
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error processing barcode", e);
                    imageProxy.close();
                });
    }

    private boolean isValidIsbn(String value) {
        // Check if it's a valid ISBN format (10 or 13 digits, possibly with dashes/spaces)
        String cleaned = cleanIsbn(value);
        return cleaned.length() == 10 || cleaned.length() == 13;
    }

    private String cleanIsbn(String isbn) {
        // Remove all non-alphanumeric characters except X (for ISBN-10)
        return isbn.replaceAll("[^0-9X]", "");
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

