package com.example.snoring_detection_nodemcu.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snoring_detection_nodemcu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.SimpleTimeZone;


public class LogFragment extends Fragment {
    private ListView listView;
    private TextView dataText;
    private Calendar calendar;
    private ImageButton previousDay,nextDay;
    private ArrayList<String> timesList = new ArrayList<>();
    private int selectedY,selectedM,selectedD;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);
        listView = (ListView) view.findViewById(R.id.data);
        dataText = (TextView) view.findViewById(R.id.calendar);
        previousDay = (ImageButton)view.findViewById(R.id.previousDay);
        nextDay = (ImageButton)view.findViewById(R.id.nextDay);

        calendar = Calendar.getInstance();
        setDate();

        //初始現在時間
        upDataText();


        dataText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(), (view1, year, month, dayOfMonth) -> {
                        selectedY = year;
                        selectedM = month;
                        selectedD = dayOfMonth;

                        upDataText();
                    },selectedY,selectedM,selectedD
            );
            datePickerDialog.show();
        });

        previousDay.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            setDate();
            upDataText();
        });

        nextDay.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            setDate();
            upDataText();
        });



        return view;
    }

    public static LogFragment newInstance() {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        // 可以將需要的參數存儲到 Bundle 中
        fragment.setArguments(args);
        return fragment;
    }
    private void upDataText(){
        String date = String.format(Locale.getDefault(),
                "%04d/%02d/%02d",selectedY,selectedM,selectedD);
        dataText.setText(date);
        //更新日期時重新抓時間
        timesList.clear();
        readFireBaseData(selectedY,selectedM,selectedD);
    }
    private void readFireBaseData(int year, int month, int day){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://snorig-detection-default-rtdb.firebaseio.com/");
        myRef.child("TimeData")
                .child(String.valueOf(year))
                .child(String.format("%02d",month))
                .child(String.format("%02d",day))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot childSnapshot : snapshot.getChildren()){
                            String time = childSnapshot.getValue(String.class);
                            timesList.add(time);
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getActivity(), android.R.layout.simple_list_item_1,timesList);
                        listView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    private void setDate(){
        selectedY = calendar.get(Calendar.YEAR);
        selectedM = calendar.get(Calendar.MONTH)+1;
        selectedD = calendar.get(Calendar.DAY_OF_MONTH);
    }

}