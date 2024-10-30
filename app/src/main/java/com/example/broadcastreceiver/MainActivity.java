package com.example.broadcastreceiver;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{
    ArrayList<ClassHistory> myList = new ArrayList<>();
    Adapter myAdapter;
    ListView lv;
    HashMap<String,String> mapping =new HashMap<String, String>();
    private int offset=0;
    private int limit=20;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        lv=findViewById(R.id.lv);
        myAdapter=new Adapter(MainActivity.this,myList,R.layout.call_history_item);
        lv.setAdapter(myAdapter);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
        }
        else
        {
            mappingContactName();
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ android.Manifest.permission.READ_CALL_LOG}, 1);
        }
        else
        {
            getCallLog(offset,limit);
        }
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int currentVisibleItemCount;
            private int totalItemCount;
            private int currentFirstVisibleItem;
            private int currentScrollState;
            private boolean isLoading = false;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
                if (isScrollCompleted()) {
                    loadMoreCalls();
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentFirstVisibleItem = firstVisibleItem;
                this.currentVisibleItemCount = visibleItemCount;
                this.totalItemCount = totalItemCount;
            }
            private boolean isScrollCompleted() {
                return (totalItemCount - currentFirstVisibleItem) <= currentVisibleItemCount
                        && currentScrollState == SCROLL_STATE_IDLE && !isLoading;
            }
            private void loadMoreCalls() {
                isLoading = true;
                offset += limit;
                getCallLog(offset, limit);
                if (myList != null && !myList.isEmpty()) {
                    myAdapter.notifyDataSetChanged();
                }
                isLoading = false;
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCallLog(offset,limit);
            }
        }
        if (requestCode == 2)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mappingContactName();
            }
        }
    }
    public void getCallLog(int offset, int limit)
    {
        String[] projection = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };
        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC ");
        if (cursor != null) {
            if (cursor.moveToPosition(offset))
            {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE);
                int count=0;
                 do {
                    String phoneNumber = cursor.getString(numberIndex);
                    String contactName = mapping.get(phoneNumber);
                    if (contactName == null) {
                        contactName = phoneNumber;
                    }
                    int callTypeCode = cursor.getInt(typeIndex);
                    ClassHistory.Status status;
                    switch (callTypeCode) {
                        case CallLog.Calls.INCOMING_TYPE:
                            status = ClassHistory.Status.RECEIVE;
                            break;
                        case CallLog.Calls.OUTGOING_TYPE:
                            status = ClassHistory.Status.FORWARDED;
                            break;
                        case CallLog.Calls.MISSED_TYPE:
                            status = ClassHistory.Status.MISSED;
                            break;
                        default:
                            status = ClassHistory.Status.REJECTED;
                            break;
                    }
                    String callDate = cursor.getString(dateIndex);
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String formattedDate = formatter.format(new Date(Long.parseLong(callDate)));
                    ClassHistory temp = new ClassHistory(contactName, formattedDate, status);
                    myList.add(temp);
                } while (cursor.moveToNext() && count<limit);
            }
            cursor.close();
        }
        myAdapter.notifyDataSetChanged();
    }
    public void mappingContactName()
    {
        ContentResolver contentResolver=getContentResolver();
        Uri contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        Cursor cursor = contentResolver.query(contactsUri, projection, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String cleanPhoneNumber = phoneNumber.replace("(","")
                                                     .replace(")","")
                                                     .replace("-","")
                                                     .replace(" ","");
                mapping.put(cleanPhoneNumber,name);
            }
            cursor.close();
        }
    }
}