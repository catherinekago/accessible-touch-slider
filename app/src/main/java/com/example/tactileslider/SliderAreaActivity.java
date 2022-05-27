package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SliderAreaActivity extends AppCompatActivity {

    TactileSlider tactileSlider;
    UserData userData;

    // Tactile Area Variant
    TactileArea tactileArea;
    private int xTouch;
    private int yTouch;
    private String feedbackMode;
    private final String AUDIO = "audio";
    private final String HAPTIC = "haptic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);
        userData = (UserData) getIntent().getSerializableExtra("userData");
        feedbackMode = (String) getIntent().getExtras().get("feedbackMode");

        setContentView(R.layout.activity_slider_area);
        //LinearLayout sliderLin = (LinearLayout) findViewById(R.id.lin1);
        //tactileSlider = new TactileSlider(this, 100, 10, userData);
        //tactileSlider.addTactileSlider(sliderLin);

        tactileArea = new TactileArea(this, userData);

        View.OnTouchListener getCoordinates = setUpTouchListener();
        findViewById(R.id.mainView).setOnTouchListener(getCoordinates);


    }

    // Setup touch listener to determine the coordinates of the touch event to handle it accordingly
    private View.OnTouchListener setUpTouchListener() {
        View.OnTouchListener handleTouch = new View.OnTouchListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                xTouch = (int) event.getX();
                yTouch = (int) event.getY();


                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("TAG", "touched down");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("TAG", "moving: (" + xTouch + ", " + yTouch + ")");

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("TAG", "touched up");
                        break;
                }
                tactileArea.handleTouchEvent(xTouch, yTouch);
                return true;

            }
        };
        return handleTouch;
    }

}