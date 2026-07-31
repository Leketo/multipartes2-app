package py.multipartesapp.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PdfFileUtils {

    private static final int BUFFER_SIZE = 8192;

    public static File copyToDownloads(File sourceFile, String fileNamePrefix) throws IOException {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            throw new IOException("Archivo PDF no encontrado.");
        }

        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsFolder.exists() && !downloadsFolder.mkdirs()) {
            throw new IOException("No se pudo acceder a Descargas.");
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File targetFile = uniqueFile(downloadsFolder, fileNamePrefix + "_" + timestamp, ".pdf");
        copyFile(sourceFile, targetFile);
        return targetFile;
    }

    private static File uniqueFile(File folder, String baseName, String extension) {
        File file = new File(folder, baseName + extension);
        int index = 1;
        while (file.exists()) {
            file = new File(folder, baseName + "_" + index + extension);
            index++;
        }
        return file;
    }

    private static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(sourceFile);
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }
}
