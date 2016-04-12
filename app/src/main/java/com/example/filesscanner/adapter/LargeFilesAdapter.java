package com.example.filesscanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.filesscanner.R;
import com.example.filesscanner.common.TConstants;
import com.example.filesscanner.utils.SDCardFile;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;


public class LargeFilesAdapter extends BaseAdapter{

    private final ArrayList<SDCardFile> fileExtensions;
    private Context mContext;

    public LargeFilesAdapter(ArrayList<SDCardFile> fileExtensions, Context context) {
        this.fileExtensions = fileExtensions;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return fileExtensions.size();
    }

    @Override
    public Object getItem(int position) {
        return fileExtensions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_row, null);
        }

        SDCardFile item = fileExtensions.get(position);

        TextView nameTv = ButterKnife.findById(convertView, R.id.nameTv);
        TextView valueTv = ButterKnife.findById(convertView, R.id.valueTv);

        nameTv.setText("FileName: "+item.getFileName());
        StringBuffer sb = new StringBuffer();
        String size = String.format(Locale.getDefault(),
                TConstants.TWO_DECIMALFORMATER, item.getFormattedSize());
        sb.append(size).append(item.getMetric());
        valueTv.setText("Size: "+size.toString());


        return convertView;
    }
}
