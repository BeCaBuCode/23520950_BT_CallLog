package com.example.broadcastreceiver;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Adapter extends ArrayAdapter<ClassHistory> {
    Activity activity;
    ArrayList<ClassHistory> myList;
    int idLayout;

    public Adapter( Activity activity, ArrayList<ClassHistory> myList, int idLayout) {
        super(activity,idLayout,myList);
        this.activity = activity;
        this.myList = myList;
        this.idLayout = idLayout;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myInflater = activity.getLayoutInflater();
        convertView=myInflater.inflate(idLayout,null);
        ClassHistory currentHistory = myList.get(position);
        TextView t1=convertView.findViewById(R.id.txt_displayName);
        TextView t2=convertView.findViewById(R.id.txt_date);
        ImageView i1=convertView.findViewById(R.id.img_callStatus);
        t1.setText(currentHistory.getDisplayName());
        t2.setText(currentHistory.getDisplayDate());
        if (currentHistory.getStatus() == ClassHistory.Status.RECEIVE)
        {
            i1.setImageResource(R.drawable.incoming_phone);
        }
        else if (currentHistory.getStatus() == ClassHistory.Status.MISSED)
        {
            i1.setImageResource(R.drawable.missed_call);
        }
        else if (currentHistory.getStatus() == ClassHistory.Status.FORWARDED)
        {
            i1.setImageResource(R.drawable.outgoing_phone);
        }
        else
        {
            i1.setImageResource(R.drawable.new_disable_phone);
        }
        return convertView;
    }
}
