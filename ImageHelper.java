package com.arsinex.com;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {
    public File getImageFile(Drawable.ConstantState constantState, String file_name){
        try {
            // covert image to bitmap
            Drawable image = constantState.newDrawable().mutate();
            Bitmap bitmap = ((BitmapDrawable)image).getBitmap();
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            // converts bitmap to JPEG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream);

            // converts jpg to binary to write to file
            byte[] bitmapData = byteStream.toByteArray();

            // create temp directory
            File tempDirectory = new File(Environment.getRootDirectory().getAbsolutePath() + "/temp");
            if (!tempDirectory.exists()) {
                tempDirectory.mkdir();
            }

            // create image file
            File jpegFile = new File(tempDirectory, "selfie.jpg");
            jpegFile.createNewFile();
//            if (jpegFile.exists()) { jpegFile.delete(); }

            // write image into file
            FileOutputStream fos = new FileOutputStream(jpegFile,false);
            fos.write(bitmapData);
            fos.close();

            return jpegFile;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                return true;
            }
        }
        return false;
    }
}
