package pl.fairydeck.bookscanner.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {
    public static String saveCoverImage(Context context, Bitmap bitmap, String isbn) {
        if (bitmap == null) {
            return null;
        }
        
        File imagesDir = new File(context.getFilesDir(), "book_covers");
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }

        String filename = "cover_" + isbn + "_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imagesDir, filename);

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String saveCoverImageFromFile(Context context, File sourceFile, String isbn) {
        if (sourceFile == null || !sourceFile.exists()) {
            return null;
        }
        
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());
            return saveCoverImage(context, bitmap, isbn);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}




