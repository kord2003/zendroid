package com.zendroid;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

    private final static String FILE_EXT = ".zip";

    private static final String TAG = Zipper.class.getName();

    @SuppressWarnings("deprecation")
    public static File zipFile(Context context, File file) {
        try {
            // make zip file
            String zipFileName = file.getName();
            int lastIndex = zipFileName.lastIndexOf(".");
            if (lastIndex != -1) {
                zipFileName = zipFileName.substring(0, lastIndex);
            }
            zipFileName = Environment.getExternalStorageDirectory() + "/"  + zipFileName + FILE_EXT;

            //Log.d(TAG, "zipFileName: " +  zipFileName);

            // write to zip
            OutputStream os = new FileOutputStream(zipFileName);
            ZipOutputStream zipWriter = new ZipOutputStream(os);
            zip(file, zipWriter);
            zipWriter.close();

            return new File(zipFileName);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void zip(File file, ZipOutputStream zipWriter) throws IOException {
        if (file.isDirectory()) {
            for (File innerFile : file.listFiles()) {
                zip(innerFile, zipWriter);
            }
        } else {
            byte[] buffer = new byte[1024];
            FileInputStream reader = new FileInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipWriter.putNextEntry(zipEntry);

            int len;
            while ((len = reader.read(buffer)) > 0) {
                zipWriter.write(buffer, 0, len);
            }

            zipWriter.closeEntry();
            reader.close();
        }
    }

}
