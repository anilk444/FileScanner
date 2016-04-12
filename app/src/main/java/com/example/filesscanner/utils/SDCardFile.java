package com.example.filesscanner.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.filesscanner.common.TConstants;


public class SDCardFile implements Parcelable {
    private String path;
    private String fileName;
    private String fileExt;
    private String metric;
    private double size;
    private double formattedSize;


    public SDCardFile(String path, String fileName, String fileExt, double size) {

        this.path = path;
        this.fileName = fileName;
        this.fileExt = fileExt;
        formattedSize = size;

        // Logic to determine the file size units.
        if (formattedSize >= (TConstants.ONE_KB
                * TConstants.ONE_KB * TConstants.ONE_KB)) {
            metric = TConstants.GB;
            formattedSize = formattedSize
                    / (TConstants.ONE_KB
                    * TConstants.ONE_KB * TConstants.ONE_KB);
        } else if (formattedSize >= (TConstants.ONE_KB * TConstants.ONE_KB)) {
            metric = TConstants.MB;
            formattedSize = formattedSize
                    / (TConstants.ONE_KB * TConstants.ONE_KB);
        } else {
            metric = TConstants.KB;
            formattedSize = formattedSize / TConstants.ONE_KB;
        }
        this.size = size;

    }

    /**
     * Get the file path.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the file name.
     *
     * @return filename
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get file size.
     *
     * @return size
     */
    public double getSize() {
        return size;
    }

    /**
     * Get formatted size.
     *
     * @return size
     */
    public double getFormattedSize() {
        return formattedSize;
    }

    /**
     * Get file Extension.
     *
     * @return Extension
     */
    public String getExtension() {
        return fileExt;
    }

    /**
     * Get file size units like GB or MB or KB
     *
     * @return metric
     */
    public String getMetric() {
        return metric;
    }

    @Override
    public boolean equals(Object object) {
        boolean isSame = false;
        if (object != null && object instanceof SDCardFile) {
            isSame = this.fileName
                    .equalsIgnoreCase(((SDCardFile) object).fileName);
        }
        return isSame;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(path);
        out.writeString(fileName);
        out.writeString(fileExt);
        out.writeString(metric);
        out.writeDouble(size);
        out.writeDouble(formattedSize);
    }

    private SDCardFile(Parcel in) {
        this.path = in.readString();

    }

    public static final Creator<SDCardFile> CREATOR = new Creator<SDCardFile>() {
        @Override
        public SDCardFile createFromParcel(Parcel source) {
            return new SDCardFile(source);
        }

        @Override
        public SDCardFile[] newArray(int size) {
            return new SDCardFile[size];
        }
    };


    private static String TAG = SDCardFile.class.getSimpleName();

}