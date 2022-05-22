package com.example.tactileslider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TactileSlider tactileSlider;
    UserData userData;

    // Tactile Area Variant
    TactileArea tactileArea;
    private int xTouch;
    private int yTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);
        // TODO: setup data mask for userID entry before experiment (move LinearLayout and Slider
        // TODO: to "onUserIDsumbitted"
        userData = new UserData("TestID", 1);
        setContentView(R.layout.activity_main);
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