package com.example.snoring_detection_nodemcu.fragment;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.snoring_detection_nodemcu.R;
import com.example.snoring_detection_nodemcu.audio.AudioClassificationActivity;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ClockFragment extends Fragment {
    private MaterialTimePicker timePicker;
    private ImageButton start_record;
    private TextView clock_text;
    private Calendar calendar;
    private View bt_vibrate,bt_ringtone;
    private LottieAnimationView vibrate_anim,ringtone_anim;
    boolean calendar_switch,vibrate_check,ringtone_check;
    private SharedPreferences myPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clock,container,false);
        clock_text = view.findViewById(R.id.clock_text);
        start_record = view.findViewById(R.id.start_record);
        calendar = Calendar.getInstance(); // 初始化 calendar 物件
        bt_ringtone = view.findViewById(R.id.bt_ringtone);
        bt_vibrate = view.findViewById(R.id.bt_vibrate);
        vibrate_anim = view.findViewById(R.id.vibrate_check);
        ringtone_anim = view.findViewById(R.id.ringtone_check);

        //取得sharedPreference
        myPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getBaseContext());
        //建立key
        myPrefs.getBoolean("vibrate",false);
        myPrefs.getBoolean("ringtone",false);

        bt_vibrate.setOnClickListener(v ->{
            vibrate_check=check(vibrate_check,vibrate_anim,"vibrate");
        });

        bt_ringtone.setOnClickListener(v -> {
            ringtone_check=check(ringtone_check,ringtone_anim,"ringtone");
        });

        clock_text.setOnClickListener(v -> {
            timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .setHour(12)
                    .setMinute(0)
                    .setTitleText("Select Alarm Time")
                    .build();
            timePicker.show(getChildFragmentManager(), "steven");
            //按下確定時
            timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("clock", "add");
                    calendar_switch = true;
                    calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    calendar.set(Calendar.MINUTE, timePicker.getMinute());
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                        // 如果設定的時間早於現在時間，將時間增加一天
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    if (timePicker.getHour() > 12) {

                        Toast.makeText(getActivity(), "set "+String.format("%02d", (calendar.get(Calendar.HOUR_OF_DAY) - 12)) + ":" +
                                String.format("%02d", calendar.get(Calendar.MINUTE)) + " PM", Toast.LENGTH_SHORT).show();
                        clock_text.setText(String.format("%02d", (calendar.get(Calendar.HOUR_OF_DAY) - 12)) + ":" +
                                String.format("%02d", calendar.get(Calendar.MINUTE)) + " PM");
                    } else {
                        Toast.makeText(getActivity(), "set "+calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                                String.format("%02d", calendar.get(Calendar.MINUTE)) + " AM", Toast.LENGTH_SHORT).show();

                        clock_text.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                                String.format("%02d", calendar.get(Calendar.MINUTE)) + " AM");
                    }
                }
            });
            //按下取消時
            timePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // 當按下取消按鈕時的程式碼
                    calendar_switch = false;
                    Log.d("clock", "cancel");
                }
            });
        });

        start_record.setOnClickListener(v -> {
            if (calendar_switch){
                Intent intent = new Intent(getActivity(), AudioClassificationActivity.class);
                intent.putExtra("calendar", calendar);
                startActivity(intent);
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else {
                Toast.makeText(getActivity(), "請設定時間", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    private boolean check(Boolean check_bt,LottieAnimationView animView,String key) {
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (check_bt){
            check_bt=false;
            editData(key,false);
            animView.setProgress(0f);
        }else {
            check_bt=true;
            editData(key,true);
            startAnimation(animView);
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect);
        }return check_bt;
    }
    private void startAnimation(LottieAnimationView animView) {
        animView.setMinAndMaxFrame(0, 49);
        animView.playAnimation();
    }
    public static ClockFragment newInstance() {
        ClockFragment fragment = new ClockFragment();
        Bundle args = new Bundle();
        // 可以將需要的參數存儲到 Bundle 中
        fragment.setArguments(args);
        return fragment;
    }
    public void editData(String key, Boolean value){
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putBoolean(key,value);
        Log.v("myPrefs",key+":"+value);
        editor.apply();
    }
}