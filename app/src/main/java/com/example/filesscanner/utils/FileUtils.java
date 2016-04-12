package com.example.filesscanner.utils;

import com.example.filesscanner.common.TConstants;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Locale;


public class FileUtils {

    private final String TAG = FileUtils.class.getSimpleName();

    public static void readDir(File file, ArrayList<SDCardFile> filesList, final String[] fileExtensions) {
        try {
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    for (String fileExtension : fileExtensions) {
                        String fileName = pathname.getName();
                        if (fileName != null)
                            fileName = fileName.toLowerCase(Locale.getDefault());
                        if (fileName.endsWith(fileExtension)
                                || ((pathname.isDirectory()))) {
                            return true;
                        }
                    }

                    return false;
                }
            });

            if (files != null) {
                for (int index = 0; index < files.length; index++) {
                    if (files[index].isDirectory()) {
                        readDir(files[index], filesList, fileExtensions);
                    } else {
                        if (files[index].isAbsolute()
                                && !files[index].isHidden()
                                && files[index].isFile()) {
                            String name = files[index].getName();
                           /* if (filesScanAsynTask.isCancelled()) {
                                return;
                            }*/
                            filesList.add(new SDCardFile(
                                    files[index].getAbsolutePath()
                                    , name, name.substring(name.lastIndexOf('.')),
                                    (double) files[index].length()));

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileSizeInMetricFormat(double size) {
        String metric = "";
        // Logic to determine the file size units.
        if (size >= (TConstants.ONE_KB
                * TConstants.ONE_KB * TConstants.ONE_KB)) {
            metric = TConstants.GB;
            size = size
                    / (TConstants.ONE_KB
                    * TConstants.ONE_KB * TConstants.ONE_KB);
        } else if (size >= (TConstants.ONE_KB * TConstants.ONE_KB)) {
            metric = TConstants.MB;
            size = size
                    / (TConstants.ONE_KB * TConstants.ONE_KB);
        } else {
            metric = TConstants.KB;
            size = size / TConstants.ONE_KB;
        }
        String sizeWithLocale = String.format(Locale.getDefault(),
                TConstants.TWO_DECIMALFORMATER, size);
        return sizeWithLocale + metric;
    }

    public static String[] getFormatArray() {

        String[] fileTypes = new String[]{".doc", ".docx", ".xls", ".txt", ".pdf"};
        return fileTypes;
    }

    public static SDCardFile getFile(String fileName, ArrayList<SDCardFile> filesList) {
        for (SDCardFile file : filesList) {
            if (file.getFileName().equalsIgnoreCase(fileName)) {
                return file;
            }
        }
        return null;
    }
}
