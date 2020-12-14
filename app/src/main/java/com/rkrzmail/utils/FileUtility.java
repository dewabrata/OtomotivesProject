package com.rkrzmail.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtility {

    public static byte[] convertFileToByteArray(String fileName, String filePath) {
        byte[] byteArray = null;
        try {
            File directory = new File(filePath);
            File originalFile = new File(directory, fileName);

            Bitmap bm = BitmapFactory
                    .decodeFile(originalFile.getAbsolutePath());
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byteArray = stream.toByteArray();
            stream.close();

            /*
             * Bitmap bm=
             * BitmapFactory.decodeFile(originalFile.getAbsolutePath());
             * ByteArrayOutputStream stream = new ByteArrayOutputStream();
             * bm.compress(Bitmap.CompressFormat.JPEG, 100, stream); byteArray =
             * stream.toByteArray(); stream.close();
             */
        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArray;
    }

    public static String encodeToStringBase64(String filePath) {
        String encodedFile = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            byte[] ba = bos.toByteArray();
            encodedFile = Base64.encodeToString(ba, Base64.DEFAULT);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return encodedFile;
    }

    public static void decodeBase64ToFile(String fileName, String filePath,
                                          String content) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath
                    + fileName);
            InputStream inputStream = new ByteArrayInputStream(Base64.decode(
                    content.getBytes(), Base64.DEFAULT));
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            File file=new File(filePath
                    + fileName);
            if(file.exists())
                Log.d("","Ada");
            fileOutputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String fileName, String filePath) {
        try {
            File directory = new File(filePath);
            File file = new File(directory, fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void CreatePath(String path) {
        File Directory = new File(path);
        if (!Directory.exists())
            Directory.mkdirs();
    }

    public static void resizeImage(String fileName, String filePath,
                                   int MaxSize, int SResolution) {
        try {
            FileOutputStream fOutputStream;
            CreatePath(filePath);
            File directory = new File(filePath);
            File originalFile = new File(directory, fileName);

            Bitmap bm = BitmapFactory
                    .decodeFile(originalFile.getAbsolutePath());

            int width = bm.getWidth();
            int height = bm.getHeight();

            float ratio = Math.min((float) MaxSize / width, (float) MaxSize
                    / height);

            int newwidth = Math.round(ratio * width);
            int newheight = Math.round(ratio * height);

            Matrix mtrx = new Matrix();

            mtrx.setRectToRect(new RectF(0, 0, bm.getWidth(), bm.getHeight()),
                    new RectF(0, 0, newwidth, newheight),
                    Matrix.ScaleToFit.CENTER);

            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    mtrx, false);

            File fileResized = new File(directory, "RES" + fileName);
            File newfileName = new File(directory, fileName);

            fOutputStream = new FileOutputStream(fileResized);
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, SResolution,
                    fOutputStream);
            fOutputStream.flush();
            fOutputStream.close();
            originalFile.delete();
            fileResized.renameTo(newfileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void resizeImageFile(String fileName, String fileNameNew,
                                       String filePath, String filePathNew, int MaxSize, int SResolution) {
        try {
            FileOutputStream fOutputStream;
            CreatePath(filePathNew);
            File directory = new File(filePath);
            File originalFile = new File(directory, fileName);

            File directoryNew = new File(filePathNew);
            // File destination= new File(directory, fileName);

            Bitmap bm = BitmapFactory
                    .decodeFile(originalFile.getAbsolutePath());

            int width = bm.getWidth();
            int height = bm.getHeight();

            float ratio = Math.min((float) MaxSize / width, (float) MaxSize
                    / height);

            int newwidth = Math.round(ratio * width);
            int newheight = Math.round(ratio * height);

            Matrix mtrx = new Matrix();

            mtrx.setRectToRect(new RectF(0, 0, bm.getWidth(), bm.getHeight()),
                    new RectF(0, 0, newwidth, newheight),
                    Matrix.ScaleToFit.CENTER);

            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    mtrx, false);

            File fileResized = new File(directoryNew, fileNameNew);

            fOutputStream = new FileOutputStream(fileResized);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, SResolution,
                    fOutputStream);
            fOutputStream.flush();
            fOutputStream.close();
            // originalFile.delete();
            // fileResized.renameTo(newfileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void bmptofile(Bitmap bm, String fileName,
                                 String fileNameNew, String filePath, String filePathNew,
                                 int Swidth, int Sheight, int SResolution) {
        try {
            FileOutputStream fOutputStream;

            File directoryNew = new File(filePathNew);

            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = ((float) Swidth) / width;
            float scaleHeight = ((float) Sheight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                    matrix, false);

            File fileResized = new File(directoryNew, fileNameNew);
            fOutputStream = new FileOutputStream(fileResized);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    fOutputStream);
            fOutputStream.flush();
            fOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
