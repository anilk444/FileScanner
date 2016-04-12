package com.example.filesscanner.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.filesscanner.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import butterknife.ButterKnife;


public class MostUsedFilesAdapter extends BaseAdapter{
    private ArrayList dataList;
    private Context mContext;

    public MostUsedFilesAdapter(TreeMap<String, Integer> mostUsedFiles, Context context) {
        this.mContext = context;
        dataList = new ArrayList();
        dataList.addAll(mostUsedFiles.entrySet());
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
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

        TextView nameTv = ButterKnife.findById(convertView, R.id.nameTv);
        TextView valueTv = ButterKnife.findById(convertView, R.id.valueTv);

        Map.Entry item = (Map.Entry) dataList.get(position);

        nameTv.setText("Extension: "+(String)item.getKey());
        valueTv.setText("Frequency: "+((Integer)item.getValue()));

        return convertView;
    }
}
