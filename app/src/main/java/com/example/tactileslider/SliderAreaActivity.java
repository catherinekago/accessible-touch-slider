package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

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
    private int soundIdLongClick;
    private TextToSpeech ttsObject;
    private boolean tasksStarted = false;

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
        this.soundIdLongClick = audioFeedback.getSoundPool().load(this, R.raw.longclick, 1);
        
        // Setup tts component
        this.ttsObject =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        Locale german = new Locale("de", "DE");
        ttsObject.setLanguage(german);

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
                                if (tasksStarted && !taskCompleted){
                                    handleValueSelection();
                                } else  if (tasksStarted && taskCompleted){
                                    continueWithNextTask();
                                } else if (!tasksStarted){
                                    startFirstTask();
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

    // Initialize first task
    private void startFirstTask() {
        audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
        readAloudTarget();
        tasksStarted = true;
    }

    // Generate speech output to read aloud task
    private void readAloudTarget(){
        int target = (int) Math.round(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        String toSpeak = "Bitte wählen Sie die " + target + ".";
        final int interval = 2000; // 1 Second
        Handler handler = new Handler();
        Runnable runnable = new Runnable(){
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.05");
                ttsObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);

    }

    // Accesses the next target within the userData target list and starts speech output
    private void continueWithNextTask() {
        if (userData.getCurrentTargetIndex() + 1 < userData.getCurrentTargetList().size()){
            userData.incrementCurrentTargetIndex();
            userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
            taskCompleted = false;
            audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
            readAloudTarget();
        } else {
            audioFeedback.getSoundPool().play(soundIdCompletion, 0.5F, 0.5F, 1, 0, 1);
            final String[] userId = userData.getUserId().split("_" + feedbackMode);
            userData.setUserID(userId[0]);
            // TODO: randomize targets again
            finish();
        }

    }

    // Setup long click listener for slider dragging interaction
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
                        audioFeedback.getSoundPool().play(soundIdLongClick, 1F, 1F, 1, 0, 1);
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