package pl.fairydeck.bookscanner.ui.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.graphics.pdf.PdfDocument;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pl.fairydeck.bookscanner.data.database.BookEntity;

public class ExportUtils {
    public static File exportToCsv(Context context, List<BookEntity> books) throws IOException {
        File exportDir = new File(context.getExternalFilesDir(null), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File csvFile = new File(exportDir, "books_export_" + timestamp + ".csv");

        FileWriter writer = new FileWriter(csvFile);
        
        // Write header
        writer.append("ISBN,Tytuł,Autor,Wydawca,Data wydania\n");
        
        // Write data
        for (BookEntity book : books) {
            writer.append(escapeCsv(book.getIsbn())).append(",");
            writer.append(escapeCsv(book.getTitle())).append(",");
            writer.append(escapeCsv(book.getAuthor())).append(",");
            writer.append(escapeCsv(book.getPublisher())).append(",");
            writer.append(escapeCsv(book.getPublishedDate())).append("\n");
        }
        
        writer.flush();
        writer.close();
        
        return csvFile;
    }

    public static File exportToPdf(Context context, List<BookEntity> books) throws IOException {
        File exportDir = new File(context.getExternalFilesDir(null), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File pdfFile = new File(exportDir, "books_export_" + timestamp + ".pdf");

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        titlePaint.setTextSize(16f);
        titlePaint.setFakeBoldText(true);

        int pageWidth = 595; // A4 width in points (approx for 72dpi)
        int pageHeight = 842;
        int y = 40;
        int margin = 24;
        int contentWidth = pageWidth - margin * 2;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        for (int i = 0; i < books.size(); i++) {
            BookEntity book = books.get(i);

            if (y > pageHeight - 120) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 40;
            }

            // Title
            canvas.drawText(nonNull(book.getTitle(), "Brak tytułu"), margin, y, titlePaint);
            y += 20;
            // Author
            canvas.drawText("Autor: " + nonNull(book.getAuthor(), "-"), margin, y, paint);
            y += 16;
            canvas.drawText("ISBN: " + nonNull(book.getIsbn(), "-"), margin, y, paint);
            y += 16;
            canvas.drawText("Wydawca: " + nonNull(book.getPublisher(), "-"), margin, y, paint);
            y += 16;
            canvas.drawText("Data wydania: " + nonNull(book.getPublishedDate(), "-"), margin, y, paint);
            y += 16;

            // Description trimmed
            String desc = nonNull(book.getDescription(), "");
            String[] lines = splitToLines(desc, 60);
            for (String line : lines) {
                canvas.drawText(line, margin, y, paint);
                y += 14;
            }

            // Cover image (only local)
            if (!TextUtils.isEmpty(book.getLocalCoverPath())) {
                Bitmap bmp = BitmapFactory.decodeFile(book.getLocalCoverPath());
                if (bmp != null) {
                    Bitmap scaled = Bitmap.createScaledBitmap(bmp, 120, 160, true);
                    canvas.drawBitmap(scaled, pageWidth - margin - scaled.getWidth(), y - 60, paint);
                    bmp.recycle();
                    scaled.recycle();
                }
            }

            y += 40;
        }

        pdfDocument.finishPage(page);

        FileOutputStream fos = new FileOutputStream(pdfFile);
        pdfDocument.writeTo(fos);
        fos.flush();
        fos.close();
        pdfDocument.close();

        return pdfFile;
    }

    private static String[] splitToLines(String text, int maxLen) {
        if (TextUtils.isEmpty(text)) return new String[0];
        text = text.replace("\n", " ");
        List<String> lines = new java.util.ArrayList<>();
        while (text.length() > maxLen) {
            int cut = text.lastIndexOf(' ', maxLen);
            if (cut <= 0) cut = maxLen;
            lines.add(text.substring(0, cut));
            text = text.substring(cut).trim();
        }
        if (!text.isEmpty()) lines.add(text);
        return lines.toArray(new String[0]);
    }

    private static String nonNull(String val, String fallback) {
        return val == null ? fallback : val;
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma or newline
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static Intent createShareIntent(Context context, File csvFile) {
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getApplicationContext().getPackageName() + ".fileprovider",
                csvFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        return shareIntent;
    }

    public static Intent createPdfShareIntent(Context context, File pdfFile) {
        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getApplicationContext().getPackageName() + ".fileprovider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }
}




