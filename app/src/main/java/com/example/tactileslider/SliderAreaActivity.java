package com.example.tactileslider;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    private TextToSpeech ttsObject;
    private boolean tasksStarted = false;

    // Intent Extras
    UserData userData;
    ArrayList<String> feedbackModes;
    ArrayList<String> lengths;
    ArrayList<String> orientations;

    int currentVariant = 0;
    String phase;
    private MediaPlayer doubleTapSound;
    private MediaPlayer successSound;
    private MediaPlayer longClickSound;
    private TextView coorinatesView;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide(); //<< this
        super.onCreate(savedInstanceState);

        getExtras();

        setContentView(R.layout.activity_slider_area);

        this.context = this;
        tactileArea = new TactileArea(this, userData, feedbackModes.get(currentVariant), lengths.get(currentVariant), orientations.get(currentVariant), phase, context);
        coorinatesView = findViewById(R.id.topBar);

        View.OnTouchListener getCoordinates = setUpTapAndMotionListener();
        findViewById(R.id.mainView).setOnTouchListener(getCoordinates);
        View.OnLongClickListener detectLongPress = setUpLongClickListener();
        findViewById(R.id.mainView).setOnLongClickListener(detectLongPress);

        // Setup vibration
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Setup sounds
        doubleTapSound = MediaPlayer.create(context, R.raw.doubletap);
        doubleTapSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        doubleTapSound.setVolume(0.25F, 0.25F);

        successSound = MediaPlayer.create(context, R.raw.completion_sound);
        successSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        successSound.setVolume(0.25F, 0.25F);

        longClickSound = MediaPlayer.create(context, R.raw.longclick);
        longClickSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
        longClickSound.setVolume(0.25F, 0.25F);


        // Setup tts component
        this.ttsObject = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
        Locale german = new Locale("de", "DE");
        ttsObject.setLanguage(german);

        // add first measurement
        if (phase.equals(STUDY)){
            userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        } else if (phase.equals(QUEST)){
            userData.addMeasurement(userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex()));
        }

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
        String id = userData.getUserId() + "_" + feedbackModes.get(0) + "_" + lengths.get(0) + "_" + orientations.get(0) + "_" + phase;
        userData.setUserID(id);
        // Add ID to database only if phase is study or quest
        if (phase.equals(STUDY) || phase.equals(QUEST)) {
            userData.createNewUserDataReference(id);
        }
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
                           // if (tasksStarted && !taskCompleted) {
                               // handleValueSelection(); // no differentiation between select and continue
                             if (tasksStarted && !taskCompleted) { // instead of && taskIsCompleted
                                handleValueSelection();
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

            doubleTapSound.start();
            readAloudTarget();
            tasksStarted = true;
            // In trial task, return to selection screen
            if (phase.equals(STUDY)){
                userData.addMeasurement(userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
            } else if (phase.equals(QUEST)){
                userData.addMeasurement(userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex()));
            }

        } else {
            successSound.start();

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
            coorinatesView.setText(userData.getUserId() + ":      Target " + userData.getCurrentTargetList().get(userData.getCurrentTargetIndex()));
        } else {
            String question = userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex());
            toSpeak = question;
            coorinatesView.setText(userData.getUserId() + ":      Question " + userData.getCurrentQuestionList().get(userData.getCurrentQuestionIndex()));
        }

        final int interval = 500; // half a second
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {


            public void run() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.15");
                ttsObject.setSpeechRate(1.3F);
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
                    //doubleTapSound.start();
                    readAloudTarget();
                } else {
                    successSound.start();
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
                    doubleTapSound.start();
                    readAloudTarget();
                } else {
                    successSound.start();
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
        final String[] userId = userData.getUserId().split("_" + feedbackModes.get(currentVariant));
        currentVariant = currentVariant +1;
        // set userId
        String id = userId[0] + "_" + feedbackModes.get(currentVariant) + "_" + lengths.get(currentVariant) + "_" + orientations.get(currentVariant) + "_" + phase;
        userData.setUserID(id);
        userData.createNewUserDataReference(id);
        // reset indices;
        userData.resetCurrentTargetIndex();
        userData.resetCurrentQuestionIndex();
        userData.setTargets(userData.createRandomizedTargetList(10));
        userData.setQuestions(userData.createRandomizedQuestionList());
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
                longClickSound.start(); // independent of feedabck mode, make longklick sound
                if (!isLongClick && !taskCompleted) {
                    isLongClick = true;
                   // if (feedbackModes.get(currentVariant).equals(AUDIO) || feedbackModes.get(currentVariant).equals(COMBINED)) {
                        // Haptic feedback that slider is activated
                        //VibrationEffect effect = null;
                      //  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        //    effect = VibrationEffect.createOneShot(150, 85);
                        //    vibrator.vibrate(effect);
                      //  }
                   // } else {
                      //  longClickSound.start();
                   // }
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
            doubleTapSound.start();
            taskCompleted = true;
            startTask = 0;
            userData.getLastMeasurement().setInput(userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getValue());
            // Set completion time value
            userData.getLastMeasurement().setCompletionTime((long) userData.getLastMeasurement().getMeasurementPairs().get(userData.getLastMeasurement().getMeasurementPairs().size() - 1).getTimestamp());

            userData.pushDataToDatabase(phase);
        }

    }

}