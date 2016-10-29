package io.github.engsergiu.react;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File handler used for editing files.
 * @author Sergiu
 */
class FileHandler {
    private String folder;  //the folder which contains the file
    private String fileName; //the name of the file
    private boolean clearPath; // true if the folder path is vaild

    /**
     * @param folder   folder containing app's files
     * @param fileName name of the file
     */
    public FileHandler(String folder, String fileName) {
        this.folder = folder;
        this.fileName = fileName;
        this.clearPath = false;

        makePath(folder);
    }


    /**
     * Create the path for the generated file
     */
    private void makePath(String folder) {
        File directory = new File(Environment.getExternalStorageDirectory() + "/" + folder);
        this.clearPath = directory.isDirectory() ? true : directory.mkdirs();
    }


    /**
     * Write data to file in append mode
     *
     * @param content written content
     */
    public void write(String content) {
        if (isExternalStorageWritable() && this.clearPath) {
            File file;
            FileOutputStream outputStream;
            boolean writeColumnNames;
            try {
                file = new File(Environment.getExternalStorageDirectory(), this.folder + "/" + this.fileName);
                writeColumnNames=file.isFile()?false:true;
                outputStream = new FileOutputStream(file, true);
                if (writeColumnNames)
                    outputStream.write("\"date\",\"reaction time\",\"red screen duration\"\n".getBytes());
                outputStream.write(content.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Check if external storage is available for read and write
     *
     * @see <a href="https://developer.android.com/training/basics/data-storage/files.html">Saving Files | Android Developers</a>
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
