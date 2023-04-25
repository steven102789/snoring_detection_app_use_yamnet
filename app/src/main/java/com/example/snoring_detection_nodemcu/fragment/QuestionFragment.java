package com.example.snoring_detection_nodemcu.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.snoring_detection_nodemcu.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class QuestionFragment extends Fragment {
    private boolean isOn = false;
    private LottieAnimationView toggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_question,container,false);
        toggle = view.findViewById(R.id.toggle_bt);
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        fireBase("led_ctrl_sw",true);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOn){
                    toggle.setMinAndMaxFrame(28,60);
                    toggle.playAnimation();
                    fireBase("led_status",false);
                    Toast.makeText(getActivity(), "LED off", Toast.LENGTH_SHORT).show();
                    isOn=false;
                }else {
                    toggle.setMinAndMaxFrame(0,28);
                    toggle.playAnimation();
                    fireBase("led_status",true);
                    VibrationEffect vibrationEffect = VibrationEffect.createOneShot(100,VibrationEffect.DEFAULT_AMPLITUDE);
                    vibrator.vibrate(vibrationEffect);
                    Toast.makeText(getActivity(), "LED on", Toast.LENGTH_SHORT).show();
                    isOn=true;
                }
            }
        });
        return view;
    }
    public static QuestionFragment newInstance() {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        // 可以將需要的參數存儲到 Bundle 中
        fragment.setArguments(args);
        return fragment;
    }
    public void fireBase(String key,Boolean value){
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://snorig-detection-default-rtdb.firebaseio.com/");
        myRef.child("led_control").child(key)
                                            .setValue(value);

    }


}