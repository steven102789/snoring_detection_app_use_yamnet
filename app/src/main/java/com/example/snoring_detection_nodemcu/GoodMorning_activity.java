package com.example.snoring_detection_nodemcu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GoodMorning_activity extends AppCompatActivity {
    ImageButton wakeup;
    private Boolean vibrate_status, ringtone_status;
    private Vibrator vibrator;
    private SharedPreferences myPrefs;
    private Uri notification_uri;
    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_good_morning);
        wakeup = (ImageButton) findViewById(R.id.wake_up);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        notification_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        //取得sharedPreference
        myPrefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
        vibrate_status = myPrefs.getBoolean("vibrate",false);
        ringtone_status = myPrefs.getBoolean("ringtone",false);
        Log.v("myPrefs","vibrate:"+vibrate_status+" ringtone:"+ringtone_status);

        if (vibrate_status){
            long[] pattern = {0, 1000, 2000};
            vibrator.vibrate(pattern, 0);
        }
        if (ringtone_status){
            mediaPlayer = MediaPlayer.create(this, notification_uri);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        wakeup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vibrate_status) {
                    vibrator.cancel();
                }
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                Intent intent = new Intent(GoodMorning_activity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        vibrator.cancel();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}