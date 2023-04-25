package com.example.snoring_detection_nodemcu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

import com.example.snoring_detection_nodemcu.fragment.ClockFragment;
import com.example.snoring_detection_nodemcu.fragment.LogFragment;
import com.example.snoring_detection_nodemcu.fragment.QuestionFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayDeque;
import java.util.Deque;

public class MainActivity extends AppCompatActivity{
    BottomNavigationView bottomNavigationView;
    Deque<Integer> integerDeque = new ArrayDeque<>(2);
    QuestionFragment questionFragment ;
    ClockFragment clockFragment ;
    LogFragment logFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("TAG", "onCreate: start MainActivity2");
        //bottomNavigation控制fragment
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        integerDeque.add(R.id.bottom_question);
        // 在 onCreate() 中查看 integerDeque 的状态
        Log.d("MainActivity", "integerDeque in onCreate: " + integerDeque.toString());

        if (savedInstanceState != null) {
            questionFragment = (QuestionFragment) getSupportFragmentManager().findFragmentByTag(QuestionFragment.class.getName());
            clockFragment = (ClockFragment) getSupportFragmentManager().findFragmentByTag(ClockFragment.class.getName());
            logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag(LogFragment.class.getName());

            getSupportFragmentManager()
                    .beginTransaction()
                    .show(questionFragment)
                    .hide(clockFragment)
                    .hide(logFragment)
                    .commit();
        }else {
            questionFragment = QuestionFragment.newInstance();
            clockFragment = ClockFragment.newInstance();
            logFragment = LogFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, questionFragment, getFragmentTag(questionFragment))
                    .add(R.id.fragment_container, clockFragment, getFragmentTag(clockFragment))
                    .add(R.id.fragment_container, logFragment, getFragmentTag(logFragment))
                    .hide(clockFragment)
                    .hide(logFragment)
                    .commit();
        }
        //set home fragment
        bottomNavigationView.setSelectedItemId(R.id.bottom_question);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id  = item.getItemId();
            // 判断前一个 id 是否与当前 id 相同
            if (!integerDeque.isEmpty() && integerDeque.peekLast() == id) {
                return true;
            }
            switch(id) {
                case R.id.bottom_question:
                    getSupportFragmentManager().beginTransaction()
                            .show(questionFragment)
                            .hide(clockFragment)
                            .hide(logFragment)
                            .commit();
                    integerDeque.add(id);
                    Log.d("MainActivity", "integerDeque after adding question: " + integerDeque.toString());

                    break;
                case R.id.bottom_alarm_clock:
                    getSupportFragmentManager().beginTransaction()
                            .show(clockFragment)
                            .hide(questionFragment)
                            .hide(logFragment)
                            .commit();
                    integerDeque.add(id);
                    Log.d("MainActivity", "integerDeque after adding alarm clock: " + integerDeque.toString());

                    break;
                case R.id.bottom_log:
                    getSupportFragmentManager().beginTransaction()
                            .show(logFragment)
                            .hide(questionFragment)
                            .hide(clockFragment)
                            .commit();
                    integerDeque.add(id);
                    Log.d("MainActivity", "integerDeque after adding log: " + integerDeque.toString());

                    break;
            }
            return true;
        });


    }
    @Override
    public void onBackPressed() {
        if (integerDeque.size() <= 1) {
            super.onBackPressed();
            return;
        }
        integerDeque.removeLast(); // 刪除最後一個 id，即當前 Fragment 的 id

        // 取得新的最後一個 id
        int lastId = integerDeque.peekLast();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment questionFragment = fragmentManager.findFragmentByTag(QuestionFragment.class.getName());
        Fragment clockFragment = fragmentManager.findFragmentByTag(ClockFragment.class.getName());
        Fragment logFragment = fragmentManager.findFragmentByTag(LogFragment.class.getName());
        switch (lastId) {
            case R.id.bottom_question:
                fragmentManager.beginTransaction()
                        .show(questionFragment)
                        .hide(clockFragment)
                        .hide(logFragment)
                        .commit();
                Log.d("onbackpressed", "question"+integerDeque.toString());
                break;
            case R.id.bottom_alarm_clock:
                fragmentManager.beginTransaction()
                        .show(clockFragment)
                        .hide(questionFragment)
                        .hide(logFragment)
                        .commit();
                Log.d("onbackpressed", "clock"+integerDeque.toString());
                break;
            case R.id.bottom_log:
                fragmentManager.beginTransaction()
                        .show(logFragment)
                        .hide(questionFragment)
                        .hide(clockFragment)
                        .commit();
                Log.d("onbackpressed", "log"+integerDeque.toString());
                break;
        }

    }
    public String getFragmentTag(Fragment fragment){
        return fragment.getClass().getName();
    }
}