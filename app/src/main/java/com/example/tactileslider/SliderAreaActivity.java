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

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class SliderAreaActivity extends AppCompatActivity {

    // Tactile Area Variant
    TactileArea tactileArea;
    private int xTouch;
    private int yTouch;

    private final String AUDIO = "audio";
    private final String TACTILE = "tactile";
    private final String COMBINED = "combined";
    private final String LONG = "long";
    private final String SHORT = "short";
    private final String HORIZONTAL = "horizontal";
    private final String VERTICAL = "vertical";

    private final String TRIAL = "trial";
    private final String STUDY = "study";
    private final int STUDY_REPETITIONS = 1; // TODO: set to 3
    private final String QUEST = "questionnaire";
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

    // Intent Extras
    UserData userData;
    ArrayList<String> feedbackModes;
    ArrayList<String> lengths;
    ArrayList<String> orientations;

    int currentVariant = 0;
    String phase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);

        getExtras();

        setContentView(R.layout.activity_slider_area);

        this.context = this;
        tactileArea = new TactileArea(this, userData, feedbackModes.get(currentVariant), lengths.get(currentVariant), orientations.get(currentVariant), phase);

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
        this.ttsObject = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        Locale german = new Locale("de", "DE");
        ttsObject.setLanguage(german);

    }

    // Get data from intent
    private void getExtras() {
        feedbackModes = new ArrayList<>();
        orientations = new ArrayList<>();
        lengths = new ArrayList<>();


        this.phase = (String) getIntent().getExtras().get("phase");
        userData = (UserData) getIntent().getSerializableExtra("userData");

        String feedbackMode_1 = (String) getIntent().getExtras().get("feedbackMode_1");
        String orientation_1 = (String) getIntent().getExtras().get("orientation_1");
        String length_1 = (String) getIntent().getExtras().get("length_1");

        feedbackModes.add(feedbackMode_1);
        orientations.add(orientation_1);
        lengths.add(length_1);

        if (phase.equals(STUDY) || phase.equals(QUEST)) {
            String feedbackMode_2 = (String) getIntent().getExtras().get("feedbackMode_2");
            String length_2 = (String) getIntent().getExtras().get("length_2");
            String orientation_2 = (String) getIntent().getExtras().get("orientation_2");
            feedbackModes.add(feedbackMode_2);
            orientations.add(orientation_2);
            lengths.add(length_2);
        }
        if (phase.equals(STUDY) && (String) getIntent().getExtras().get("feedbackMode_3") != null) {
            String feedbackMode_3 = (String) getIntent().getExtras().get("feedbackMode_3");
            String length_3 = (String) getIntent().getExtras().get("length_3");
            String orientation_3 = (String) getIntent().getExtras().get("orientation_3");
            feedbackModes.add(feedbackMode_3);
            orientations.add(orientation_3);
            lengths.add(length_3);
        }
        userData.setUserID(userData.getUserId() + "_" + feedbackModes.get(0) + "_" + lengths.get(0) + "_" + orientations.get(0) + "_" + phase);
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
                            if (tasksStarted && !taskCompleted) {
                                handleValueSelection();
                            } else if (tasksStarted && taskCompleted) {
                                continueWithNextTask();
                            } else if (!tasksStarted) {
                                startFirstTask();
                            }
                        }
                        lastClickTime = clickTime;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isLongClick) {
                            tactileArea.handleTouchEvent(xTouch, yTouch, userData, startTask, phase);
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
        if (phase.equals(STUDY) || phase.equals(QUEST)) {
            audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
            readAloudTarget();
            tasksStarted = true;
            // In trial task, return to selection screen
        } else {
            audioFeedback.getSoundPool().play(soundIdCompletion, 0.5F, 0.5F, 1, 0, 1);
            final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
            userData.setUserID(userId[0]);
            finish();
        }

    }

    // Generate speech output to read aloud task
    private void readAloudTarget() {

        String toSpeak;
        if (phase.equals(STUDY)) {
            int target = (int) Math.round(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
            toSpeak = "Bitte wählen Sie die " + target + ".";
        } else {
            String question = userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex());
            toSpeak = question;
        }

        final int interval = 500; // half a second
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.15");
                ttsObject.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params);
            }
        };
        handler.postAtTime(runnable, System.currentTimeMillis() + interval);
        handler.postDelayed(runnable, interval);

    }

    // Accesses the next target within the userData target list and starts speech output
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void continueWithNextTask() {
            // Handle study phase
            if (phase.equals(STUDY)) {
                if (userData.getCurrentTargetIndex() + 1 < userData.getCurrentTargetList().size()) {
                    userData.incrementCurrentTargetIndex();
                    userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
                    taskCompleted = false;
                    audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
                    readAloudTarget();
                } else {
                    audioFeedback.getSoundPool().play(soundIdCompletion, 0.5F, 0.5F, 1, 0, 1);
                    if (feedbackModes.size() > currentVariant + 1 ){
                        initializeNextVariant();

                    } else {
                        final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
                        userData.setUserID(userId[0]);
                        finish();
                    }

                }

                // Handle questionnaire phase
            } else if (phase.equals(QUEST)){
                if (userData.getCurrentQuestionIndex() + 1 < userData.getCurrentQuestionList().size()) {
                    userData.incrementCurrentQuestionIndex();
                    userData.addMeasurement(userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex()));
                    taskCompleted = false;
                    audioFeedback.getSoundPool().play(soundIdDoubleTap, 0.5F, 0.5F, 1, 0, 1);
                    readAloudTarget();
                } else {
                    audioFeedback.getSoundPool().play(soundIdCompletion, 0.5F, 0.5F, 1, 0, 1);
                    if (feedbackModes.size() > currentVariant + 1 ){
                        initializeNextVariant();
                    } else {
                        final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
                        userData.setUserID(userId[0]);
                        finish();
                    }
                }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initializeNextVariant() {
        currentVariant = currentVariant +1;
        // set userId
        final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
        userData.setUserID(userId[0] + "_" + feedbackModes.get(currentVariant) + "_" + lengths.get(currentVariant) + "_" + orientations.get(currentVariant) + "_" + phase);
        // reset indices;
        userData.resetCurrentTargetIndex();
        userData.resetCurrentQuestionIndex();
        // setup tactile area according to new variant
        tactileArea.changeLayout(feedbackModes.get(currentVariant), lengths.get(currentVariant), orientations.get(currentVariant));

        // reset variables
        tasksStarted = false;
        taskCompleted = false;


    }

    // Setup long click listener for slider dragging interaction
    private View.OnLongClickListener setUpLongClickListener() {
        View.OnLongClickListener handleLongClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                if (!isLongClick && !taskCompleted) {
                    isLongClick = true;
                    if (feedbackModes.get(currentVariant).equals(AUDIO) || feedbackModes.get(currentVariant).equals(COMBINED)) {
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

        // Set input value
        if (userData.getLastMeasurement().getMeasurementPairs().size() > 0){
            audioFeedback.getSoundPool().play(soundIdDoubleTap, 1F, 1F, 1, 0, 1);
            taskCompleted = true;
            startTask = 0;
            userData.getLastMeasurement().setInput(userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getValue());
            // Set completion time value
            userData.getLastMeasurement().setCompletionTime((long) userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getTimestamp());

            userData.pushDataToDatabase(phase);
        }

    }

}