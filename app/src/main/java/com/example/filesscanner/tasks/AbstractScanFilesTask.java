package com.example.filesscanner.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.example.filesscanner.logger.TLog;
import com.example.filesscanner.utils.FileUtils;
import com.example.filesscanner.utils.SDCardFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public abstract class AbstractScanFilesTask extends AsyncTask<String, String, Void> {

    private final String TAG = AbstractScanFilesTask.class.getSimpleName();
    private String[] fileExtensions;
    private TreeMap<String, Integer> sortedFrequencyCountMap;
    private TreeMap<String, Integer> frequencyCountMap;
    private ArrayList<SDCardFile> filesList;
    private HashMap<String, Double> topSizedFiles;
    private double totalFileSize;
    private ArrayList<SDCardFile> sortedFiles;
    private double top10FileSize;

    public AbstractScanFilesTask(Context context, String[] fileExtensions, TreeMap<String, Integer> sortedFrequencyCountMap,
                                 TreeMap<String, Integer> frequencyCountMap, ArrayList<SDCardFile> filesList,
                                 HashMap<String, Double> topSizedFiles, double totalFileSize, ArrayList<SDCardFile> sortedFiles,
                                 double top10FileSize) {
        this.fileExtensions = fileExtensions;
        this.sortedFrequencyCountMap = sortedFrequencyCountMap;
        this.frequencyCountMap = frequencyCountMap;
        this.filesList = filesList;
        this.topSizedFiles = topSizedFiles;
        this.totalFileSize = totalFileSize;
        this.sortedFiles = sortedFiles;
        this.top10FileSize = top10FileSize;
    }

    @Override
    protected Void doInBackground(String... params) {
        File sdcardLoc = Environment.getExternalStorageDirectory();
        for (String fileExtension : fileExtensions) {
            frequencyCountMap.put(fileExtension, 0);
        }
        FileUtils.readDir(sdcardLoc, filesList, fileExtensions);

        String fileExt = null;
        for (SDCardFile file : filesList) {
            fileExt = file.getExtension();
            topSizedFiles.put(file.getFileName(), file.getSize());
            int count = frequencyCountMap.get(fileExt.toLowerCase());
            frequencyCountMap.put(fileExt, ++count);
            totalFileSize = totalFileSize + file.getSize();
            TLog.i(TAG, "FileName:" + file.getFileName() + " fileExt:" + fileExt + " count:" + count);

        }
        TopSizeFilesComparator topSizeFilesComparator = new TopSizeFilesComparator(topSizedFiles);
        TreeMap<String, Double> topFilesMap = new TreeMap<String, Double>(topSizeFilesComparator);
        topFilesMap.putAll(topSizedFiles);

        // get the top 10 files
        Iterator it = topFilesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String name = pair.getKey().toString();
            if (!TextUtils.isEmpty(name)) {
                if (sortedFiles.size() < 10) {
                    SDCardFile file = FileUtils.getFile(name, filesList);
                    sortedFiles.add(file);
                    top10FileSize = top10FileSize + file.getSize();
                } else {
                    break;
                }
            }
            it.remove();
        }

        ValueComparator bvc = new ValueComparator(frequencyCountMap);
        sortedFrequencyCountMap = new TreeMap<String, Integer>(bvc);
        sortedFrequencyCountMap.putAll(frequencyCountMap);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        double totalFilesAverageSize = totalFileSize / filesList.size();
        onSuccess(sortedFiles, sortedFrequencyCountMap, FileUtils.getFileSizeInMetricFormat(totalFilesAverageSize), FileUtils.getFileSizeInMetricFormat(top10FileSize / sortedFiles.size()));
    }

    protected abstract void onSuccess(ArrayList<SDCardFile> sortedFiles, TreeMap<String, Integer> sortedFrequencyCountMap, String totalFilesAvgSize, String top10FilesAvg);
    protected abstract void onFailure();

    @Override
    protected void onCancelled(Void aVoid) {
        super.onCancelled(aVoid);
        onFailure();
    }

    public class TopSizeFilesComparator implements Comparator<String> {

        Map<String, Double> base;

        public TopSizeFilesComparator(Map<String, Double> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
