package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
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
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; //milliseconds
    long lastClickTime = 0;
    private boolean isLongClick = false;

    private long startTask = 0;
    private Vibrator vibrator;
    private boolean taskCompleted = false;
    private AudioFeedback audioFeedback;
    private int soundIdCompletion;
    private int soundIdDoubleTap;

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
        tactileArea = new TactileArea(this, userData, feedbackMode);

        View.OnTouchListener getCoordinates = setUpTapAndMotionListener();
        findViewById(R.id.mainView).setOnTouchListener(getCoordinates);
        View.OnLongClickListener detectLongPress = setUpLongClickListener();
        findViewById(R.id.mainView).setOnLongClickListener(detectLongPress);

        // Setup vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Setup audio success sound
        this.audioFeedback = new AudioFeedback();
        this.soundIdCompletion = audioFeedback.getSoundPool().load(this, R.raw.completion_sound, 1);
        this.soundIdDoubleTap = audioFeedback.getSoundPool().load(this, R.raw.doubletap, 1);

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
                                if (!taskCompleted){
                                    handleValueSelection();
                                } else {
                                    continueWithNextTask();
                                }
                            }
                            lastClickTime = clickTime;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if (isLongClick){
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

    // Accesses the next target within the userData target list and starts speech output
    private void continueWithNextTask() {
        if (userData.getCurrentTargetIndex() + 1 < userData.getCurrentTargetList().size()){
            userData.incrementCurrentTargetIndex();
            userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
            taskCompleted = false;
            audioFeedback.getSoundPool().play(soundIdDoubleTap, 1F, 1F, 1, 0, 1);
        } else {
            audioFeedback.getSoundPool().play(soundIdCompletion, 1F, 1F, 1, 0, 1);
            final String[] userId = userData.getUserId().split("_" + feedbackMode);
            userData.setUserID(userId[0]);
            finish();
        }

    }

    private View.OnLongClickListener setUpLongClickListener (){
        View.OnLongClickListener handleLongClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                if (!isLongClick && !taskCompleted){
                    isLongClick = true;
                    if (feedbackMode.equals(AUDIO)){
                        // Haptic feedback that slider is activated
                        VibrationEffect effect = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            effect = VibrationEffect.createOneShot(150, 85);
                            vibrator.vibrate(effect);
                        }
                    } else {
                        // TODO: Audio feedback that slider is activated?
                    }


                    // start completionTimer and record values
                    startTask = System.currentTimeMillis();

                }
                return false;
            }
        };
            return handleLongClick;


    }

    // When value has been successfully selected, provide feedback, reset values, and
    // update userData model
    private void handleValueSelection() {
        audioFeedback.getSoundPool().play(soundIdDoubleTap, 1F, 1F, 1, 0, 1);
        taskCompleted = true;
        startTask = 0;

        // Set input value
        userData.getLastMeasurement().setInput(userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size()-1).getValue());
        // Set completion time value
        userData.getLastMeasurement().setCompletionTime((long) userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size()-1).getTimestamp());

        userData.pushDataToDatabase();

    }

    ;

}