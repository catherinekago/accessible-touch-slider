package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class SliderAreaActivity extends AppCompatActivity {

    // Gestures

    TactileSlider tactileSlider;
    UserData userData;

    // Tactile Area Variant
    TactileArea tactileArea;
    private int xTouch;
    private int yTouch;
    private String feedbackMode;
    private final String AUDIO = "audio";
    private final String HAPTIC = "haptic";
    private Context context;

    // Touch Gestures
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds
    long lastClickTime = 0;
    private boolean isLongClick = false;

    private long startTask = 0;

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

        this.context = this;
        tactileArea = new TactileArea(this, userData);

        View.OnTouchListener getCoordinates = setUpTapAndMotionListener();
        findViewById(R.id.mainView).setOnTouchListener(getCoordinates);
        View.OnLongClickListener detectLongPress = setUpLongClickListener();
        findViewById(R.id.mainView).setOnLongClickListener(detectLongPress);

    }

    // Setup touch listener to determine the coordinates of the touch event to handle it accordingly
    private View.OnTouchListener setUpTapAndMotionListener() {
        View.OnTouchListener handleTouch = new View.OnTouchListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                xTouch = (int) event.getX();
                yTouch = (int) event.getY();
                long clickTime = System.currentTimeMillis();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //Handle double click
                            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                                Toast.makeText(context, "double", Toast.LENGTH_SHORT).show();
                                handleValueSelection();
                            }
                            lastClickTime = clickTime;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isLongClick){
                                Log.i("TAG", "moving: (" + xTouch + ", " + yTouch + ")");
                                tactileArea.handleTouchEvent(xTouch, yTouch, userData, startTask);
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            isLongClick = false;
                            break;
                }
                return false;

            }
        };
        return handleTouch;
    }

    private View.OnLongClickListener setUpLongClickListener (){
        View.OnLongClickListener handleLongClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                if (!isLongClick){
                    isLongClick = true;
                    Toast.makeText(context, "longclick", Toast.LENGTH_SHORT).show();
                    // TODO start completionTimer and record values
                    // TODO add audio feedback that user has been recognized
                    // startTask = System.currentTimeMillis();
                }
                return false;
            }
        };
            return handleLongClick;


    }

    private void handleValueSelection() {
        UserData data = userData;
        data.getLastMeasurement().removeLastMeasurementPair();
        data.getLastMeasurement().removeLastMeasurementPair();
        UserData cleanedUserData = data;
    }

    ;

}