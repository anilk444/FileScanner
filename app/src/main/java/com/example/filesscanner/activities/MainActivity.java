package com.example.filesscanner.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filesscanner.R;
import com.example.filesscanner.adapter.LargeFilesAdapter;
import com.example.filesscanner.adapter.MostUsedFilesAdapter;
import com.example.filesscanner.common.TConstants;
import com.example.filesscanner.tasks.AbstractScanFilesTask;
import com.example.filesscanner.utils.FileUtils;
import com.example.filesscanner.utils.SDCardFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.startBtn)
    Button mStartBtn;
    @Bind(R.id.stopBtn)
    Button mStopBtn;
    @Bind(R.id.mainLayout)
    CoordinatorLayout mMainLayout;
    @Bind(R.id.statusTv)
    TextView mStatusTv;
    @Bind(R.id.fileStatsTv)
    TextView mFileStatsTv;
    @Bind(R.id.bigFilesList)
    ListView bigFilesListView;
    @Bind(R.id.mostFilesList)
    ListView mostFilesListView;

    private ArrayList<SDCardFile> mFilesList = null;
    private String[] mFileExtensions;
    private TreeMap<String, Integer> mSortedFrequencyCountMap;
    private TreeMap<String, Integer> mFrequencyCountMap;
    private HashMap<String, Double> mTopSizedFiles;
    private double mTotalFileSize;
    private ArrayList<SDCardFile> mSortedFiles = null;
    private double mTop10FileSize;
    private ProgressBar mProgressBar;
    private ScanFilesTask mScanFilesTask;

    private String mTotalFilesAvgSize, mFileStats;
    private NotificationManager mNotificationManager;

    private final int PERMISSION_REQ = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        mMainLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStartBtn = (Button) findViewById(R.id.startBtn);

        mStopBtn = (Button) findViewById(R.id.stopBtn);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mStatusTv = (TextView) findViewById(R.id.statusTv);
        mFileStatsTv = (TextView) findViewById(R.id.fileStatsTv);

        mFilesList = new ArrayList<>();
        mFileExtensions = FileUtils.getFormatArray();
        mSortedFrequencyCountMap = new TreeMap<>();
        mFrequencyCountMap = new TreeMap<>();
        mTopSizedFiles = new HashMap<>();
        mSortedFiles = new ArrayList<>();
    }

    @OnClick(R.id.startBtn)
    void startclicked(){
        startScanningFiles();
    }

    @OnClick(R.id.stopBtn)
    void stopclicked(){
        stopScanning();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.hasExtra(TConstants.INTENT_EXTRA)) {
            if (intent.getExtras().getInt(TConstants.INTENT_EXTRA) == TConstants.STOP)
                stopScanning();
        }
    }

    private void startScanningFiles() {
        if (!isPermissionCheckRequired()) {
            scanFiles();
        }
    }

    /**
     * Start the scanning process
     */
    private void scanFiles() {

        mScanFilesTask = new ScanFilesTask(MainActivity.this, mFileExtensions, mSortedFrequencyCountMap, mFrequencyCountMap, mFilesList, mTopSizedFiles, mTotalFileSize,
                mSortedFiles, mTop10FileSize);
        mScanFilesTask.execute();
        mProgressBar.setVisibility(View.VISIBLE);
        mStatusTv.setVisibility(View.GONE);
        mFileStats = null;

        createNotificaton();
    }

    private class ScanFilesTask extends AbstractScanFilesTask {

        public ScanFilesTask(Context context, String[] fileExtensions, TreeMap<String, Integer> sortedFrequencyCountMap, TreeMap<String, Integer> frequencyCountMap,
                             ArrayList<SDCardFile> filesList, HashMap<String, Double> topSizedFiles, double totalFileSize, ArrayList<SDCardFile> sortedFiles, double top10FileSize) {
            super(context, fileExtensions, sortedFrequencyCountMap, frequencyCountMap, filesList, topSizedFiles, totalFileSize, sortedFiles, top10FileSize);
        }

        @Override
        protected void onSuccess(ArrayList<SDCardFile> sortedFiles, TreeMap<String, Integer> sortedFrequencyCountMap,
                                 String totalFilesAvgSize, String top10FilesAvg) {

            ButterKnife.findById(MainActivity.this, R.id.mostFilesTv).setVisibility(View.VISIBLE);
            ButterKnife.findById(MainActivity.this, R.id.bigFilesTv).setVisibility(View.VISIBLE);

            LargeFilesAdapter largeFilesAdapter = new LargeFilesAdapter(sortedFiles, MainActivity.this);
            bigFilesListView.setAdapter(largeFilesAdapter);

            MostUsedFilesAdapter mostUsedFilesAdapter = new MostUsedFilesAdapter(sortedFrequencyCountMap, MainActivity.this);
            mostFilesListView.setAdapter(mostUsedFilesAdapter);

            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.GONE);
            }

            MainActivity.this.mFileStats = "Average = " + totalFilesAvgSize +
                    "\n 10 Top files Avg = " + top10FilesAvg;

            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_bottom);

            mFileStatsTv.setText(mFileStats);
            mFileStatsTv.startAnimation(animation);
            mFileStatsTv.setVisibility(View.VISIBLE);

            removeNotification();
            invalidateOptionsMenu();
        }

        @Override
        protected void onFailure() {
            mStatusTv.setText(getString(R.string.not_scanned_string));
            mStatusTv.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            removeNotification();
        }
    }

    /**
     * If VERSION > Android M, request for dynamic permission
     *
     * @return
     */
    private boolean isPermissionCheckRequired() {
        //Check for permission if API Level > 23
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //Request for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQ);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    scanFiles();

                } else {

                    //Show Retry option
                    Snackbar snackbar = Snackbar
                            .make(mMainLayout, getString(R.string.permission_denied_string), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.retry_string), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startScanningFiles();
                                }
                            });

                    snackbar.setActionTextColor(Color.RED);

                    View snackbarView = snackbar.getView();
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.YELLOW);
                    snackbar.show();
                }
                return;
            }
        }
    }

    private void stopScanning() {
        if (mScanFilesTask != null && mScanFilesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mScanFilesTask.cancel(true);

            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Scan stopped", Toast.LENGTH_LONG).show();

            removeNotification();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //Share button appear only after successful results
        if (mFileStats != null && mFileStats.length() > 0) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        } else
            getMenuInflater().inflate(R.menu.menu_default, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {

            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "File Stats");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mFileStats);
            startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Create a notification
     * Show big notation; User can stop current scan from notification bar
     */
    public void createNotificaton() {

        Intent stopIntent = new Intent(getApplicationContext(), MainActivity.class);
        stopIntent.putExtra(TConstants.INTENT_EXTRA, TConstants.STOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, stopIntent, 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.scan_on_going))

                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getApplicationContext().getResources().getString(R.string.app_name))
                        .setContentText(getApplicationContext().getResources().getString(R.string.clickHereToOpen))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(""))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.small_circle_button_red,
                                getString(R.string.stop_string), resultPendingIntent);


        mBuilder.setOngoing(true);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(pendingIntent);
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(TConstants.NOTIFICATION_ID, mBuilder.build());
    }

    private void removeNotification() {
        if (mNotificationManager != null)
            mNotificationManager.cancel(TConstants.NOTIFICATION_ID);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopScanning();
    }
}