package com.example.snoring_detection_nodemcu.audio;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.DateSorter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.airbnb.lottie.LottieAnimationView;
import com.example.snoring_detection_nodemcu.GoodMorning_activity;
import com.example.snoring_detection_nodemcu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AudioClassificationActivity extends AppCompatActivity {
    //ml audio public
    private ScheduledExecutorService scheduledExecutorService;
    public final static int REQUEST_RECORD_AUDIO = 2033;
    private final String  model = "yamnet_classification.tflite";
    float probabilityThreshold = 0.3f;
    AudioClassifier audioClassifier;
    private TensorAudio tensorAudio;
    private AudioRecord audioRecord;
    private ScheduledFuture<?> scheduledFuture;
    private TextView outputText,wakeUpTime;
    private ImageButton stop;
    private LottieAnimationView wave;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private BroadcastReceiver stopReceiver;
    private Calendar calendar2;
    final String Action = "stop_recording";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_classification);

        stop = findViewById(R.id.stop);
        outputText = findViewById(R.id.outputText);
        wakeUpTime = findViewById(R.id.wake_up_time);
        wave = findViewById(R.id.wave);

        fireBase("led_ctrl_sw",false);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        getIntentData("calendar","vibrate_check");
        //權限檢查
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
        //載入模型
        try {
            audioClassifier = AudioClassifier.createFromFile(this, model);
        } catch (IOException e) {
            e.printStackTrace();
            // 在发生异常时可以在 UI 上显示一个错误提示
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
            return;
        }
        //ml audio part
        tensorAudio = audioClassifier.createInputTensorAudio();
        TensorAudio.TensorAudioFormat format = audioClassifier.getRequiredTensorAudioFormat();
        //啟用錄音
        audioRecord = audioClassifier.createAudioRecord();
        audioRecord.startRecording();
        startRecording();


        //接收broadcast
        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("AudioClassification", "Received STOP action from AlarmReceiver");
                fireBase("led_ctrl_sw",true);
                stopRecording();
                goodMorning();
            }
        };


        IntentFilter filter = new IntentFilter(Action);
        registerReceiver(stopReceiver, filter);


        stop.setOnClickListener(v -> onBackPressed());
        wave.setScaleX(-1f);
    }
    @Override
    public void onBackPressed() {
        stopRecording();
        Log.v("record", "stopRecord");
        cancelAlarm();
        fireBase("led_ctrl_sw",true);
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //ml audio classification
    public void startRecording() {
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //classifying audio data
                int numberOfSamples = tensorAudio.load(audioRecord);
                List<Classifications> output = audioClassifier.classify(tensorAudio);

                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output){
                    for (Category category : classifications.getCategories()){
                        if (category.getScore() > probabilityThreshold){
                            finalOutput.add(category);
                        }
                        if (category.getLabel().equals("Snoring")
                                && category.getScore() > probabilityThreshold){
                            Log.v("record","Snoring");
                        }
                    }
                }
                //sorting the results
                Collections.sort(finalOutput, ((o1, o2) -> (int) (o1.getScore() - o2.getScore())));

                //creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput){
                    outputStr.append(category.getLabel())
                            .append(":")
                            .append(category.getScore())
                            .append("\n");
                }

                //updating the ui
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalOutput.isEmpty()){
                            outputText.setText("Non-snoring");
                        }else {
                            boolean isSnoring = false;
                            for (Category category : finalOutput){
                                if (category.getLabel().equals("Snoring") && category.getScore() > probabilityThreshold){
                                    isSnoring = true;
                                    break;
                                }
                            }
                            if (isSnoring) {
                                outputText.setText("Snoring");
                                fireBase("led_snoring",true);
                                fireBaseSendTime();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        fireBase("led_snoring",false);
                                    }
                                }, 3000);
                            } else {
                                outputText.setText("Non-snoring");
                            }
                        }
                    }
                });

            }
        },0 ,1 , TimeUnit.SECONDS );
        Log.v("record","startRecord");
    }
    public void stopRecording(){
        audioRecord.stop();
        audioRecord.release();
        scheduledFuture.cancel(false);
        scheduledExecutorService.shutdown();
        Log.v("record", "stopRecord");
    }

    //alarm clock
    public void setAlarm(Calendar calendar){
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(Action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.v("alarm","alarm set");
    }
    public void cancelAlarm(){
        Intent intent = new Intent(Action);
        pendingIntent = PendingIntent.getBroadcast(this,0,intent,0);

        if (alarmManager == null){
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
        alarmManager.cancel(pendingIntent);
        Log.v("alarm","alarm cancel");

    }
    public void getIntentData(String... args){
         for (String arg : args){
             if (arg=="calendar");{
                 getIntent().hasExtra("calendar");
                 Calendar calendar = (Calendar) getIntent().getSerializableExtra("calendar");
                 Log.v("clock", "Calendar information received: " + calendar.getTime());
                 setAlarm(calendar);
                 wakeUpTime.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                         String.format("%02d", calendar.get(Calendar.MINUTE)));
             }
             if (arg=="vibrate_check"){
                 getIntent().hasExtra("vibrate_check");
                 boolean vibrate_check = getIntent().getBooleanExtra("vibrate_check",false);
                 Log.v("vibrate_check", ""+vibrate_check);
             }
         }
    }

    //others
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stopReceiver);
    }
    public void goodMorning(){
        Intent intent2 = new Intent(this, GoodMorning_activity.class);
        startActivity(intent2);
    }
    public void fireBase(String key,Boolean value){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://snorig-detection-default-rtdb.firebaseio.com/");
        myRef.child("led_control").child(key)
                .setValue(value);

    }
    public void fireBaseSendTime(){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://snorig-detection-default-rtdb.firebaseio.com/");
        String month,day,hour,minute;
        calendar2 = Calendar.getInstance();

        month = String.format("%02d",calendar2.get(Calendar.MONTH)+1);
        day = String.format("%02d",calendar2.get(Calendar.DAY_OF_MONTH));
        hour = String.format("%02d",calendar2.get(Calendar.HOUR_OF_DAY));
        minute = String.format("%02d",calendar2.get(Calendar.MINUTE));

        myRef.child("TimeData")
                .child("2023")
                .child(month)
                .child(day)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            List<String> timeList = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()){
                                timeList.add(child.getValue(String.class));
                            }
                            String currentTime = hour+":"+minute;
                            if (!timeList.contains(currentTime)){//如果有相同時間
                                timeList.add(currentTime);
                                myRef.child("TimeData")
                                        .child("2023")
                                        .child(month)
                                        .child(day).setValue(timeList);
                                Log.v("firebase:",hour+":"+minute);
                            }else{Log.v("firebase:","data_same");}

                        }else {
                            List<String> timeList = new ArrayList<>();
                            timeList.add(hour+":"+minute);
                            myRef.child("TimeData")
                                    .child("2023")
                                    .child(month)
                                    .child(day)
                                    .setValue(timeList);
                            Log.v("firebase:",hour+":"+minute);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("firebase","error value.",error.toException());
                    }
                });
    }

}